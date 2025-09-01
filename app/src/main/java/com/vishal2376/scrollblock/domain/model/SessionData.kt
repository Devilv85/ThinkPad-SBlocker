package com.vishal2376.scrollblock.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks detailed session information for analytics and pattern recognition
 */
@Entity(tableName = "SessionData")
data class SessionData(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long = 0,
    val totalScrolls: Int = 0,
    val blockedScrolls: Int = 0,
    val averageScrollVelocity: Float = 0f,
    val maxConsecutiveScrolls: Int = 0,
    val sessionType: String = "unknown", // "productive", "doom_scroll", "mixed"
    val exitMethod: String = "natural", // "natural", "blocked", "forced"
    val batteryLevel: Int = 100,
    val timeOfDay: String = "", // "morning", "afternoon", "evening", "night"
    val dayOfWeek: String = "",
    val createdAt: Long = System.currentTimeMillis()
)