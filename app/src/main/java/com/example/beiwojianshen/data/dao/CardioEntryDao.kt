package com.example.beiwojianshen.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.beiwojianshen.data.entity.CardioEntryEntity

@Dao
interface CardioEntryDao {
    @Query("SELECT * FROM cardio_entries WHERE date = :date ORDER BY id DESC")
    suspend fun listByDate(date: String): List<CardioEntryEntity>

    @Insert
    suspend fun insert(entry: CardioEntryEntity): Long

    @Update
    suspend fun update(entry: CardioEntryEntity)

    @Delete
    suspend fun delete(entry: CardioEntryEntity)
}

