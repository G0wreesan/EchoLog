package com.echolog.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM logs WHERE user_id = :userId ORDER BY created_at DESC")
    fun getLogsForUser(userId: String): Flow<List<LogEntity>>

    @Query("SELECT * FROM logs WHERE user_id = :userId AND created_at LIKE :datePrefix || '%' ORDER BY created_at DESC")
    fun getLogsByDate(userId: String, datePrefix: String): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)

    @Query("UPDATE logs SET is_synced = 1 WHERE id = :logId")
    suspend fun markAsSynced(logId: String)

    @Query("SELECT * FROM logs WHERE is_synced = 0")
    suspend fun getUnsyncedLogs(): List<LogEntity>

    @Query("UPDATE logs SET is_synced = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Boolean)
}
