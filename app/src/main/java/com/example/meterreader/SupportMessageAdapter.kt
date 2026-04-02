package com.example.meterreader

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.meterreader.data.model.SupportMessage
import com.example.meterreader.databinding.ItemSupportMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class SupportMessageAdapter : RecyclerView.Adapter<SupportMessageAdapter.ViewHolder>() {

    private var messages = listOf<SupportMessage>()

    fun submitList(list: List<SupportMessage>) {
        messages = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class ViewHolder(private val binding: ItemSupportMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: SupportMessage) {
            binding.tvMessage.text = message.text
            val sdf = SimpleDateFormat("HH:mm dd.MM.yy", Locale.getDefault())
            binding.tvTime.text = sdf.format(Date(message.timestamp))

            // Определяем, от кого сообщение
            if (message.isFromUser) {
                // Сообщение пользователя – слева (серый)
                binding.tvMessage.setBackgroundResource(R.drawable.bubble_user)
                binding.tvMessage.setTextColor(android.graphics.Color.BLACK)
                // Сбрасываем отступы (по умолчанию слева)
                val params = binding.root.layoutParams as? ViewGroup.MarginLayoutParams
                params?.apply {
                    marginStart = 0
                    marginEnd = 80
                }
                binding.root.layoutParams = params
            } else {
                // Сообщение поддержки – справа (синий)
                binding.tvMessage.setBackgroundResource(R.drawable.bubble_support)
                binding.tvMessage.setTextColor(android.graphics.Color.WHITE)
                val params = binding.root.layoutParams as? ViewGroup.MarginLayoutParams
                params?.apply {
                    marginStart = 80
                    marginEnd = 0
                }
                binding.root.layoutParams = params
            }
        }
    }
}
