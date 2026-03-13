package com.example.meterreader

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var prefs: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        radioGroup = findViewById(R.id.radioGroupTheme)
        btnSave = findViewById(R.id.btnSaveTheme)
        
        val currentTheme = prefs.getString("theme", "system")
        when (currentTheme) {
            "light" -> radioGroup.check(R.id.radioLight)
            "dark" -> radioGroup.check(R.id.radioDark)
            else -> radioGroup.check(R.id.radioSystem)
        }
        
        btnSave.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            val theme = when (selectedId) {
                R.id.radioLight -> "light"
                R.id.radioDark -> "dark"
                else -> "system"
            }
            
            prefs.edit().putString("theme", theme).apply()
            
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
