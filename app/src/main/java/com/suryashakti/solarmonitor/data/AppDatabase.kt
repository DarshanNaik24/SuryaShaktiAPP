package com.suryashakti.solarmonitor.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EnergyLog::class, User::class, SolarPlant::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun energyLogDao(): EnergyLogDao
    abstract fun userDao(): UserDao
    abstract fun solarPlantDao(): SolarPlantDao
}
