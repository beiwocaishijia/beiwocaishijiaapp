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
import com.example.beiwojianshen.data.entity.CardioEntryEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardioFragment : Fragment() {
    private val date: String by lazy { requireArguments().getString(ARG_DATE)!! }

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: CardioAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_cardio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyView = view.findViewById(R.id.emptyView)

        adapter = CardioAdapter(
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
            val rows = withContext(Dispatchers.IO) { db.cardioEntryDao().listByDate(date) }
            adapter.submit(rows)
            emptyView.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun confirmDelete(entity: CardioEntryEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除记录")
            .setMessage("确认删除这条有氧记录吗？")
            .setPositiveButton("删除") { _, _ ->
                delete(entity)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun delete(entity: CardioEntryEntity) {
        val db = DbProvider.get(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.cardioEntryDao().delete(entity) }
            load()
        }
    }

    private fun showUpsertDialog(entity: CardioEntryEntity?) {
        val ctx = requireContext()
        val content = LayoutInflater.from(ctx).inflate(R.layout.dialog_cardio_entry, null, false)
        val typeInput = content.findViewById<EditText>(R.id.typeInput)
        val minutesInput = content.findViewById<EditText>(R.id.minutesInput)
        val distanceInput = content.findViewById<EditText>(R.id.distanceInput)
        val caloriesInput = content.findViewById<EditText>(R.id.caloriesInput)
        val noteInput = content.findViewById<EditText>(R.id.noteInput)

        if (entity != null) {
            typeInput.setText(entity.type)
            minutesInput.setText(entity.minutes.toString())
            entity.distanceKm?.let { distanceInput.setText(it.toString()) }
            entity.caloriesKcal?.let { caloriesInput.setText(it.toString()) }
            entity.note?.let { noteInput.setText(it) }
        }

        val dialog = AlertDialog.Builder(ctx)
            .setTitle(if (entity == null) "新增有氧" else "编辑有氧")
            .setView(content)
            .setPositiveButton("保存", null)
            .setNegativeButton("取消", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val type = typeInput.text.toString().trim()
                val minutes = minutesInput.text.toString().trim().toIntOrNull()
                val distance = distanceInput.text.toString().trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
                val calories = caloriesInput.text.toString().trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                val note = noteInput.text.toString().trim().takeIf { it.isNotEmpty() }

                if (type.isEmpty()) {
                    Toast.makeText(ctx, "有氧类型不能为空", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (minutes == null || minutes <= 0) {
                    Toast.makeText(ctx, "时长不合法", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (distanceInput.text.toString().trim().isNotEmpty() && distance == null) {
                    Toast.makeText(ctx, "距离不合法", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (caloriesInput.text.toString().trim().isNotEmpty() && calories == null) {
                    Toast.makeText(ctx, "卡路里不合法", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val db = DbProvider.get(ctx)
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val upsert = CardioEntryEntity(
                            id = entity?.id ?: 0,
                            date = date,
                            type = type,
                            minutes = minutes,
                            distanceKm = distance,
                            caloriesKcal = calories,
                            note = note
                        )
                        if (entity == null) db.cardioEntryDao().insert(upsert) else db.cardioEntryDao().update(upsert)
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

        fun newInstance(date: String) = CardioFragment().apply {
            arguments = bundleOf(ARG_DATE to date)
        }
    }
}

