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

        // Ваша ссылка на Google Форму
        webView.loadUrl("https://forms.gle/trMWP75AmmjbrySv8")

        binding.root.addView(webView, 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
