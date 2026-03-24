package com.example.meterreader

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class ChangelogDialog(context: Context) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_changelog)

        val tvTitle = findViewById<TextView>(R.id.tvChangelogTitle)
        val tvContent = findViewById<TextView>(R.id.tvChangelogContent)
        val btnClose = findViewById<Button>(R.id.btnChangelogClose)

        tvTitle.text = "Что нового в версии 1.0.10"
        tvContent.text = "✨ PRO версия с пробным периодом!\n" +
                "   • Экспорт в Excel\n" +
                "   • Расширенная статистика\n" +
                "   • Дополнительные темы оформления\n\n" +
                "📱 Наша страница VK: новости и советы\n\n" +
                "🔔 Теперь вы можете активировать PRO на 4 дня, подписавшись на VK-паблик"

        btnClose.setOnClickListener { dismiss() }
    }
}
