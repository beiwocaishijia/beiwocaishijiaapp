package com.example.beiwojianshen.ui.exercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beiwojianshen.R
import com.example.beiwojianshen.data.ExerciseCategories
import com.example.beiwojianshen.data.entity.ExerciseEntity

class ExerciseAdapter(
    private val onClick: (ExerciseEntity) -> Unit,
    private val onLongClick: (ExerciseEntity) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.VH>() {
    private val items = mutableListOf<ExerciseEntity>()

    fun submit(list: List<ExerciseEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.name
        val state = if (item.isActive) "启用" else "停用"
        val category = ExerciseCategories.displayName(item.category)
        val notePart = item.note?.takeIf { it.isNotBlank() } ?: ""
        holder.subtitle.text = if (notePart.isEmpty()) "$category  ·  $state" else "$category  ·  $state  ·  $notePart"

        holder.itemView.setOnClickListener { onClick(item) }
        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val subtitle: TextView = itemView.findViewById(R.id.subtitle)
    }
}
