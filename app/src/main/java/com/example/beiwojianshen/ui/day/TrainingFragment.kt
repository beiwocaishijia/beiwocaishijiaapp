package com.example.beiwojianshen.ui.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.beiwojianshen.R
import com.example.beiwojianshen.data.ExerciseCategories
import com.example.beiwojianshen.data.DbProvider
import com.example.beiwojianshen.data.dao.StrengthEntryRow
import com.example.beiwojianshen.data.entity.ExerciseEntity
import com.example.beiwojianshen.data.entity.StrengthEntryEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlin.coroutines.resume

class TrainingFragment : Fragment() {
    private val date: String by lazy { requireArguments().getString(ARG_DATE)!! }

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: StrengthAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_training, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)

        adapter = StrengthAdapter(
            onClick = { showUpsertDialog(it) },
            onLongClick = { confirmDelete(it) }
        )
        recyclerView.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            showUpsertDialog(null)
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        val ctx = requireContext()
        val db = DbProvider.get(ctx)
        viewLifecycleOwner.lifecycleScope.launch {
            val rows = withContext(Dispatchers.IO) { db.strengthEntryDao().listByDate(date) }
            adapter.submit(buildItems(rows))
            emptyView.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun buildItems(rows: List<StrengthEntryRow>): List<StrengthAdapter.Item> {
        if (rows.isEmpty()) return emptyList()
        val order = ExerciseCategories.all
        val grouped = rows.groupBy { it.exerciseCategory }
        val result = ArrayList<StrengthAdapter.Item>(rows.size + 8)
        for (cat in order) {
            val list = grouped[cat].orEmpty()
            if (list.isEmpty()) continue
            result.add(StrengthAdapter.Item.Header(ExerciseCategories.displayName(cat)))
            for (r in list) result.add(StrengthAdapter.Item.Row(r))
        }
        val other = grouped.keys.filter { it !in order }.sorted()
        for (cat in other) {
            val list = grouped[cat].orEmpty()
            if (list.isEmpty()) continue
            result.add(StrengthAdapter.Item.Header(ExerciseCategories.displayName(cat)))
            for (r in list) result.add(StrengthAdapter.Item.Row(r))
        }
        return result
    }

    private fun confirmDelete(row: StrengthEntryRow) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除记录")
            .setMessage("确认删除这条训练记录吗？")
            .setPositiveButton("删除") { _, _ ->
                delete(row.id)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun delete(id: Long) {
        val ctx = requireContext()
        val db = DbProvider.get(ctx)
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val entity = db.strengthEntryDao().getById(id) ?: return@withContext
                db.strengthEntryDao().delete(entity)
            }
            load()
        }
    }

    private fun showUpsertDialog(row: StrengthEntryRow?) {
        val ctx = requireContext()
        val db = DbProvider.get(ctx)

        viewLifecycleOwner.lifecycleScope.launch {
            val content = LayoutInflater.from(ctx).inflate(R.layout.dialog_strength_entry, null, false)
            val categorySpinner = content.findViewById<Spinner>(R.id.categorySpinner)
            val spinner = content.findViewById<Spinner>(R.id.exerciseSpinner)
            val addExerciseButton = content.findViewById<Button>(R.id.addExerciseButton)
            val setsInput = content.findViewById<EditText>(R.id.setsInput)
            val weightInput = content.findViewById<EditText>(R.id.weightInput)
            val repsInput = content.findViewById<EditText>(R.id.repsInput)
            val noteInput = content.findViewById<EditText>(R.id.noteInput)

            val categoryNames = ExerciseCategories.all.map { ExerciseCategories.displayName(it) }
            categorySpinner.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, categoryNames)
            val initialCategory = row?.exerciseCategory ?: ExerciseCategories.CHEST
            val initialCategoryIdx = ExerciseCategories.all.indexOf(initialCategory).takeIf { it >= 0 } ?: 0
            categorySpinner.setSelection(initialCategoryIdx)

            var exerciseEntities = withContext(Dispatchers.IO) { db.exerciseDao().listActiveByCategory(initialCategory) }.toMutableList()
            if (exerciseEntities.isEmpty()) {
                val created = promptCreateExercise(initialCategory)
                if (created == null) return@launch
                exerciseEntities = withContext(Dispatchers.IO) { db.exerciseDao().listActiveByCategory(initialCategory) }.toMutableList()
            }
            spinner.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, exerciseEntities.map { it.name })

            if (row != null) {
                val idx = exerciseEntities.indexOfFirst { it.id == row.exerciseId }.takeIf { it >= 0 } ?: 0
                spinner.setSelection(idx)
                setsInput.setText(row.sets.toString())
                weightInput.setText(row.weight.toString())
                row.reps?.let { repsInput.setText(it.toString()) }
                row.note?.let { noteInput.setText(it) }
            } else {
                setsInput.setText("3")
            }

            val dialog = AlertDialog.Builder(ctx)
                .setTitle(if (row == null) "新增训练" else "编辑训练")
                .setView(content)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create()

            dialog.setOnShowListener {
                categorySpinner.setOnItemSelectedListener(com.example.beiwojianshen.SimpleItemSelectedListener {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val cat = ExerciseCategories.all.getOrNull(categorySpinner.selectedItemPosition) ?: ExerciseCategories.CHEST
                        val list = withContext(Dispatchers.IO) { db.exerciseDao().listActiveByCategory(cat) }.toMutableList()
                        exerciseEntities = list
                        if (exerciseEntities.isEmpty()) {
                            val created = promptCreateExercise(cat)
                            if (created != null) {
                                exerciseEntities = withContext(Dispatchers.IO) { db.exerciseDao().listActiveByCategory(cat) }.toMutableList()
                            }
                        }
                        spinner.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, exerciseEntities.map { it.name })
                        val selectedIdx = if (row != null && row.exerciseCategory == cat) {
                            exerciseEntities.indexOfFirst { it.id == row.exerciseId }.takeIf { it >= 0 } ?: 0
                        } else {
                            0
                        }
                        spinner.setSelection(selectedIdx)
                    }
                })

                addExerciseButton.setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val cat = ExerciseCategories.all.getOrNull(categorySpinner.selectedItemPosition) ?: ExerciseCategories.CHEST
                        val created = promptCreateExercise(cat) ?: return@launch
                        exerciseEntities = withContext(Dispatchers.IO) { db.exerciseDao().listActiveByCategory(cat) }.toMutableList()
                        spinner.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, exerciseEntities.map { it.name })
                        val selectedIdx = exerciseEntities.indexOfFirst { it.id == created.id }.takeIf { it >= 0 } ?: 0
                        spinner.setSelection(selectedIdx)
                    }
                }

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val selectedExercise = exerciseEntities.getOrNull(spinner.selectedItemPosition) ?: exerciseEntities.first()

                    val sets = setsInput.text.toString().trim().toIntOrNull()
                    val weight = weightInput.text.toString().trim().toDoubleOrNull()
                    val reps = repsInput.text.toString().trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                    val note = noteInput.text.toString().trim().takeIf { it.isNotEmpty() }

                    if (sets == null || sets <= 0) {
                        Toast.makeText(ctx, "组数不合法", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (weight == null || weight < 0) {
                        Toast.makeText(ctx, "重量不合法", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    if (repsInput.text.toString().trim().isNotEmpty() && reps == null) {
                        Toast.makeText(ctx, "次数不合法", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val entity = StrengthEntryEntity(
                                id = row?.id ?: 0,
                                date = date,
                                exerciseId = selectedExercise.id,
                                sets = sets,
                                weight = weight,
                                reps = reps,
                                note = note
                            )
                            if (row == null) db.strengthEntryDao().insert(entity) else db.strengthEntryDao().update(entity)
                        }
                        dialog.dismiss()
                        load()
                    }
                }
            }

            dialog.show()
        }
    }

    private suspend fun promptCreateExercise(category: String): ExerciseEntity? = withContext(Dispatchers.Main) {
        val ctx = requireContext()
        val appCtx = ctx.applicationContext
        val input = EditText(ctx)
        input.hint = "动作名称（例如卧推）"
        val result = kotlinx.coroutines.suspendCancellableCoroutine<ExerciseEntity?> { cont ->
            val resumed = java.util.concurrent.atomic.AtomicBoolean(false)
            fun resumeOnce(value: ExerciseEntity?) {
                if (resumed.compareAndSet(false, true)) cont.resume(value)
            }
            AlertDialog.Builder(ctx)
                .setTitle("新增动作")
                .setView(input)
                .setPositiveButton("保存") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isEmpty()) {
                        Toast.makeText(ctx, "动作名称不能为空", Toast.LENGTH_SHORT).show()
                        resumeOnce(null)
                        return@setPositiveButton
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        val created = withContext(Dispatchers.IO) {
                            val db = DbProvider.get(appCtx)
                            try {
                                val id = db.exerciseDao().insert(ExerciseEntity(name = name, category = category))
                                ExerciseEntity(id = id, name = name, category = category)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        if (created == null) {
                            Toast.makeText(ctx, "保存失败：可能存在同名动作", Toast.LENGTH_SHORT).show()
                            resumeOnce(null)
                        } else {
                            resumeOnce(created)
                        }
                    }
                }
                .setNegativeButton("取消") { _, _ -> resumeOnce(null) }
                .setOnCancelListener { resumeOnce(null) }
                .show()
        }
        result
    }

    companion object {
        private const val ARG_DATE = "arg_date"

        fun newInstance(date: String) = TrainingFragment().apply {
            arguments = bundleOf(ARG_DATE to date)
        }
    }
}
