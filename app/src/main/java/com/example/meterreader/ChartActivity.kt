package com.example.meterreader

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ChartActivity : AppCompatActivity() {
    
    private lateinit var lineChart: LineChart
    private lateinit var tvTitle: TextView
    private lateinit var dbHelper: DatabaseHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        
        dbHelper = DatabaseHelper(this)
        
        val meterId = intent.getLongExtra("meter_id", 0)
        val meterName = intent.getStringExtra("meter_name") ?: "График"
        
        lineChart = findViewById(R.id.lineChart)
        tvTitle = findViewById(R.id.tvChartTitle)
        
        tvTitle.text = meterName
        
        val readings = dbHelper.getReadingsForMeter(meterId)
        val entries = ArrayList<Entry>()
        
        for (i in readings.indices) {
            entries.add(Entry(i.toFloat(), readings[i].value))
        }
        
        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Показания")
            dataSet.color = Color.BLUE
            dataSet.valueTextColor = Color.BLACK
            dataSet.lineWidth = 2f
            dataSet.circleRadius = 4f
            dataSet.setCircleColor(Color.BLUE)
            
            val lineData = LineData(dataSet)
            lineChart.data = lineData
            lineChart.invalidate() // обновить
        }
    }
}
