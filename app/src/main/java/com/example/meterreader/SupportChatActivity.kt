package com.example.meterreader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meterreader.data.database.AppDatabase
import com.example.meterreader.data.model.SupportMessage
import com.example.meterreader.databinding.ActivitySupportChatBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class SupportChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportChatBinding
    private lateinit var adapter: SupportMessageAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }

    // Замените на свой токен и peer_id
    private val vkAccessToken = "vk1.a.tB2QSOyr67LxAq3AOCLn3INhS7QoxwPTPRAjGJYPjgpWgMIrTOr4MoynuJvgcEKRjkvFxVfU4UsCcaxducGXRojt4MqCD3k9PJtrccbAdO6nDGy9_4AcovKsmM3UfvjcFNI8xEFwxioKA-Yck7mWQ6088k6jTHargjRB-i4Qjd-SodcjTPVHRFQzNpx0Cwl5UarIFbu8t4PkpEEQwPYxvA"
    private val vkPeerId = "123456789"   // СЮДА ВСТАВЬТЕ ID ВАШЕГО СООБЩЕСТВА (число)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Поддержка"

        adapter = SupportMessageAdapter()
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter

        // Загружаем историю сообщений
        lifecycleScope.launch {
            db.supportMessageDao().getAllMessages().collect { messages ->
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun sendMessage(text: String) {
        val userId = getCurrentUserId()
        lifecycleScope.launch {
            val message = SupportMessage(text = text, isFromUser = true)
            db.supportMessageDao().insert(message)

            val success = sendToVK(text, userId)
            if (success) {
                Toast.makeText(this@SupportChatActivity, "Сообщение отправлено в поддержку", Toast.LENGTH_SHORT).show()
                binding.etMessage.text.clear()
            } else {
                Toast.makeText(this@SupportChatActivity, "Ошибка отправки. Попробуйте позже.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun sendToVK(message: String, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val fullMessage = "📩 Новое сообщение от $userId:\n\n$message"
            val formBody = FormBody.Builder()
                .add("access_token", vkAccessToken)
                .add("peer_id", vkPeerId)
                .add("message", fullMessage)
                .add("random_id", System.currentTimeMillis().toString())
                .add("v", "5.131")
                .build()
            val request = Request.Builder()
                .url("https://api.vk.com/method/messages.send")
                .post(formBody)
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getCurrentUserId(): String {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        var userId = prefs.getString("user_id", null)
        if (userId == null) {
            userId = "user_${System.currentTimeMillis()}"
            prefs.edit().putString("user_id", userId).apply()
        }
        return userId
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
