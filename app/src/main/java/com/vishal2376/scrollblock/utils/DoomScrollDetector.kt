package com.vishal2376.scrollblock.utils

import android.util.Log
import kotlin.math.abs

/**
 * Advanced doom scrolling detection engine using behavioral pattern analysis
 * 
 * This class implements sophisticated algorithms to differentiate between:
 * - Intentional content consumption (searching, direct navigation)
 * - Doom scrolling behavior (rapid, mindless scrolling)
 * 
 * Detection is based on multiple factors:
 * 1. Scroll velocity and acceleration patterns
 * 2. Pause duration between scrolls
 * 3. Session duration and engagement metrics
 * 4. Time-of-day and usage context
 */
class DoomScrollDetector {
    
    companion object {
        private const val TAG = "DoomScrollDetector"
        
        // Thresholds for doom scrolling detection
        private const val HIGH_VELOCITY_THRESHOLD = 5.0f // scrolls per second
        private const val RAPID_SCROLL_COUNT = 10 // consecutive rapid scrolls
        private const val SHORT_PAUSE_THRESHOLD = 500L // milliseconds
        private const val DOOM_SESSION_DURATION = 120000L // 2 minutes
        private const val MIN_CONFIDENCE_SCORE = 0.7f
        
        // Behavioral pattern weights
        private const val VELOCITY_WEIGHT = 0.3f
        private const val CONSISTENCY_WEIGHT = 0.25f
        private const val DURATION_WEIGHT = 0.2f
        private const val PAUSE_WEIGHT = 0.15f
        private const val CONTEXT_WEIGHT = 0.1f
    }
    
    private var sessionStartTime = 0L
    private var lastScrollTime = 0L
    private var scrollTimes = mutableListOf<Long>()
    private var scrollVelocities = mutableListOf<Float>()
    private var consecutiveRapidScrolls = 0
    private var totalScrollCount = 0
    
    /**
     * Analyzes current scroll event and determines if it's part of doom scrolling behavior
     * 
     * @param currentTime Current timestamp in milliseconds
     * @param scrollPosition Current scroll position (optional)
     * @return Confidence score (0.0 to 1.0) indicating likelihood of doom scrolling
     */
    fun analyzeScrollEvent(currentTime: Long, scrollPosition: Int = 0): Float {
        if (sessionStartTime == 0L) {
            sessionStartTime = currentTime
        }
        
        val timeSinceLastScroll = if (lastScrollTime > 0) currentTime - lastScrollTime else 0L
        lastScrollTime = currentTime
        totalScrollCount++
        
        // Track scroll timing
        scrollTimes.add(currentTime)
        
        // Calculate scroll velocity (scrolls per second)
        val velocity = calculateScrollVelocity(currentTime)
        scrollVelocities.add(velocity)
        
        // Update consecutive rapid scroll counter
        if (velocity > HIGH_VELOCITY_THRESHOLD && timeSinceLastScroll < SHORT_PAUSE_THRESHOLD) {
            consecutiveRapidScrolls++
        } else {
            consecutiveRapidScrolls = 0
        }
        
        // Clean old data (keep only last 30 seconds)
        cleanOldData(currentTime)
        
        // Calculate confidence score
        return calculateConfidenceScore(currentTime)
    }
    
    /**
     * Calculates scroll velocity based on recent scroll events
     */
    private fun calculateScrollVelocity(currentTime: Long): Float {
        val recentScrolls = scrollTimes.filter { currentTime - it <= 1000L } // Last 1 second
        return recentScrolls.size.toFloat()
    }
    
    /**
     * Calculates confidence score for doom scrolling detection
     * Uses weighted combination of multiple behavioral indicators
     */
    private fun calculateConfidenceScore(currentTime: Long): Float {
        val sessionDuration = currentTime - sessionStartTime
        
        // Velocity score (0.0 to 1.0)
        val avgVelocity = if (scrollVelocities.isNotEmpty()) {
            scrollVelocities.takeLast(10).average().toFloat()
        } else 0f
        val velocityScore = minOf(1.0f, avgVelocity / HIGH_VELOCITY_THRESHOLD)
        
        // Consistency score (how consistent the scrolling pattern is)
        val consistencyScore = calculateConsistencyScore()
        
        // Duration score (longer sessions = higher doom scroll likelihood)
        val durationScore = minOf(1.0f, sessionDuration.toFloat() / DOOM_SESSION_DURATION)
        
        // Pause score (shorter pauses = higher doom scroll likelihood)
        val pauseScore = calculatePauseScore()
        
        // Context score (time of day, app usage patterns)
        val contextScore = calculateContextScore(currentTime)
        
        // Weighted combination
        val confidenceScore = (velocityScore * VELOCITY_WEIGHT) +
                (consistencyScore * CONSISTENCY_WEIGHT) +
                (durationScore * DURATION_WEIGHT) +
                (pauseScore * PAUSE_WEIGHT) +
                (contextScore * CONTEXT_WEIGHT)
        
        Log.d(TAG, "Confidence Score: $confidenceScore (V:$velocityScore, C:$consistencyScore, D:$durationScore, P:$pauseScore, Ctx:$contextScore)")
        
        return confidenceScore
    }
    
