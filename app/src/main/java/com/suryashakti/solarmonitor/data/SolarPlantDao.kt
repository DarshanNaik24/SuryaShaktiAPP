package com.suryashakti.solarmonitor.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SolarPlantDao {
    @Query("SELECT * FROM solar_plants WHERE userId = :userId")
    fun getPlantsByUserId(userId: String): Flow<List<SolarPlant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: SolarPlant)

    @Update
    suspend fun updatePlant(plant: SolarPlant)

    @Delete
    suspend fun deletePlant(plant: SolarPlant)
}
