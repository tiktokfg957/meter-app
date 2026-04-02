package com.example.meterreader

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meterreader.databinding.ActivitySupportChatBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SupportChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportChatBinding
    private lateinit var adapter: SupportMessageAdapter
    private lateinit var prefs: SharedPreferences
    private val messagesList = mutableListOf<SupportMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Поддержка"

        prefs = getSharedPreferences("support_chat", MODE_PRIVATE)

        adapter = SupportMessageAdapter()
        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = adapter

        loadMessages()

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val message = SupportMessage(
                    text = text,
                    isFromUser = true,
                    timestamp = System.currentTimeMillis()
                )
                messagesList.add(message)
                saveMessages()
                adapter.submitList(messagesList.toList())
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
                binding.etMessage.text.clear()
                Toast.makeText(this, "Сообщение отправлено в поддержку", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMessages() {
        val json = prefs.getString("messages", "[]")
        val array = JSONArray(json)
        messagesList.clear()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val message = SupportMessage(
                text = obj.getString("text"),
                isFromUser = obj.getBoolean("isFromUser"),
                timestamp = obj.getLong("timestamp")
            )
            messagesList.add(message)
        }
        adapter.submitList(messagesList.toList())
        if (messagesList.isNotEmpty()) {
            binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun saveMessages() {
        val array = JSONArray()
        for (msg in messagesList) {
            val obj = JSONObject()
            obj.put("text", msg.text)
            obj.put("isFromUser", msg.isFromUser)
            obj.put("timestamp", msg.timestamp)
            array.put(obj)
        }
        prefs.edit().putString("messages", array.toString()).apply()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    data class SupportMessage(
        val text: String,
        val isFromUser: Boolean,
        val timestamp: Long
    )
}
