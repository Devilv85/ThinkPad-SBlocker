package com.vishal2376.scrollblock.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vishal2376.scrollblock.domain.model.ScrollPattern
import kotlinx.coroutines.flow.Flow

@Dao
interface ScrollPatternDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScrollPattern(pattern: ScrollPattern)

    @Query("SELECT * FROM ScrollPattern WHERE packageName = :packageName ORDER BY createdAt DESC LIMIT 10")
    fun getRecentPatterns(packageName: String): Flow<List<ScrollPattern>>

    @Query("SELECT * FROM ScrollPattern WHERE isDoomScrolling = 1 AND createdAt > :since")
    fun getDoomScrollingSessions(since: Long): Flow<List<ScrollPattern>>

    @Query("SELECT AVG(scrollVelocity) FROM ScrollPattern WHERE packageName = :packageName AND createdAt > :since")
    suspend fun getAverageScrollVelocity(packageName: String, since: Long): Float?

    @Query("DELETE FROM ScrollPattern WHERE createdAt < :cutoffTime")
    suspend fun cleanOldPatterns(cutoffTime: Long)
}