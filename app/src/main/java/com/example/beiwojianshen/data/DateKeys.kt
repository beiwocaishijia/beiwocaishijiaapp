package com.example.beiwojianshen.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateKeys {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    fun today(): String = dateFormat.format(System.currentTimeMillis())

    fun fromMillis(timeMillis: Long): String = dateFormat.format(timeMillis)

    fun fromYmd(year: Int, monthZeroBased: Int, dayOfMonth: Int): String {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, monthZeroBased)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return dateFormat.format(c.timeInMillis)
    }

    fun addDays(date: String, deltaDays: Int): String {
        val c = Calendar.getInstance()
        c.timeInMillis = dateFormat.parse(date)?.time ?: System.currentTimeMillis()
        c.add(Calendar.DAY_OF_MONTH, deltaDays)
        return dateFormat.format(c.timeInMillis)
    }
}
