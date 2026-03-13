package com.example.meterreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MeterAdapter(
    private val meters: List<Meter>,
    private val onItemClick: (Meter) -> Unit
) : RecyclerView.Adapter<MeterAdapter.MeterViewHolder>() {
    
    class MeterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvMeterName)
        val tvType: TextView = itemView.findViewById(R.id.tvMeterType)
        val tvInitial: TextView = itemView.findViewById(R.id.tvInitialReading)
        val btnChart: Button = itemView.findViewById(R.id.btnChart)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meter, parent, false)
        return MeterViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MeterViewHolder, position: Int) {
        val meter = meters[position]
        
        holder.tvName.text = meter.name
        holder.tvType.text = "Тип: ${meter.type}"
        holder.tvInitial.text = "Нач.: ${meter.initialReading}"
        
        holder.itemView.setOnClickListener {
            onItemClick(meter)
        }
        
        holder.btnChart.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, ChartActivity::class.java)
            intent.putExtra("meter_id", meter.id)
            intent.putExtra("meter_name", meter.name)
            context.startActivity(intent)
        }
        
        holder.btnDelete.setOnClickListener {
            val dbHelper = DatabaseHelper(holder.itemView.context)
            dbHelper.deleteMeter(meter.id)
            // Обновляем список (в реальном приложении нужно уведомить адаптер)
            (holder.itemView.context as? MainActivity)?.onResume()
        }
    }
    
    override fun getItemCount() = meters.size
}
