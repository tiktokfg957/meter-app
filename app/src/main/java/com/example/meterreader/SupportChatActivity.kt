package com.example.meterreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SupportChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val infoText = TextView(this).apply {
            text = "Выберите удобный способ связи с поддержкой:"
            textSize = 18f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(infoText)

        val vkButton = Button(this).apply {
            text = "📱 Написать в VK"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/club237302917"))
                startActivity(intent)
            }
        }
        layout.addView(vkButton)

        val spacer = TextView(this).apply {
            height = 32
        }
        layout.addView(spacer)

        val formButton = Button(this).apply {
            text = "📝 Заполнить Google Форму"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/pnYnwALnLjyjMAzU7"))
                startActivity(intent)
            }
        }
        layout.addView(formButton)

        setContentView(layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Поддержка"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
