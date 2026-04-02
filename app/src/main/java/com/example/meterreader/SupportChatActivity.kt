package com.example.meterreader

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.meterreader.databinding.ActivitySupportChatBinding

class SupportChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportChatBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Поддержка"

        try {
            val webView = WebView(this)
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                    Toast.makeText(this@SupportChatActivity, "Ошибка загрузки страницы. Проверьте интернет.", Toast.LENGTH_SHORT).show()
                }
            }
            webView.loadUrl("https://forms.gle/trMWP75AmmjbrySv8")
            binding.root.addView(webView, 0)
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось загрузить форму. Пожалуйста, напишите нам на почту support@example.com", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
