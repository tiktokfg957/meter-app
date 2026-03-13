package com.example.meterreader

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddMeterActivity : AppCompatActivity() {
    
    private lateinit var etName: EditText
    private lateinit var etType: EditText
    private lateinit var etInitial: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var dbHelper: DatabaseHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meter)
        
        dbHelper = DatabaseHelper(this)
        
        etName = findViewById(R.id.etMeterName)
        etType = findViewById(R.id.etMeterType)
        etInitial = findViewById(R.id.etInitialReading)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        
        btnSave.setOnClickListener {
            saveMeter()
        }
        
        btnCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun saveMeter() {
        val name = etName.text.toString().trim()
        val type = etType.text.toString().trim()
        val initialStr = etInitial.text.toString().trim()
        
        if (name.isEmpty() || type.isEmpty() || initialStr.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }
        
        val initial = initialStr.toFloatOrNull() ?: 0f
        
        val meter = Meter(
            name = name,
            type = type,
            initialReading = initial
        )
        
        dbHelper.insertMeter(meter)
        Toast.makeText(this, "Счётчик добавлен", Toast.LENGTH_SHORT).show()
        finish()
    }
}
