package com.example.beiwojianshen.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DbProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        val existing = instance
        if (existing != null) return existing
        return synchronized(this) {
            val again = instance
            if (again != null) return@synchronized again
            val created = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "beiwojianshen.db"
            ).addMigrations(MIGRATION_1_2).build()
            instance = created
            created
        }
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE exercises ADD COLUMN category TEXT NOT NULL DEFAULT '${ExerciseCategories.CHEST}'")
            db.execSQL("UPDATE exercises SET category='${ExerciseCategories.LEGS}' WHERE name LIKE '%深蹲%' OR name LIKE '%腿%'")
            db.execSQL("UPDATE exercises SET category='${ExerciseCategories.BACK}' WHERE name LIKE '%引体%' OR name LIKE '%划船%' OR name LIKE '%背%' OR name LIKE '%硬拉%'")
            db.execSQL("UPDATE exercises SET category='${ExerciseCategories.SHOULDER}' WHERE name LIKE '%肩%' OR name LIKE '%推举%' OR name LIKE '%侧平举%'")
            db.execSQL("UPDATE exercises SET category='${ExerciseCategories.ARMS}' WHERE name LIKE '%弯举%' OR name LIKE '%臂%' OR name LIKE '%三头%' OR name LIKE '%二头%'")
            db.execSQL("UPDATE exercises SET category='${ExerciseCategories.CHEST}' WHERE name LIKE '%卧推%' OR name LIKE '%胸%' OR name LIKE '%俯卧撑%'")
        }
    }
}
