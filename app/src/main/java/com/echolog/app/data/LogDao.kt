package com.echolog.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM logs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getLogsForUser(userId: String): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)

    @Query("UPDATE logs SET isSynced = 1 WHERE id = :logId")
    suspend fun markAsSynced(logId: String)

    @Query("SELECT * FROM logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<LogEntity>

    @Query("UPDATE logs SET isSynced = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Boolean)
}