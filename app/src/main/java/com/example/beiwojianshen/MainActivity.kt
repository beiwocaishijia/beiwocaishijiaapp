package com.example.beiwojianshen

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beiwojianshen.data.AppNamePrefs
import com.example.beiwojianshen.data.DateKeys
import com.example.beiwojianshen.data.DbProvider
import com.example.beiwojianshen.data.WallpaperPrefs
import com.example.beiwojianshen.ui.calendar.CalendarAdapter
import com.example.beiwojianshen.ui.calendar.CalendarDayCell
import com.example.beiwojianshen.ui.WallpaperApplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var monthText: TextView
    private lateinit var prevMonthButton: Button
    private lateinit var nextMonthButton: Button
    private lateinit var calendarRecycler: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private lateinit var titleText: TextView
    private lateinit var wallpaperImage: ImageView

    private val pickWallpaper = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        WallpaperPrefs.set(this, uri.toString())
        WallpaperApplier.apply(this, wallpaperImage)
    }

    private val monthTitleFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    private val monthCal: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wallpaperImage = findViewById(R.id.wallpaperImage)
        titleText = findViewById(R.id.titleText)
        monthText = findViewById(R.id.monthText)
        prevMonthButton = findViewById(R.id.prevMonthButton)
        nextMonthButton = findViewById(R.id.nextMonthButton)
        calendarRecycler = findViewById(R.id.calendarRecycler)

        adapter = CalendarAdapter { cell ->
            startActivity(Intent(this, DayDetailActivity::class.java).putExtra(DayDetailActivity.EXTRA_DATE, cell.dateKey))
        }
        calendarRecycler.layoutManager = GridLayoutManager(this, 7)
        calendarRecycler.adapter = adapter

        prevMonthButton.setOnClickListener {
            monthCal.add(Calendar.MONTH, -1)
            refreshMonth()
        }
        nextMonthButton.setOnClickListener {
            monthCal.add(Calendar.MONTH, 1)
            refreshMonth()
        }

        refreshMonth()
    }

    override fun onResume() {
        super.onResume()
        applyAppName()
        WallpaperApplier.apply(this, wallpaperImage)
        refreshMonth()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_rename_app -> {
                showRenameDialog()
                true
            }
            R.id.action_change_wallpaper -> {
                pickWallpaper.launch(arrayOf("image/*"))
                true
            }
            R.id.action_clear_wallpaper -> {
                WallpaperPrefs.clear(this)
                WallpaperApplier.apply(this, wallpaperImage)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun applyAppName() {
        val name = AppNamePrefs.get(this)
        title = name
        titleText.text = name
    }

    private fun showRenameDialog() {
        val input = EditText(this)
        input.setText(AppNamePrefs.get(this))
        AlertDialog.Builder(this)
            .setTitle("修改名称")
            .setView(input)
            .setPositiveButton("保存") { _, _ ->
                val v = input.text.toString().trim()
                if (v.isEmpty()) {
                    Toast.makeText(this, "名称不能为空", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                AppNamePrefs.set(this, v)
                applyAppName()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun refreshMonth() {
        monthText.text = monthTitleFormat.format(monthCal.timeInMillis)
        val year = monthCal.get(Calendar.YEAR)
        val month = monthCal.get(Calendar.MONTH)
        val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val monthStartKey = DateKeys.fromYmd(year, month, 1)
        val monthEndKey = DateKeys.fromYmd(year, month, daysInMonth)

        val db = DbProvider.get(this)
        lifecycleScope.launch {
            val rows = withContext(Dispatchers.IO) { db.strengthEntryDao().categoriesByDateRange(monthStartKey, monthEndKey) }
            val map = rows.associate { row ->
                val cats = row.categories
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
                row.date to cats
            }

            val first = (monthCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            val firstDayOfWeek = first.firstDayOfWeek
            val dow = first.get(Calendar.DAY_OF_WEEK)
            val offset = (dow - firstDayOfWeek + 7) % 7

            val total = offset + daysInMonth
            val padEnd = (7 - (total % 7)) % 7
            val cells = ArrayList<CalendarDayCell>(total + padEnd)

            repeat(offset) {
                cells.add(CalendarDayCell(dateKey = "", dayOfMonth = 0, inMonth = false, categories = emptyList()))
            }

            for (day in 1..daysInMonth) {
                val dateKey = DateKeys.fromYmd(year, month, day)
                val categories = map[dateKey].orEmpty()
                cells.add(CalendarDayCell(dateKey = dateKey, dayOfMonth = day, inMonth = true, categories = categories))
            }

            repeat(padEnd) {
                cells.add(CalendarDayCell(dateKey = "", dayOfMonth = 0, inMonth = false, categories = emptyList()))
            }
            adapter.submit(cells)
        }
    }
}
