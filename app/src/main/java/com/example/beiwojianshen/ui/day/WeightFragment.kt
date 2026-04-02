package com.example.beiwojianshen.ui.day

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.beiwojianshen.R
import com.example.beiwojianshen.data.DbProvider
import com.example.beiwojianshen.data.entity.WeightEntryEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeightFragment : Fragment() {
    private val date: String by lazy { requireArguments().getString(ARG_DATE)!! }

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: WeightAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_weight, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)

        adapter = WeightAdapter(
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
        val db = DbProvider.get(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            val rows = withContext(Dispatchers.IO) { db.weightEntryDao().listByDate(date) }
            adapter.submit(rows)
            emptyView.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun confirmDelete(entity: WeightEntryEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除记录")
            .setMessage("确认删除这条体重记录吗？")
            .setPositiveButton("删除") { _, _ ->
                delete(entity)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun delete(entity: WeightEntryEntity) {
        val db = DbProvider.get(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.weightEntryDao().delete(entity) }
            load()
        }
    }

    private fun showUpsertDialog(entity: WeightEntryEntity?) {
        val ctx = requireContext()
        val content = LayoutInflater.from(ctx).inflate(R.layout.dialog_weight_entry, null, false)
        val weightInput = content.findViewById<EditText>(R.id.weightInput)
        val noteInput = content.findViewById<EditText>(R.id.noteInput)

        if (entity != null) {
            weightInput.setText(entity.weight.toString())
            entity.note?.let { noteInput.setText(it) }
        }

        val dialog = AlertDialog.Builder(ctx)
            .setTitle(if (entity == null) "新增体重" else "编辑体重")
            .setView(content)
            .setPositiveButton("保存", null)
            .setNegativeButton("取消", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val weight = weightInput.text.toString().trim().toDoubleOrNull()
                val note = noteInput.text.toString().trim().takeIf { it.isNotEmpty() }

                if (weight == null || weight <= 0) {
                    Toast.makeText(ctx, "体重不合法", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val db = DbProvider.get(ctx)
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val upsert = WeightEntryEntity(
                            id = entity?.id ?: 0,
                            date = date,
                            dateTime = entity?.dateTime ?: System.currentTimeMillis(),
                            weight = weight,
                            note = note
                        )
                        if (entity == null) db.weightEntryDao().insert(upsert) else db.weightEntryDao().update(upsert)
                    }
                    dialog.dismiss()
                    load()
                }
            }
        }

        dialog.show()
    }

    companion object {
        private const val ARG_DATE = "arg_date"

        fun newInstance(date: String) = WeightFragment().apply {
            arguments = bundleOf(ARG_DATE to date)
        }
    }
}

