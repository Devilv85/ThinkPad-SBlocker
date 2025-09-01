package com.vishal2376.scrollblock.services

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.vishal2376.scrollblock.data.local.AppUsageDao
import com.vishal2376.scrollblock.data.local.BlockingRuleDao
import com.vishal2376.scrollblock.data.local.ScrollPatternDao
import com.vishal2376.scrollblock.data.local.SessionDataDao
import com.vishal2376.scrollblock.domain.model.AppUsage
import com.vishal2376.scrollblock.domain.model.ScrollPattern
import com.vishal2376.scrollblock.domain.model.SessionData
import com.vishal2376.scrollblock.utils.BlockingStrategy
import com.vishal2376.scrollblock.utils.ContentType
import com.vishal2376.scrollblock.utils.DoomScrollDetector
import com.vishal2376.scrollblock.utils.NOTIFICATION_ID
import com.vishal2376.scrollblock.utils.NotificationHelper
import com.vishal2376.scrollblock.utils.OverlayManager
import com.vishal2376.scrollblock.utils.PatternRecognitionEngine
import com.vishal2376.scrollblock.utils.SettingsStore
import com.vishal2376.scrollblock.utils.SmartContentDetector
import com.vishal2376.scrollblock.utils.SupportedApps
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject
import kotlin.math.max

/**
 * Enhanced Accessibility Service with advanced doom scrolling detection
 * 
 * This service implements sophisticated behavioral analysis to detect and prevent
 * doom scrolling while preserving productive app usage patterns.
 * 
 * Key Features:
 * - Real-time scroll pattern analysis
 * - Adaptive blocking based on user behavior
 * - Smart content type detection
 * - Personalized thresholds
 * - Comprehensive usage analytics
 */
@AndroidEntryPoint
class EnhancedScrollAccessibility : AccessibilityService() {
    
    @Inject lateinit var appUsageDao: AppUsageDao
    @Inject lateinit var scrollPatternDao: ScrollPatternDao
    @Inject lateinit var sessionDataDao: SessionDataDao
    @Inject lateinit var blockingRuleDao: BlockingRuleDao
    @Inject lateinit var store: SettingsStore
    
    // Core detection engines
    private lateinit var doomScrollDetector: DoomScrollDetector
    private lateinit var contentDetector: SmartContentDetector
    private lateinit var patternEngine: PatternRecognitionEngine
    private lateinit var overlayManager: OverlayManager
    
    // Session tracking
    private var currentSession: SessionData? = null
    private var sessionStartTime = 0L
    private var lastScrollTime = 0L
    private var scrollCount = 0
    private var blockedScrollCount = 0
    
    // App state tracking
    private var currentPackage = ""
    private var isBlocking = false
    private var blockingStrategy = BlockingStrategy.MODERATE
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val mainHandler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val TAG = "EnhancedScrollService"
        private const val MIN_SCROLL_INTERVAL = 100L // Minimum time between scroll events
        private const val SESSION_TIMEOUT = 30000L // 30 seconds of inactivity ends session
        private const val COOLDOWN_PERIOD = 5000L // 5 seconds cooldown after blocking
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        // Initialize detection engines
        doomScrollDetector = DoomScrollDetector()
        contentDetector = SmartContentDetector()
        patternEngine = PatternRecognitionEngine()
        overlayManager = OverlayManager(this)
        
        // Start foreground notification
        val notificationHelper = NotificationHelper(this)
        startForeground(NOTIFICATION_ID, notificationHelper.buildNotification())
        
