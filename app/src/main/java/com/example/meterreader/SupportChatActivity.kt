package com.example.meterreader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meterreader.data.database.AppDatabase
import com.example.meterreader.data.model.SupportMessage
import com.example.meterreader.databinding.ActivitySupportChatBinding
import kotlinx.coroutines.launch

class SupportChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportChatBinding
    private lateinit var adapter: SupportMessageAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }

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

        // Загружаем все сообщения из базы и отображаем
        lifecycleScope.launch {
            db.supportMessageDao().getAllMessages().collect { messages ->
                adapter.submitList(messages)
                // Прокручиваем вниз, если есть сообщения
                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                lifecycleScope.launch {
                    // Создаём сообщение от пользователя
                    val message = SupportMessage(text = text, isFromUser = true)
                    // Вставляем в базу
                    db.supportMessageDao().insert(message)
                    // Очищаем поле
                    binding.etMessage.text.clear()
                    Toast.makeText(this@SupportChatActivity, "Сообщение отправлено в поддержку", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
