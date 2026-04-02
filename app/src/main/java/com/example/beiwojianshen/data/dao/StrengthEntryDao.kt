package com.example.beiwojianshen.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.beiwojianshen.data.entity.StrengthEntryEntity

data class StrengthEntryRow(
    val id: Long,
    val date: String,
    val exerciseId: Long,
    val exerciseName: String,
    val exerciseCategory: String,
    val sets: Int,
    val weight: Double,
    val reps: Int?,
    val note: String?
)

data class DateValuePoint(
    val date: String,
    val value: Double
)

data class DateCategoriesRow(
    val date: String,
    val categories: String?
)

@Dao
interface StrengthEntryDao {
    @Query(
        """
        SELECT s.id AS id, s.date AS date, s.exerciseId AS exerciseId, e.name AS exerciseName,
               e.category AS exerciseCategory,
               s.sets AS sets, s.weight AS weight, s.reps AS reps, s.note AS note
        FROM strength_entries s
        JOIN exercises e ON e.id = s.exerciseId
        WHERE s.date = :date
        ORDER BY s.id DESC
        """
    )
    suspend fun listByDate(date: String): List<StrengthEntryRow>

    @Query(
        """
        SELECT s.date AS date, GROUP_CONCAT(DISTINCT e.category) AS categories
        FROM strength_entries s
        JOIN exercises e ON e.id = s.exerciseId
        WHERE s.date >= :startDate AND s.date <= :endDate
        GROUP BY s.date
        ORDER BY s.date
        """
    )
    suspend fun categoriesByDateRange(startDate: String, endDate: String): List<DateCategoriesRow>

    @Query("SELECT * FROM strength_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): StrengthEntryEntity?

    @Insert
    suspend fun insert(entry: StrengthEntryEntity): Long

    @Update
    suspend fun update(entry: StrengthEntryEntity)

    @Delete
    suspend fun delete(entry: StrengthEntryEntity)

    @Query(
        """
        SELECT date AS date, MAX(weight) AS value
        FROM strength_entries
        WHERE exerciseId = :exerciseId
          AND date >= :startDate
          AND date <= :endDate
        GROUP BY date
        ORDER BY date
        """
    )
    suspend fun maxWeightByDate(exerciseId: Long, startDate: String, endDate: String): List<DateValuePoint>
}
