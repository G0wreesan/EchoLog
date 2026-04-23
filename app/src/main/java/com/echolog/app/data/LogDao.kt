package com.echolog.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM logs ORDER BY createdAt DESC")
    fun getAllLogs(): Flow<List<LogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)

    @Query("UPDATE logs SET isSynced = 1 WHERE id = :logId")
    suspend fun markAsSynced(logId: String)
}