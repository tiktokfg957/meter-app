package com.example.meterreader

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var radioGroup: RadioGroup
    private lateinit var etReminderDay: EditText
    private lateinit var btnSave: Button
    private lateinit var prefs: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        radioGroup = findViewById(R.id.radioGroupTheme)
        etReminderDay = findViewById(R.id.etReminderDay)
        btnSave = findViewById(R.id.btnSaveTheme)
        
        val currentTheme = prefs.getString("theme", "system")
        when (currentTheme) {
            "light" -> radioGroup.check(R.id.radioLight)
            "dark" -> radioGroup.check(R.id.radioDark)
            else -> radioGroup.check(R.id.radioSystem)
        }
        
        val reminderDay = prefs.getInt("reminder_day", 1)
        etReminderDay.setText(reminderDay.toString())
        
        btnSave.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            val theme = when (selectedId) {
                R.id.radioLight -> "light"
                R.id.radioDark -> "dark"
                else -> "system"
            }
            
            val dayStr = etReminderDay.text.toString().trim()
            val day = if (dayStr.isNotEmpty()) dayStr.toInt() else 1
            
            prefs.edit().putString("theme", theme).putInt("reminder_day", day).apply()
            
            scheduleReminder(day)
            
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    private fun scheduleReminder(day: Int) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.DAYS) // для теста можно поставить меньше
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build())
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        
        Toast.makeText(this, "Напоминание установлено на $day число каждого месяца", Toast.LENGTH_LONG).show()
    }
}
