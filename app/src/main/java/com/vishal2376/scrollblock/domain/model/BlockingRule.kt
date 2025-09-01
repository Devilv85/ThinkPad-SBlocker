package com.vishal2376.scrollblock.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents customizable blocking rules for different apps and content types
 */
@Entity(tableName = "BlockingRule")
data class BlockingRule(
    @PrimaryKey(autoGenerate = true)
    val ruleId: Int = 0,
    val packageName: String,
    val contentType: String, // "shorts", "reels", "feed", "stories"
    val isEnabled: Boolean = true,
    val sensitivityLevel: Int = 3, // 1-5 scale
    val maxSessionTime: Int = 15, // minutes
    val cooldownPeriod: Int = 5, // minutes
    val allowedTimeSlots: String = "", // JSON array of time ranges
    val customViewIds: String = "", // JSON array of additional view IDs to block
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)