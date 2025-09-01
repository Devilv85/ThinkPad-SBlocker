package com.vishal2376.scrollblock.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing scroll behavior patterns for doom scrolling detection
 */
@Entity(tableName = "ScrollPattern")
data class ScrollPattern(
    @PrimaryKey(autoGenerate = true)
    val patternId: Int = 0,
    val packageName: String,
    val sessionStartTime: Long,
    val sessionEndTime: Long = 0,
    val scrollVelocity: Float = 0f, // scrolls per second
    val scrollDirection: String = "vertical", // vertical, horizontal
    val consecutiveScrolls: Int = 0,
    val pauseDuration: Long = 0, // milliseconds between scrolls
    val isDoomScrolling: Boolean = false,
    val confidenceScore: Float = 0f, // 0.0 to 1.0
    val createdAt: Long = System.currentTimeMillis()
)