package com.example.beiwojianshen.ui.day

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beiwojianshen.R
import com.example.beiwojianshen.data.dao.StrengthEntryRow

class StrengthAdapter(
    private val onClick: (StrengthEntryRow) -> Unit,
    private val onLongClick: (StrengthEntryRow) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    sealed interface Item {
        data class Header(val title: String) : Item
        data class Row(val row: StrengthEntryRow) : Item
    }

    private val items = mutableListOf<Item>()

    fun submit(list: List<Item>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Item.Header -> 0
            is Item.Row -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_section_header, parent, false)
            HeaderVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_strength_entry, parent, false)
            RowVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is Item.Header -> {
                (holder as HeaderVH).title.text = item.title
            }

            is Item.Row -> {
                val row = item.row
                val h = holder as RowVH
                h.title.text = row.exerciseName
                val repsPart = row.reps?.let { "  ${it}次" } ?: ""
                val notePart = row.note?.takeIf { it.isNotBlank() }?.let { "  ·  $it" } ?: ""
                h.subtitle.text = "${row.sets}组  ${row.weight}kg$repsPart$notePart"

                h.itemView.setOnClickListener { onClick(row) }
                h.itemView.setOnLongClickListener {
                    onLongClick(row)
                    true
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
    }

    class RowVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val subtitle: TextView = itemView.findViewById(R.id.subtitle)
    }
}
