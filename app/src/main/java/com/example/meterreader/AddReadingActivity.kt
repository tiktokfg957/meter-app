package com.example.meterreader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.meterreader.databinding.ActivityAddReadingBinding
import com.example.meterreader.utils.MLKitHelper
import java.text.SimpleDateFormat
import java.util.*

class AddReadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReadingBinding
    private lateinit var dbHelper: DatabaseHelper
    private var meterId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        meterId = intent.getLongExtra("meter_id", 0)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Добавить показание"

        // Кнопка камеры
        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            } else {
                openCamera()
            }
        }

        binding.btnSave.setOnClickListener {
            val readingStr = binding.etReading.text.toString().trim()
            if (readingStr.isEmpty()) {
                Toast.makeText(this, "Введите показание", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val reading = readingStr.toFloatOrNull()
            if (reading == null) {
                Toast.makeText(this, "Некорректное число", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dbHelper.addReading(meterId, reading, date)
            Toast.makeText(this, "Показание добавлено", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            MLKitHelper.recognizeText(bitmap,
                onResult = { recognizedText ->
                    val numbers = Regex("\\d+").findAll(recognizedText).map { it.value }.toList()
                    val firstNumber = numbers.firstOrNull()
                    if (firstNumber != null) {
                        binding.etReading.setText(firstNumber)
                        Toast.makeText(this, "Распознано: $firstNumber", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Не удалось найти число на фото", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = {
                    Toast.makeText(this, "Ошибка распознавания", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Нет доступа к камере", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
