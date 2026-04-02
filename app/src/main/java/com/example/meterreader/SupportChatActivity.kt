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

        // Создаём контейнер (LinearLayout) для кнопок
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // Заголовок / пояснение
        val infoText = TextView(this).apply {
            text = "Выберите удобный способ связи с поддержкой:"
            textSize = 18f
            setPadding(0, 0, 0, 32)
        }
        layout.addView(infoText)

        // Кнопка "Написать в ВК"
        val vkButton = Button(this).apply {
            text = "📱 Написать в VK"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/club237302917"))
                startActivity(intent)
            }
        }
        layout.addView(vkButton)

        // Отступ между кнопками
        val spacer = TextView(this).apply {
            height = 32
        }
        layout.addView(spacer)

        // Кнопка "Google Форма"
        val formButton = Button(this).apply {
            text = "📝 Заполнить Google Форму"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/pnYnwALnLjyjMAzU7"))
                startActivity(intent)
            }
        }
        layout.addView(formButton)

        // Дополнительный текст с почтой (опционально)
        val emailText = TextView(this).apply {
            text = "Или напишите нам на почту: support@example.com"
            textSize = 14f
            setPadding(0, 32, 0, 0)
        }
        layout.addView(emailText)

        setContentView(layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Поддержка"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
