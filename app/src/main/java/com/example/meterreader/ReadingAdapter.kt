package com.example.meterreader

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ReadingAdapter(
    private val readings: List<Reading>,
    private val initialReading: Float,
    private val onItemClick: (Reading) -> Unit,
    private val onItemLongClick: (Reading) -> Unit
) : RecyclerView.Adapter<ReadingAdapter.ReadingViewHolder>() {

    class ReadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvReadingDate)
        val tvValue: TextView = itemView.findViewById(R.id.tvReadingValue)
        val tvDiff: TextView = itemView.findViewById(R.id.tvReadingDiff)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading, parent, false)
        return ReadingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReadingViewHolder, position: Int) {
        // Защита от выхода за границы
        if (position >= readings.size) return

        val reading = readings[position]

        holder.tvDate.text = reading.date
        holder.tvValue.text = "Показания: ${reading.value}"

        val diff = if (position > 0) {
            reading.value - readings[position - 1].value
        } else {
            reading.value - initialReading
        }

        holder.tvDiff.text = "Разница: $diff"

        // Короткое нажатие – редактирование
        holder.itemView.setOnClickListener {
            try {
                onItemClick(reading)
            } catch (e: Exception) {
                Log.e("ReadingAdapter", "Ошибка при коротком нажатии", e)
                Toast.makeText(holder.itemView.context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Долгое нажатие – удаление
        holder.itemView.setOnLongClickListener {
            try {
                onItemLongClick(reading)
            } catch (e: Exception) {
                Log.e("ReadingAdapter", "Ошибка при долгом нажатии", e)
                Toast.makeText(holder.itemView.context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    override fun getItemCount() = readings.size
}
