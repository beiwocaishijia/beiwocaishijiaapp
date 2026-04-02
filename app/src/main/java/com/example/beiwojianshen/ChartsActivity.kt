package com.example.beiwojianshen

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.beiwojianshen.data.DateKeys
import com.example.beiwojianshen.data.DbProvider
import com.example.beiwojianshen.data.entity.ExerciseEntity
import com.example.beiwojianshen.ui.chart.LineChartView
import com.example.beiwojianshen.ui.WallpaperApplier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChartsActivity : AppCompatActivity() {
    private lateinit var rangeSpinner: Spinner
    private lateinit var exerciseSpinner: Spinner
    private lateinit var weightChart: LineChartView
    private lateinit var strengthChart: LineChartView
    private lateinit var weightEmpty: TextView
    private lateinit var strengthEmpty: TextView
    private lateinit var wallpaperImage: ImageView

    private var exercises: List<ExerciseEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)
        title = "图表"

        wallpaperImage = findViewById(R.id.wallpaperImage)
        WallpaperApplier.apply(this, wallpaperImage)

        rangeSpinner = findViewById(R.id.rangeSpinner)
        exerciseSpinner = findViewById(R.id.exerciseSpinner)
        weightChart = findViewById(R.id.weightChart)
        strengthChart = findViewById(R.id.strengthChart)
        weightEmpty = findViewById(R.id.weightEmpty)
        strengthEmpty = findViewById(R.id.strengthEmpty)

        weightChart.setOnPointSelectedListener { label, value ->
            Toast.makeText(this, "$label  ${value}kg", Toast.LENGTH_SHORT).show()
        }
        strengthChart.setOnPointSelectedListener { label, value ->
            val exName = exercises.getOrNull(exerciseSpinner.selectedItemPosition)?.name ?: "动作"
            Toast.makeText(this, "$label  $exName  ${value}kg", Toast.LENGTH_SHORT).show()
        }

        val ranges = listOf("7天", "30天", "90天", "全部")
        rangeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ranges)
        rangeSpinner.setSelection(1)

        lifecycleScope.launch {
            val db = DbProvider.get(this@ChartsActivity)
            exercises = withContext(Dispatchers.IO) { db.exerciseDao().listAll() }
            val names = exercises.map { if (it.isActive) it.name else "${it.name}（停用）" }
            exerciseSpinner.adapter = ArrayAdapter(this@ChartsActivity, android.R.layout.simple_spinner_dropdown_item, names)
            refresh()
        }

        rangeSpinner.setOnItemSelectedListener(SimpleItemSelectedListener { refresh() })
        exerciseSpinner.setOnItemSelectedListener(SimpleItemSelectedListener { refreshStrength() })
    }

    override fun onResume() {
        super.onResume()
        WallpaperApplier.apply(this, wallpaperImage)
    }

    private fun refresh() {
        refreshWeight()
        refreshStrength()
    }

    private fun dateRange(): Pair<String, String> {
        val end = DateKeys.today()
        val selected = rangeSpinner.selectedItem?.toString().orEmpty()
        val start = when (selected) {
            "7天" -> DateKeys.addDays(end, -6)
            "30天" -> DateKeys.addDays(end, -29)
            "90天" -> DateKeys.addDays(end, -89)
            else -> "0000-01-01"
        }
        return start to end
    }

    private fun refreshWeight() {
        val db = DbProvider.get(this)
        val (start, end) = dateRange()
        lifecycleScope.launch {
            val points = withContext(Dispatchers.IO) { db.weightEntryDao().lastWeightByDate(start, end) }
            if (points.isEmpty()) {
                weightEmpty.visibility = TextView.VISIBLE
                weightChart.setSeries(emptyList(), emptyList())
            } else {
                weightEmpty.visibility = TextView.GONE
                weightChart.setSeries(points.map { it.date }, points.map { it.weight })
            }
        }
    }

    private fun refreshStrength() {
        val db = DbProvider.get(this)
        val (start, end) = dateRange()
        val selectedExercise = exercises.getOrNull(exerciseSpinner.selectedItemPosition)
        if (selectedExercise == null) {
            strengthEmpty.visibility = TextView.VISIBLE
            strengthChart.setSeries(emptyList(), emptyList())
            return
        }
        lifecycleScope.launch {
            val points = withContext(Dispatchers.IO) {
                db.strengthEntryDao().maxWeightByDate(selectedExercise.id, start, end)
            }
            if (points.isEmpty()) {
                strengthEmpty.visibility = TextView.VISIBLE
                strengthChart.setSeries(emptyList(), emptyList())
            } else {
                strengthEmpty.visibility = TextView.GONE
                strengthChart.setSeries(points.map { it.date }, points.map { it.value })
            }
        }
    }
}
