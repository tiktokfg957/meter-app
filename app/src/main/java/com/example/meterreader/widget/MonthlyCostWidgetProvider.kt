package com.example.meterreader.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.meterreader.R
import com.example.meterreader.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class MonthlyCostWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val dbHelper = DatabaseHelper(context)
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val currentObjectId = prefs.getLong("current_object_id", 1) // по умолчанию объект 1
            val meters = dbHelper.getAllMeters(currentObjectId)
            val readings = dbHelper.getAllReadings()
            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            var totalMonth = 0f

            for (meter in meters) {
                val allReadings = readings.filter { it.meterId == meter.id }.sortedBy { it.date }
                val monthReadings = allReadings.filter { it.date.startsWith(currentMonth) }
                if (monthReadings.isNotEmpty()) {
                    val lastOfMonth = monthReadings.last()
                    val previous = allReadings.lastOrNull { it.date < currentMonth }
                    val prevValue = previous?.value ?: meter.initialReading
                    val diff = lastOfMonth.value - prevValue
                    totalMonth += diff * meter.tariff
                }
            }

            val views = RemoteViews(context.packageName, R.layout.widget_monthly_cost)
            views.setTextViewText(R.id.widget_amount, "%.2f ₽".format(totalMonth))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
