package com.example.beiwojianshen.ui.day

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beiwojianshen.R
import com.example.beiwojianshen.data.entity.WeightEntryEntity

class WeightAdapter(
    private val onClick: (WeightEntryEntity) -> Unit,
    private val onLongClick: (WeightEntryEntity) -> Unit
) : RecyclerView.Adapter<WeightAdapter.VH>() {
    private val items = mutableListOf<WeightEntryEntity>()

    fun submit(list: List<WeightEntryEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_weight_entry, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val time = DateFormat.format("HH:mm", item.dateTime).toString()
        holder.title.text = "${item.weight}kg"
        val notePart = item.note?.takeIf { it.isNotBlank() }?.let { "  ·  $it" } ?: ""
        holder.subtitle.text = "$time$notePart"

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

