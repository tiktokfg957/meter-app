package com.example.meterreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        val reading = readings[position]
        
        holder.tvDate.text = reading.date
        holder.tvValue.text = "Показания: ${reading.value}"
        
        val diff = if (position > 0) {
            reading.value - readings[position - 1].value
        } else {
            reading.value - initialReading
        }
        
        holder.tvDiff.text = "Разница: $diff"
        
        holder.itemView.setOnClickListener {
            onItemClick(reading)
        }
        
        holder.itemView.setOnLongClickListener {
            onItemLongClick(reading)
            true
        }
    }
    
    override fun getItemCount() = readings.size
}
