package com.suryashakti.solarmonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val userId: String,
    val password: String,
    val name: String
)
