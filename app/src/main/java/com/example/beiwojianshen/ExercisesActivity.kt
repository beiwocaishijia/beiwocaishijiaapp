package com.example.beiwojianshen

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.beiwojianshen.data.ExerciseCategories
import com.example.beiwojianshen.data.DbProvider
import com.example.beiwojianshen.data.entity.ExerciseEntity
import com.example.beiwojianshen.ui.exercise.ExerciseAdapter
import com.example.beiwojianshen.ui.WallpaperApplier
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExercisesActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: ExerciseAdapter
    private lateinit var wallpaperImage: ImageView

    private var allExercises: List<ExerciseEntity> = emptyList()
    private var selectedCategory: String = ExerciseCategories.CHEST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercises)
        title = "动作库"

        wallpaperImage = findViewById(R.id.wallpaperImage)
        WallpaperApplier.apply(this, wallpaperImage)

        searchInput = findViewById(R.id.searchInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)

        adapter = ExerciseAdapter(
            onClick = { showUpsertDialog(it) },
            onLongClick = { showToggleActiveDialog(it) }
        )
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { showUpsertDialog(null) }

        searchInput.addTextChangedListener { applyFilter() }

        val categoryNames = ExerciseCategories.all.map { ExerciseCategories.displayName(it) }
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryNames)
        categorySpinner.setSelection(0)
        categorySpinner.setOnItemSelectedListener(SimpleItemSelectedListener {
            selectedCategory = ExerciseCategories.all.getOrNull(categorySpinner.selectedItemPosition) ?: ExerciseCategories.CHEST
            applyFilter()
        })

        load()
    }

    override fun onResume() {
        super.onResume()
        WallpaperApplier.apply(this, wallpaperImage)
    }

    private fun load() {
        val db = DbProvider.get(this)
        lifecycleScope.launch {
            allExercises = withContext(Dispatchers.IO) { db.exerciseDao().listAll() }
            applyFilter()
        }
    }

    private fun applyFilter() {
        val q = searchInput.text?.toString()?.trim().orEmpty()
        val byCategory = allExercises.filter { it.category == selectedCategory }
        val filtered = if (q.isEmpty()) byCategory else byCategory.filter { it.name.contains(q, ignoreCase = true) }
        adapter.submit(filtered)
        emptyView.visibility = if (filtered.isEmpty()) TextView.VISIBLE else TextView.GONE
    }

    private fun showToggleActiveDialog(entity: ExerciseEntity) {
        val nextActive = !entity.isActive
        val actionText = if (nextActive) "启用" else "停用"
        AlertDialog.Builder(this)
            .setTitle("$actionText 动作")
            .setMessage("确认$actionText「${entity.name}」吗？")
            .setPositiveButton(actionText) { _, _ ->
                toggleActive(entity, nextActive)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun toggleActive(entity: ExerciseEntity, active: Boolean) {
        val db = DbProvider.get(this)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.exerciseDao().setActive(entity.id, active) }
            load()
        }
    }

    private fun showUpsertDialog(entity: ExerciseEntity?) {
        val content = layoutInflater.inflate(R.layout.dialog_exercise, null, false)
        val nameInput = content.findViewById<EditText>(R.id.nameInput)
        val categorySpinner = content.findViewById<Spinner>(R.id.categorySpinner)
        val noteInput = content.findViewById<EditText>(R.id.noteInput)

        val categoryNames = ExerciseCategories.all.map { ExerciseCategories.displayName(it) }
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryNames)

        if (entity != null) {
            nameInput.setText(entity.name)
            val idx = ExerciseCategories.all.indexOf(entity.category).takeIf { it >= 0 } ?: 0
            categorySpinner.setSelection(idx)
            entity.note?.let { noteInput.setText(it) }
        } else {
            val idx = ExerciseCategories.all.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0
            categorySpinner.setSelection(idx)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (entity == null) "新增动作" else "编辑动作")
            .setView(content)
            .setPositiveButton("保存", null)
            .setNegativeButton("取消", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = nameInput.text.toString().trim()
                val note = noteInput.text.toString().trim().takeIf { it.isNotEmpty() }
                val category = ExerciseCategories.all.getOrNull(categorySpinner.selectedItemPosition) ?: ExerciseCategories.CHEST
                if (name.isEmpty()) {
                    Toast.makeText(this, "动作名称不能为空", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val db = DbProvider.get(this)
                lifecycleScope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        try {
                            if (entity == null) {
                                db.exerciseDao().insert(ExerciseEntity(name = name, category = category, note = note))
                            } else {
                                db.exerciseDao().update(entity.copy(name = name, category = category, note = note))
                            }
                            true
                        } catch (_: Exception) {
                            false
                        }
                    }
                    if (!ok) {
                        Toast.makeText(this@ExercisesActivity, "保存失败：可能存在同名动作", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    dialog.dismiss()
                    load()
                }
            }
        }

        dialog.show()
    }
}
