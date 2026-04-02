package com.example.beiwojianshen.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cardio_entries",
    indices = [Index("date")]
)
data class CardioEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val type: String,
    val minutes: Int,
    val distanceKm: Double? = null,
    val caloriesKcal: Int? = null,
    val note: String? = null
)

