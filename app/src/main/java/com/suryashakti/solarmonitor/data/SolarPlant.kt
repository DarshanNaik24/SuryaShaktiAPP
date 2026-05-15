package com.suryashakti.solarmonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solar_plants")
data class SolarPlant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,
    val location: String,
    val type: String // e.g., "Home", "Factory", "Farmhouse"
)
