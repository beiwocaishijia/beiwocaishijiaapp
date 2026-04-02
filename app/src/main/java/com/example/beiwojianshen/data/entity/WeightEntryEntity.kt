package com.example.beiwojianshen.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    indices = [Index("date"), Index("dateTime")]
)
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val dateTime: Long,
    val weight: Double,
    val note: String? = null
)

