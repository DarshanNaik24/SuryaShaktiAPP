package com.suryashakti.solarmonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "energy_logs")
data class EnergyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val plantId: Long = 0L,
    val date: String,
    val generationKwh: Double,
    val consumptionKwh: Double,
    val netEnergy: Double,
    val exportedKwh: Double,
    val gridImportKwh: Double,
    val weatherCondition: String,
    val independenceScore: Int,
    val costSaved: Double,
    val exportCredit: Double
)
