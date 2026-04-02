package com.example.beiwojianshen.ui.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.beiwojianshen.R
import com.example.beiwojianshen.data.ExerciseCategories
import com.example.beiwojianshen.data.DateKeys

class CalendarAdapter(
    private val onClick: (CalendarDayCell) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.VH>() {
    private val items = mutableListOf<CalendarDayCell>()
    private val todayKey: String = DateKeys.today()

    fun submit(list: List<CalendarDayCell>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        if (!item.inMonth) {
            holder.dayText.text = ""
            holder.dayText.background = null
            holder.categoriesText.text = ""
            holder.categoriesText.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
            return
        }

        val isToday = item.dateKey == todayKey && item.inMonth
        holder.dayText.text = item.dayOfMonth.toString()
        if (isToday) {
            holder.dayText.setTextColor(Color.WHITE)
            holder.dayText.setBackgroundResource(R.drawable.bg_today_circle)
        } else {
            holder.dayText.background = null
            holder.dayText.setTextColor(Color.BLACK)
        }
        val cats = item.categories
            .distinct()
            .sortedBy { ExerciseCategories.all.indexOf(it).takeIf { idx -> idx >= 0 } ?: Int.MAX_VALUE }
            .map { ExerciseCategories.shortName(it) }
        holder.categoriesText.text = cats.joinToString(" ")
        holder.categoriesText.visibility = if (cats.isEmpty()) View.GONE else View.VISIBLE
        holder.categoriesText.setTextColor(0x99000000.toInt())

        holder.itemView.setOnClickListener { onClick(item) }
    }


    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
        val categoriesText: TextView = itemView.findViewById(R.id.categoriesText)
    }
}
