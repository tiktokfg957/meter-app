package com.example.meterreader.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.meterreader.R
import com.example.meterreader.DatabaseHelper
import java.util.*

class GraphWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val currentObjectId = prefs.getLong("current_object_id", 1)
            val dbHelper = DatabaseHelper(context)
            val meters = dbHelper.getAllMeters(currentObjectId)
            val readings = dbHelper.getAllReadings()
            val firstMeter = meters.firstOrNull()
            val lastReadings = firstMeter?.let {
                readings.filter { it.meterId == it.id }.sortedByDescending { it.date }.take(3)
            } ?: emptyList()

            val graphText = if (lastReadings.isNotEmpty()) {
                lastReadings.joinToString(separator = " → ") { "${it.value}" }
            } else {
                "Нет данных"
            }

            val views = RemoteViews(context.packageName, R.layout.widget_graph)
            views.setTextViewText(R.id.widget_graph_data, graphText)

            val reminderDay = prefs.getInt("reminder_day", 1)
            views.setTextViewText(R.id.widget_reminder, "Напоминание: $reminderDay число")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
