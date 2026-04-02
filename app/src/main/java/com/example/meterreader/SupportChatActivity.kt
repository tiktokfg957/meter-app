package com.example.meterreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class SupportChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = androidx.core.widget.NestedScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Служба поддержки"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 16)
        }
        layout.addView(title)

        // VK
        val vkCard = createCard(
            text = "Написать в VK",
            description = "Ответим в течение дня",
            icon = "📱",
            color = ContextCompat.getColor(this, android.R.color.holo_blue_dark)
        )
        vkCard.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/club237302917")))
        }
        layout.addView(vkCard)

        layout.addView(createSpacer())

        // Google Форма
        val formCard = createCard(
            text = "Заполнить форму",
            description = "Отправьте сообщение анонимно",
            icon = "📝",
            color = ContextCompat.getColor(this, android.R.color.holo_green_dark)
        )
        formCard.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/pnYnwALnLjyjMAzU7")))
        }
        layout.addView(formCard)

        scrollView.addView(layout)
        setContentView(scrollView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Поддержка"
    }

    private fun createCard(text: String, description: String, icon: String, color: Int): CardView {
        val card = CardView(this).apply {
            radius = 16f
            cardElevation = 4f
            setContentPadding(24, 16, 24, 16)
            setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }
        val innerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val iconView = TextView(this).apply {
            this.text = icon
            textSize = 32f
            setPadding(0, 0, 16, 0)
        }
        val textLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val titleView = TextView(this).apply {
            this.text = text
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(color)
        }
        val descView = TextView(this).apply {
            this.text = description
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }
        textLayout.addView(titleView)
        textLayout.addView(descView)
        innerLayout.addView(iconView)
        innerLayout.addView(textLayout)
        card.addView(innerLayout)
        return card
    }

    private fun createSpacer(): View {
        return TextView(this).apply { height = 24 }
    }

    override fun onSupportNavigateUp(): Boolean {
        finishAfterTransition()   // вместо onBackPressed()
        return true
    }
}
