package com.example.meterreader.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.meterreader.utils.NotificationHelper
import java.util.Calendar

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        val reminderDay = prefs.getInt("reminder_day", -1)
        if (reminderDay == -1) return Result.success()

        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        if (today == reminderDay) {
            NotificationHelper(applicationContext).showReminder()
        }
        return Result.success()
    }
}
