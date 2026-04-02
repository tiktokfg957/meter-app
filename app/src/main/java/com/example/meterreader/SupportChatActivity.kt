package com.example.meterreader

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.meterreader.databinding.ActivitySupportChatBinding

class SupportChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Поддержка"

        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        // ЗАМЕНИТЕ НА ВАШУ ССЫЛКУ (Google Форма, Telegram, VK)
        webView.loadUrl("https://t.me/ваш_чат_или_бот")  // или ссылка на форму
        binding.root.addView(webView, 0) // добавляем WebView поверх всего
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
