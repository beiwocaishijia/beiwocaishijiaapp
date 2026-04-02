package com.example.beiwojianshen.ui.day

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beiwojianshen.R
import com.example.beiwojianshen.data.entity.CardioEntryEntity

class CardioAdapter(
    private val onClick: (CardioEntryEntity) -> Unit,
    private val onLongClick: (CardioEntryEntity) -> Unit
) : RecyclerView.Adapter<CardioAdapter.VH>() {
    private val items = mutableListOf<CardioEntryEntity>()

    fun submit(list: List<CardioEntryEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_cardio_entry, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.type
        val distancePart = item.distanceKm?.let { "  ${it}km" } ?: ""
        val caloriesPart = item.caloriesKcal?.let { "  ${it}kcal" } ?: ""
        val notePart = item.note?.takeIf { it.isNotBlank() }?.let { "  ·  $it" } ?: ""
        holder.subtitle.text = "${item.minutes}分钟$distancePart$caloriesPart$notePart"

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

