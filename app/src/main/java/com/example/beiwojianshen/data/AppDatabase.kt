package com.example.beiwojianshen.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.beiwojianshen.data.dao.CardioEntryDao
import com.example.beiwojianshen.data.dao.ExerciseDao
import com.example.beiwojianshen.data.dao.StrengthEntryDao
import com.example.beiwojianshen.data.dao.WeightEntryDao
import com.example.beiwojianshen.data.entity.CardioEntryEntity
import com.example.beiwojianshen.data.entity.ExerciseEntity
import com.example.beiwojianshen.data.entity.StrengthEntryEntity
import com.example.beiwojianshen.data.entity.WeightEntryEntity

@Database(
    entities = [
        ExerciseEntity::class,
        StrengthEntryEntity::class,
        CardioEntryEntity::class,
        WeightEntryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun strengthEntryDao(): StrengthEntryDao
    abstract fun cardioEntryDao(): CardioEntryDao
    abstract fun weightEntryDao(): WeightEntryDao
}
