package com.example.beiwojianshen.data

import android.content.Context

object AppNamePrefs {
    private const val PREFS = "beiwo_prefs"
    private const val KEY = "app_display_name"
    private const val DEFAULT_NAME = "被窝健身"

    fun get(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, DEFAULT_NAME) ?: DEFAULT_NAME
    }

    fun set(context: Context, value: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, value).apply()
    }
}

