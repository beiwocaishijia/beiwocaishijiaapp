package com.example.beiwojianshen.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.beiwojianshen.data.entity.WeightEntryEntity

data class WeightPoint(
    val date: String,
    val weight: Double
)

@Dao
interface WeightEntryDao {
    @Query("SELECT * FROM weight_entries WHERE date = :date ORDER BY dateTime DESC")
    suspend fun listByDate(date: String): List<WeightEntryEntity>

    @Insert
    suspend fun insert(entry: WeightEntryEntity): Long

    @Update
    suspend fun update(entry: WeightEntryEntity)

    @Delete
    suspend fun delete(entry: WeightEntryEntity)

    @Query(
        """
        SELECT w.date AS date, w.weight AS weight
        FROM weight_entries w
        JOIN (
            SELECT date AS date, MAX(dateTime) AS maxTime
            FROM weight_entries
            WHERE date >= :startDate AND date <= :endDate
            GROUP BY date
        ) m ON m.date = w.date AND m.maxTime = w.dateTime
        ORDER BY w.date
        """
    )
    suspend fun lastWeightByDate(startDate: String, endDate: String): List<WeightPoint>
}

