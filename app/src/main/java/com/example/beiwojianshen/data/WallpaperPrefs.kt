package com.example.beiwojianshen.data

import android.content.Context

object WallpaperPrefs {
    private const val PREFS = "beiwo_prefs"
    private const val KEY = "wallpaper_uri"

    fun get(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
    }

    fun set(context: Context, uri: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, uri).apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
