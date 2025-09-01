package com.vishal2376.scrollblock.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vishal2376.scrollblock.domain.model.SessionData
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionData)

    @Query("SELECT * FROM SessionData WHERE packageName = :packageName ORDER BY startTime DESC LIMIT 20")
    fun getRecentSessions(packageName: String): Flow<List<SessionData>>

    @Query("SELECT * FROM SessionData WHERE startTime >= :startTime AND endTime <= :endTime")
    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<SessionData>>

    @Query("SELECT SUM(blockedScrolls) FROM SessionData WHERE startTime >= :since")
    suspend fun getTotalBlockedScrolls(since: Long): Int

    @Query("SELECT AVG(averageScrollVelocity) FROM SessionData WHERE packageName = :packageName AND startTime >= :since")
    suspend fun getAverageSessionVelocity(packageName: String, since: Long): Float?

    @Query("DELETE FROM SessionData WHERE startTime < :cutoffTime")
    suspend fun cleanOldSessions(cutoffTime: Long)
}