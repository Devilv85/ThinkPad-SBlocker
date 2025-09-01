package com.vishal2376.scrollblock.utils

import android.util.Log
import com.vishal2376.scrollblock.domain.model.ScrollPattern
import com.vishal2376.scrollblock.domain.model.SessionData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Advanced pattern recognition engine for behavioral analysis
 * 
 * This engine learns from user behavior to improve blocking accuracy over time.
 * It analyzes historical data to identify personal doom scrolling patterns.
 */
class PatternRecognitionEngine {
    
    companion object {
        private const val TAG = "PatternRecognition"
        private const val LEARNING_WINDOW_DAYS = 7
        private const val MIN_SESSIONS_FOR_LEARNING = 10
    }
    
    /**
     * Analyzes historical patterns to create personalized detection thresholds
     * 
     * @param historicalSessions User's historical session data
     * @return Personalized detection parameters
     */
    fun analyzeUserPatterns(historicalSessions: List<SessionData>): PersonalizedThresholds {
        if (historicalSessions.size < MIN_SESSIONS_FOR_LEARNING) {
            return getDefaultThresholds()
        }
        
        val doomScrollingSessions = historicalSessions.filter { it.sessionType == "doom_scroll" }
        val productiveSessions = historicalSessions.filter { it.sessionType == "productive" }
        
        // Calculate personalized thresholds
        val avgDoomVelocity = doomScrollingSessions.map { it.averageScrollVelocity }.average().toFloat()
        val avgProductiveVelocity = productiveSessions.map { it.averageScrollVelocity }.average().toFloat()
        
        val velocityThreshold = (avgDoomVelocity + avgProductiveVelocity) / 2
        
        val avgDoomDuration = doomScrollingSessions.map { it.endTime - it.startTime }.average().toLong()
        val durationThreshold = (avgDoomDuration * 0.7).toLong() // Trigger at 70% of average doom session
        
        Log.d(TAG, "Personalized thresholds - Velocity: $velocityThreshold, Duration: $durationThreshold")
        
        return PersonalizedThresholds(
            velocityThreshold = velocityThreshold,
            durationThreshold = durationThreshold,
            confidenceThreshold = calculatePersonalizedConfidence(doomScrollingSessions),
            adaptiveBlocking = true
        )
    }
    
    /**
     * Calculates personalized confidence threshold based on user's false positive rate
     */
    private fun calculatePersonalizedConfidence(doomSessions: List<SessionData>): Float {
        // Analyze user's typical doom scrolling patterns
        val avgScrollsPerSession = doomSessions.map { it.totalScrolls }.average()
        val avgVelocity = doomSessions.map { it.averageScrollVelocity }.average()
        
        // Adjust confidence threshold based on user behavior
        return when {
            avgScrollsPerSession > 100 && avgVelocity > 8.0 -> 0.6f // Very active user, lower threshold
            avgScrollsPerSession > 50 && avgVelocity > 5.0 -> 0.7f // Moderate user
            else -> 0.8f // Conservative user, higher threshold
        }
    }
    
    /**
     * Predicts likelihood of doom scrolling based on current context
     */
    fun predictDoomScrollingRisk(
        currentHour: Int,
        dayOfWeek: String,
        batteryLevel: Int,
        recentSessions: List<SessionData>
    ): Float {
        var riskScore = 0f
        
        // Time-based risk
        riskScore += when (currentHour) {
            in 22..23, in 0..2 -> 0.3f // Late night high risk
            in 12..13 -> 0.2f // Lunch break moderate risk
            in 17..20 -> 0.25f // Evening moderate-high risk
            else -> 0.1f
        }
        
        // Day of week risk
        riskScore += when (dayOfWeek.lowercase()) {
            "friday", "saturday", "sunday" -> 0.2f
            else -> 0.1f
        }
        
        // Battery level (lower battery = higher stress = higher risk)
        riskScore += when {
            batteryLevel < 20 -> 0.2f
            batteryLevel < 50 -> 0.1f
            else -> 0.05f
        }
        
        // Recent session analysis
        val recentDoomSessions = recentSessions.filter { 
            it.sessionType == "doom_scroll" && 
            System.currentTimeMillis() - it.endTime < 3600000L // Last hour
        }
        
        if (recentDoomSessions.isNotEmpty()) {
            riskScore += 0.3f // High risk if recent doom scrolling
        }
        
        return minOf(1.0f, riskScore)
    }
    
    /**
     * Determines optimal blocking strategy based on user patterns
     */
    fun getOptimalBlockingStrategy(
        userPatterns: PersonalizedThresholds,
        currentRisk: Float,
        appPackage: String
    ): BlockingStrategy {
        return when {
            currentRisk > 0.8f -> BlockingStrategy.AGGRESSIVE
            currentRisk > 0.6f -> BlockingStrategy.MODERATE
            currentRisk > 0.4f -> BlockingStrategy.GENTLE
            else -> BlockingStrategy.MINIMAL
        }
    }
    
    private fun getDefaultThresholds(): PersonalizedThresholds {
        return PersonalizedThresholds(
            velocityThreshold = 5.0f,
            durationThreshold = 120000L,
            confidenceThreshold = 0.7f,
            adaptiveBlocking = false
        )
    }
}

/**
 * Personalized detection thresholds based on user behavior analysis
 */
data class PersonalizedThresholds(
    val velocityThreshold: Float,
    val durationThreshold: Long,
    val confidenceThreshold: Float,
    val adaptiveBlocking: Boolean
)

/**
 * Different blocking strategies based on risk assessment
 */
enum class BlockingStrategy {
    MINIMAL,    // Just gentle nudges
    GENTLE,     // Short delays and suggestions
    MODERATE,   // Overlay blocking with easy bypass
    AGGRESSIVE  // Strong blocking with cooldown periods
}