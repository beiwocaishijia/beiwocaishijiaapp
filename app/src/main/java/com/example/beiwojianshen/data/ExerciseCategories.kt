package com.example.beiwojianshen.data

object ExerciseCategories {
    const val CHEST = "CHEST"
    const val BACK = "BACK"
    const val SHOULDER = "SHOULDER"
    const val ARMS = "ARMS"
    const val LEGS = "LEGS"

    val all = listOf(CHEST, BACK, SHOULDER, ARMS, LEGS)

    fun displayName(category: String): String = when (category) {
        CHEST -> "胸部"
        BACK -> "背部"
        SHOULDER -> "肩部"
        ARMS -> "胳膊"
        LEGS -> "腿"
        else -> "胸部"
    }

    fun shortName(category: String): String = when (category) {
        CHEST -> "胸"
        BACK -> "背"
        SHOULDER -> "肩"
        ARMS -> "臂"
        LEGS -> "腿"
        else -> "胸"
    }
}

