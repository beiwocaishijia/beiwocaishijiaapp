package com.example.beiwojianshen.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.beiwojianshen.data.entity.ExerciseEntity

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE isActive = 1 ORDER BY name COLLATE NOCASE")
    suspend fun listActive(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE isActive = 1 AND category = :category ORDER BY name COLLATE NOCASE")
    suspend fun listActiveByCategory(category: String): List<ExerciseEntity>

    @Query("SELECT * FROM exercises ORDER BY isActive DESC, name COLLATE NOCASE")
    suspend fun listAll(): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY isActive DESC, name COLLATE NOCASE")
    suspend fun listAllByCategory(category: String): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(exercise: ExerciseEntity): Long

    @Update
    suspend fun update(exercise: ExerciseEntity)

    @Query("UPDATE exercises SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("UPDATE exercises SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)
}
