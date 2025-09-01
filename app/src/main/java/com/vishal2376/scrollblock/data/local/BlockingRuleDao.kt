package com.vishal2376.scrollblock.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vishal2376.scrollblock.domain.model.BlockingRule
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockingRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: BlockingRule)

    @Update
    suspend fun updateRule(rule: BlockingRule)

    @Query("SELECT * FROM BlockingRule WHERE packageName = :packageName AND isEnabled = 1")
    suspend fun getActiveRules(packageName: String): List<BlockingRule>

    @Query("SELECT * FROM BlockingRule")
    fun getAllRules(): Flow<List<BlockingRule>>

    @Query("UPDATE BlockingRule SET isEnabled = :isEnabled WHERE packageName = :packageName")
    suspend fun toggleAppBlocking(packageName: String, isEnabled: Boolean)
}