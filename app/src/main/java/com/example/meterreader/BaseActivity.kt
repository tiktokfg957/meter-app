package com.example.meterreader

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val theme = prefs.getString("theme", "system")
        applyTheme(theme)
        super.onCreate(savedInstanceState)
    }

    private fun applyTheme(theme: String?) {
        when (theme) {
            "dark" -> setTheme(R.style.AppTheme_Dark)
            "light" -> setTheme(R.style.AppTheme)
            else -> setTheme(R.style.AppTheme)
        }
    }
}
