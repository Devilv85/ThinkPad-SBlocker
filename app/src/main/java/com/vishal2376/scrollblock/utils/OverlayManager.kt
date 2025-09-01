package com.vishal2376.scrollblock.utils

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vishal2376.scrollblock.R

/**
 * Manages overlay windows for selective content blocking
 * 
 * This class creates and manages overlay windows that appear over specific
 * UI elements to block doom scrolling while preserving app functionality
 */
class OverlayManager(private val context: Context) {
    
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: View? = null
    private var isOverlayShowing = false
    
    companion object {
        private const val TAG = "OverlayManager"
        private const val OVERLAY_DISPLAY_DURATION = 3000L // 3 seconds
    }
    
    /**
     * Shows a blocking overlay with customizable message and duration
     * 
     * @param message Message to display to user
     * @param duration How long to show overlay (milliseconds)
     * @param blockingType Type of content being blocked
     */
    fun showBlockingOverlay(
        message: String = "Take a break from scrolling",
        duration: Long = OVERLAY_DISPLAY_DURATION,
        blockingType: String = "general"
    ) {
        if (isOverlayShowing) return
        
        try {
            val layoutParams = createOverlayLayoutParams()
            overlayView = createOverlayView(message, blockingType)
            
            windowManager.addView(overlayView, layoutParams)
            isOverlayShowing = true
            
            // Auto-hide overlay after duration
            overlayView?.postDelayed({
                hideOverlay()
            }, duration)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show overlay: ${e.message}")
        }
    }
    
    /**
     * Creates overlay view with blocking message and helpful tips
     */
    private fun createOverlayView(message: String, blockingType: String): View {
        val inflater = ContextCompat.getSystemService(context, LayoutInflater::class.java)
        val view = inflater?.inflate(R.layout.overlay_blocking, null)
        
        view?.findViewById<TextView>(R.id.tvBlockingMessage)?.text = message
        view?.findViewById<TextView>(R.id.tvBlockingTip)?.text = getBlockingTip(blockingType)
        
        // Add click listener to dismiss overlay
        view?.setOnClickListener {
            hideOverlay()
        }
        
        return view ?: View(context)
    }
    
    /**
     * Creates appropriate window layout parameters for overlay
     */
    private fun createOverlayLayoutParams(): WindowManager.LayoutParams {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }
    }
    
    /**
     * Provides contextual tips based on blocking type
     */
    private fun getBlockingTip(blockingType: String): String {
        return when (blockingType) {
            "shorts" -> "Try watching a full video instead of scrolling through shorts"
            "reels" -> "Consider checking your messages or posting something creative"
            "feed" -> "Take a moment to reflect on what you're looking for"
            "stories" -> "Maybe it's time to create your own story"
            else -> "Take a deep breath and consider what you really need right now"
        }
    }
    
    /**
     * Hides the blocking overlay
     */
    fun hideOverlay() {
        try {
            overlayView?.let { view ->
                windowManager.removeView(view)
                overlayView = null
                isOverlayShowing = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide overlay: ${e.message}")
        }
    }
    
    /**
     * Shows a gentle nudge overlay for borderline cases
     */
    fun showNudgeOverlay(scrollCount: Int, sessionDuration: Long) {
        val minutes = sessionDuration / 60000
        val message = when {
            scrollCount > 50 -> "You've scrolled $scrollCount times. Maybe take a break?"
            minutes > 10 -> "You've been scrolling for ${minutes} minutes. Time for something else?"
            else -> "Mindful scrolling reminder"
        }
        
        showBlockingOverlay(message, 2000L, "nudge")
    }
    
    /**
     * Checks if overlay is currently visible
     */
    fun isOverlayVisible(): Boolean = isOverlayShowing
    
    /**
     * Cleanup method to be called when service is destroyed
     */
    fun cleanup() {
        hideOverlay()
    }
}