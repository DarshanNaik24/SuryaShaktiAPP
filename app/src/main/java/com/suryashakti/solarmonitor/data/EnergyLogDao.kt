package com.suryashakti.solarmonitor.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EnergyLogDao {
    @Query("SELECT * FROM energy_logs WHERE userId = :userId AND plantId = :plantId ORDER BY date DESC, id DESC")
    fun getAllLogs(userId: String, plantId: Long): Flow<List<EnergyLog>>

    @Query("SELECT * FROM energy_logs WHERE userId = :userId ORDER BY date DESC, id DESC")
    fun getAllLogsForUser(userId: String): Flow<List<EnergyLog>>

    @Query("SELECT * FROM energy_logs WHERE userId = :userId AND plantId = :plantId AND date = :date ORDER BY id DESC LIMIT 1")
    suspend fun getLogByDate(userId: String, plantId: Long, date: String): EnergyLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: EnergyLog)

    @Update
    suspend fun updateLog(log: EnergyLog)

    @Delete
    suspend fun deleteLog(log: EnergyLog)
}
