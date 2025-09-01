package com.vishal2376.scrollblock.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vishal2376.scrollblock.domain.model.AppUsage
import com.vishal2376.scrollblock.domain.model.BlockingRule
import com.vishal2376.scrollblock.domain.model.ScrollPattern
import com.vishal2376.scrollblock.domain.model.SessionData
import com.vishal2376.scrollblock.domain.model.Summary

@Database(
    entities = [
        AppUsage::class, 
        Summary::class, 
        ScrollPattern::class, 
        BlockingRule::class, 
        SessionData::class
    ], 
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun appUsageDao(): AppUsageDao
	abstract fun summaryDao(): SummaryDao
    abstract fun scrollPatternDao(): ScrollPatternDao
    abstract fun blockingRuleDao(): BlockingRuleDao
    abstract fun sessionDataDao(): SessionDataDao
}