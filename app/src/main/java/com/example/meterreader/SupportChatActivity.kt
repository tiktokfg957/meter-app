package com.example.meterreader

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SupportChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "Страница поддержки временно недоступна. Пожалуйста, напишите нам на почту: support@example.com"
        textView.textSize = 16f
        textView.setPadding(32, 32, 32, 32)

        setContentView(textView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Поддержка"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
