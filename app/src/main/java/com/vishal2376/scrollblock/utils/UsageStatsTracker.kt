package com.vishal2376.scrollblock.utils

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.util.Calendar

/**
 * Tracks app usage statistics using Android's UsageStatsManager
 * 
 * This class provides detailed insights into app usage patterns
 * and helps correlate blocking effectiveness with actual usage reduction
 */
class UsageStatsTracker(private val context: Context) {
    
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    
    companion object {
        private const val TAG = "UsageStatsTracker"
    }
    
    /**
     * Gets usage statistics for supported apps over specified time period
     * 
     * @param days Number of days to look back
     * @return Map of package name to usage statistics
     */
    fun getAppUsageStats(days: Int = 7): Map<String, AppUsageStats> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val startTime = calendar.timeInMillis
        
        val usageStats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyMap()
        
        val supportedPackages = listOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.linkedin.android",
            "com.snapchat.android",
            "app.revanced.android.youtube",
            "app.rvx.android.youtube"
        )
        
        return usageStats
            .filter { it.packageName in supportedPackages }
            .associate { stats ->
                stats.packageName to AppUsageStats(
                    packageName = stats.packageName,
                    totalTimeInForeground = stats.totalTimeInForeground,
                    lastTimeUsed = stats.lastTimeUsed,
                    firstTimeStamp = stats.firstTimeStamp,
                    lastTimeStamp = stats.lastTimeStamp
                )
            }
    }
    
    /**
     * Gets today's usage for a specific app
     */
    fun getTodayUsage(packageName: String): AppUsageStats? {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        val usageStats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return null
        
        return usageStats
            .find { it.packageName == packageName }
            ?.let { stats ->
                AppUsageStats(
                    packageName = stats.packageName,
                    totalTimeInForeground = stats.totalTimeInForeground,
                    lastTimeUsed = stats.lastTimeUsed,
                    firstTimeStamp = stats.firstTimeStamp,
                    lastTimeStamp = stats.lastTimeStamp
                )
            }
    }
    
    /**
     * Calculates usage reduction percentage compared to previous period
     */
    fun calculateUsageReduction(packageName: String, days: Int = 7): Float {
        val calendar = Calendar.getInstance()
        val currentEndTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val currentStartTime = calendar.timeInMillis
        
        // Get current period usage
        val currentUsage = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentStartTime,
            currentEndTime
        )?.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
        
        // Get previous period usage
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val previousStartTime = calendar.timeInMillis
        val previousEndTime = currentStartTime
        
        val previousUsage = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            previousStartTime,
            previousEndTime
        )?.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
        
        return if (previousUsage > 0) {
            ((previousUsage - currentUsage).toFloat() / previousUsage.toFloat()) * 100f
        } else 0f
    }
    
    /**
     * Checks if usage stats permission is granted
     */
    fun hasUsageStatsPermission(): Boolean {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.MINUTE, -1)
        val startTime = calendar.timeInMillis
        
        val usageStats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        
        return !usageStats.isNullOrEmpty()
    }
}

/**
 * Simplified usage statistics data class
 */
data class AppUsageStats(
    val packageName: String,
    val totalTimeInForeground: Long,
    val lastTimeUsed: Long,
    val firstTimeStamp: Long,
    val lastTimeStamp: Long
)