    /**
     * Calculates how consistent the scrolling pattern is
     * More consistent = more likely to be doom scrolling
     */
    private fun calculateConsistencyScore(): Float {
        if (scrollVelocities.size < 3) return 0f
        
        val recentVelocities = scrollVelocities.takeLast(10)
        val mean = recentVelocities.average()
        val variance = recentVelocities.map { (it - mean) * (it - mean) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)
        
        // Lower standard deviation = higher consistency = higher score
        return maxOf(0f, 1f - (standardDeviation / mean).toFloat())
    }
    
    /**
     * Calculates pause score based on time between scrolls
     * Shorter pauses indicate more compulsive scrolling
     */
    private fun calculatePauseScore(): Float {
        if (scrollTimes.size < 2) return 0f
        
        val recentPauses = mutableListOf<Long>()
        for (i in 1 until minOf(scrollTimes.size, 10)) {
            recentPauses.add(scrollTimes[i] - scrollTimes[i-1])
        }
        
        val avgPause = recentPauses.average()
        return maxOf(0f, 1f - (avgPause / SHORT_PAUSE_THRESHOLD).toFloat())
    }
    
    /**
     * Calculates context score based on time of day and usage patterns
     */
    private fun calculateContextScore(currentTime: Long): Float {
        val hour = java.time.LocalTime.now().hour
        
        // Higher scores during typical doom scrolling hours
        return when (hour) {
            in 22..23, in 0..1 -> 0.8f // Late night
            in 12..13 -> 0.6f // Lunch break
            in 17..19 -> 0.7f // Evening
            else -> 0.3f
        }
    }
    
    /**
     * Determines if current behavior constitutes doom scrolling
     */
    fun isDoomScrolling(): Boolean {
        val currentTime = System.currentTimeMillis()
        val confidence = calculateConfidenceScore(currentTime)
        
        // Additional rapid scroll check
        val hasRapidScrolls = consecutiveRapidScrolls >= RAPID_SCROLL_COUNT
        
        return confidence >= MIN_CONFIDENCE_SCORE || hasRapidScrolls
    }
    
    /**
     * Gets detailed analysis of current session
     */
    fun getSessionAnalysis(): SessionAnalysis {
        val currentTime = System.currentTimeMillis()
        val sessionDuration = currentTime - sessionStartTime
        val avgVelocity = if (scrollVelocities.isNotEmpty()) {
            scrollVelocities.average().toFloat()
        } else 0f
        
        return SessionAnalysis(
            sessionDuration = sessionDuration,
            totalScrolls = totalScrollCount,
            averageVelocity = avgVelocity,
            consecutiveRapidScrolls = consecutiveRapidScrolls,
            confidenceScore = calculateConfidenceScore(currentTime),
            isDoomScrolling = isDoomScrolling()
        )
    }
    
    /**
     * Resets session data for new app session
     */
    fun resetSession() {
        sessionStartTime = 0L
        lastScrollTime = 0L
        scrollTimes.clear()
        scrollVelocities.clear()
        consecutiveRapidScrolls = 0
        totalScrollCount = 0
    }
    
    /**
     * Removes old scroll data to prevent memory buildup
     */
    private fun cleanOldData(currentTime: Long) {
        val cutoffTime = currentTime - 30000L // Keep last 30 seconds
        scrollTimes.removeAll { it < cutoffTime }
        
        // Keep velocities array manageable
        if (scrollVelocities.size > 50) {
            scrollVelocities = scrollVelocities.takeLast(30).toMutableList()
        }
    }
}

/**
 * Data class containing session analysis results
 */
data class SessionAnalysis(
    val sessionDuration: Long,
    val totalScrolls: Int,
    val averageVelocity: Float,
    val consecutiveRapidScrolls: Int,
    val confidenceScore: Float,
    val isDoomScrolling: Boolean
)