package com.example.beiwojianshen.ui.calendar

data class CalendarDayCell(
    val dateKey: String,
    val dayOfMonth: Int,
    val inMonth: Boolean,
    val categories: List<String>
)