        Log.d(TAG, "Enhanced Scroll Accessibility Service connected")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { accessibilityEvent ->
            when (accessibilityEvent.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    handleWindowStateChange(accessibilityEvent)
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    handleScrollEvent(accessibilityEvent)
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    handleContentChange(accessibilityEvent)
                }
            }
        }
    }
    
    /**
     * Handles window state changes (app switching, screen changes)
     */
    private fun handleWindowStateChange(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        
        // End previous session if switching apps
        if (currentPackage != packageName && currentSession != null) {
            endCurrentSession()
        }
        
        // Start new session for supported apps
        if (isSupportedApp(packageName)) {
            startNewSession(packageName)
        }
        
        currentPackage = packageName
    }
    
    /**
     * Handles scroll events with advanced pattern analysis
     */
    private fun handleScrollEvent(event: AccessibilityEvent) {
        val currentTime = System.currentTimeMillis()
        val packageName = event.packageName?.toString() ?: return
        
        // Ignore rapid consecutive events
        if (currentTime - lastScrollTime < MIN_SCROLL_INTERVAL) return
        
        // Only process supported apps
        if (!isSupportedApp(packageName)) return
        
        serviceScope.launch {
            try {
                // Check if app blocking is enabled
                val isAppEnabled = isAppBlockingEnabled(packageName)
                if (!isAppEnabled) return@launch
                
                // Analyze content type
                val contentAnalysis = contentDetector.analyzeContent(rootInActiveWindow, packageName)
                
                // Skip blocking for productive content
                if (!contentAnalysis.shouldBlock) {
                    Log.d(TAG, "Skipping block for productive content: ${contentAnalysis.contentType}")
                    return@launch
                }
                
                // Analyze scroll pattern
                val confidenceScore = doomScrollDetector.analyzeScrollEvent(currentTime)
                val isDoomScrolling = doomScrollDetector.isDoomScrolling()
                
                // Update session data
                updateCurrentSession(currentTime, isDoomScrolling)
                
                // Save scroll pattern for learning
                saveScrollPattern(packageName, confidenceScore, isDoomScrolling)
                
                // Apply blocking if doom scrolling detected
                if (isDoomScrolling && confidenceScore > 0.7f) {
                    applyBlocking(packageName, contentAnalysis.contentType, confidenceScore)
                } else if (confidenceScore > 0.5f) {
                    // Show gentle nudge for borderline cases
                    showGentleNudge()
                }
                
                scrollCount++
                lastScrollTime = currentTime
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing scroll event: ${e.message}")
            }
        }
    }
    
    /**
     * Handles content changes to detect new screens/content
     */
    private fun handleContentChange(event: AccessibilityEvent) {
        // Reset detector when content significantly changes
        val packageName = event.packageName?.toString() ?: return
        if (isSupportedApp(packageName)) {
            // Analyze new content
            serviceScope.launch {
                val contentAnalysis = contentDetector.analyzeContent(rootInActiveWindow, packageName)
                Log.d(TAG, "Content changed to: ${contentAnalysis.contentType} (confidence: ${contentAnalysis.confidence})")
            }
        }
    }
    
    /**
     * Starts a new usage session
     */
    private fun startNewSession(packageName: String) {
        sessionStartTime = System.currentTimeMillis()
        scrollCount = 0
        blockedScrollCount = 0
        doomScrollDetector.resetSession()
        
        currentSession = SessionData(
            packageName = packageName,
            startTime = sessionStartTime,
            timeOfDay = getTimeOfDay(),
            dayOfWeek = java.time.LocalDate.now().dayOfWeek.toString(),
            batteryLevel = getBatteryLevel()
        )
        
        Log.d(TAG, "Started new session for $packageName")
    }
    
    /**
     * Ends current session and saves data
     */
    private fun endCurrentSession() {
        currentSession?.let { session ->
            val endTime = System.currentTimeMillis()
            val sessionAnalysis = doomScrollDetector.getSessionAnalysis()
            
            val updatedSession = session.copy(
                endTime = endTime,
                totalScrolls = scrollCount,
                blockedScrolls = blockedScrollCount,
                averageScrollVelocity = sessionAnalysis.averageVelocity,
                maxConsecutiveScrolls = sessionAnalysis.consecutiveRapidScrolls,
                sessionType = if (sessionAnalysis.isDoomScrolling) "doom_scroll" else "productive"
            )
            
            serviceScope.launch {
                sessionDataDao.insertSession(updatedSession)
                
                // Also save to existing AppUsage table for compatibility
                val appUsage = AppUsage(
                    packageName = session.packageName,
                    scrollCount = scrollCount,
                    timeSpent = ((endTime - session.startTime) / 1000).toInt(),
                    appOpenCount = 1,
                    scrollsBlocked = blockedScrollCount
                )
                appUsageDao.insertAppUsage(appUsage)
            }
        }
        
        currentSession = null
        Log.d(TAG, "Ended session - Scrolls: $scrollCount, Blocked: $blockedScrollCount")
    }
    
    /**
     * Updates current session with scroll data
     */
    private fun updateCurrentSession(currentTime: Long, isDoomScrolling: Boolean) {
        currentSession?.let { session ->
            val duration = currentTime - session.startTime
            
            // Update session type based on behavior
            val sessionType = when {
                isDoomScrolling && duration > 60000L -> "doom_scroll"
                !isDoomScrolling && scrollCount < 20 -> "productive"
                else -> "mixed"
            }
            
            currentSession = session.copy(
                totalScrolls = scrollCount,
                blockedScrolls = blockedScrollCount,
                sessionType = sessionType
            )
        }
    }
    
    /**
     * Saves scroll pattern for machine learning
     */
    private fun saveScrollPattern(packageName: String, confidence: Float, isDoomScrolling: Boolean) {
        val pattern = ScrollPattern(
            packageName = packageName,
            sessionStartTime = sessionStartTime,
            scrollVelocity = if (scrollCount > 0) scrollCount.toFloat() / ((System.currentTimeMillis() - sessionStartTime) / 1000f) else 0f,
            consecutiveScrolls = scrollCount,
            isDoomScrolling = isDoomScrolling,
            confidenceScore = confidence
        )
        
        serviceScope.launch {
            scrollPatternDao.insertScrollPattern(pattern)
        }
    }
    
    /**
     * Applies appropriate blocking strategy based on detection confidence
     */
    private fun applyBlocking(packageName: String, contentType: ContentType, confidence: Float) {
        if (isBlocking) return // Prevent multiple simultaneous blocks
        
        isBlocking = true
        blockedScrollCount++
        
        when (blockingStrategy) {
            BlockingStrategy.MINIMAL -> {
                showGentleNudge()
            }
            BlockingStrategy.GENTLE -> {
                overlayManager.showBlockingOverlay(
                    message = "Mindful scrolling reminder",
                    duration = 2000L,
                    blockingType = contentType.name.lowercase()
                )
            }
            BlockingStrategy.MODERATE -> {
                overlayManager.showBlockingOverlay(
                    message = "Take a break from ${getAppName(packageName)}",
                    duration = 3000L,
                    blockingType = contentType.name.lowercase()
                )
                // Small delay before allowing next scroll
                mainHandler.postDelayed({ isBlocking = false }, 1000L)
                return
            }
            BlockingStrategy.AGGRESSIVE -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
                Toast.makeText(this, "Doom scrolling blocked - Take a break!", Toast.LENGTH_SHORT).show()
                mainHandler.postDelayed({ isBlocking = false }, COOLDOWN_PERIOD)
                return
            }
        }
        
        // Reset blocking flag for non-aggressive strategies
        mainHandler.postDelayed({ isBlocking = false }, 500L)
    }
    
    /**
     * Shows gentle nudge for borderline cases
     */
    private fun showGentleNudge() {
        val sessionAnalysis = doomScrollDetector.getSessionAnalysis()
        overlayManager.showNudgeOverlay(
            scrollCount = sessionAnalysis.totalScrolls,
            sessionDuration = sessionAnalysis.sessionDuration
        )
    }
    
    /**
     * Checks if blocking is enabled for specific app
     */
    private suspend fun isAppBlockingEnabled(packageName: String): Boolean {
        return when (packageName) {
            SupportedApps.Instagram.packageName -> store.instagramKey.first()
            SupportedApps.Youtube.packageName,
            SupportedApps.YoutubeRevanced.packageName,
            SupportedApps.YoutubeRevancedExtended.packageName -> store.youtubeKey.first()
            SupportedApps.Linkedin.packageName -> store.linkedinKey.first()
            SupportedApps.Snapchat.packageName -> store.snapchatKey.first()
            else -> false
        }
    }
    
    /**
     * Checks if app is supported for blocking
     */
    private fun isSupportedApp(packageName: String): Boolean {
        return listOf(
            SupportedApps.Instagram.packageName,
            SupportedApps.Youtube.packageName,
            SupportedApps.YoutubeRevanced.packageName,
            SupportedApps.YoutubeRevancedExtended.packageName,
            SupportedApps.Linkedin.packageName,
            SupportedApps.Snapchat.packageName
        ).contains(packageName)
    }
    
    /**
     * Gets user-friendly app name
     */
    private fun getAppName(packageName: String): String {
        return when (packageName) {
            SupportedApps.Instagram.packageName -> "Instagram"
            SupportedApps.Youtube.packageName,
            SupportedApps.YoutubeRevanced.packageName,
            SupportedApps.YoutubeRevancedExtended.packageName -> "YouTube"
            SupportedApps.Linkedin.packageName -> "LinkedIn"
            SupportedApps.Snapchat.packageName -> "Snapchat"
            else -> "this app"
        }
    }
    
    /**
     * Gets current time of day category
     */
    private fun getTimeOfDay(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 6..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..21 -> "evening"
            else -> "night"
        }
    }
    
    /**
     * Gets current battery level (simplified implementation)
     */
    private fun getBatteryLevel(): Int {
        // In a real implementation, you would use BatteryManager
        return 100 // Placeholder
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
        cleanup()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        cleanup()
    }
    
    /**
     * Cleanup resources when service is stopped
     */
    private fun cleanup() {
        endCurrentSession()
        overlayManager.cleanup()
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }
}