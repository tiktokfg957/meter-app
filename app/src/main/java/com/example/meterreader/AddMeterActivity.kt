package com.example.meterreader

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddMeterActivity : BaseActivity() {

    private lateinit var etName: EditText
    private lateinit var etType: EditText
    private lateinit var etInitial: EditText
    private lateinit var etTariff: EditText
    private lateinit var etTag: EditText
    private lateinit var spinnerObject: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var dbHelper: DatabaseHelper
    private var meterId: Long = 0
    private var objectsList = listOf<ObjectData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meter)

        dbHelper = DatabaseHelper(this)

        etName = findViewById(R.id.etMeterName)
        etType = findViewById(R.id.etMeterType)
        etInitial = findViewById(R.id.etInitialReading)
        etTariff = findViewById(R.id.etTariff)
        etTag = findViewById(R.id.etTag)
        spinnerObject = findViewById(R.id.spinnerObject)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        // Загрузка объектов
        objectsList = dbHelper.getAllObjects()
        val objectNames = objectsList.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, objectNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerObject.adapter = adapter

        meterId = intent.getLongExtra("meter_id", 0)
        if (meterId != 0L) {
            loadMeterData()
        }

        btnSave.setOnClickListener {
            saveMeter()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadMeterData() {
        val meter = dbHelper.getMeter(meterId)
        if (meter != null) {
            etName.setText(meter.name)
            etType.setText(meter.type)
            etInitial.setText(meter.initialReading.toString())
            etTariff.setText(meter.tariff.toString())
            etTag.setText(meter.tag)
            val objectIndex = objectsList.indexOfFirst { it.id == meter.objectId }
            if (objectIndex >= 0) spinnerObject.setSelection(objectIndex)
        }
    }

    private fun saveMeter() {
        val name = etName.text.toString().trim()
        val type = etType.text.toString().trim()
        val initialStr = etInitial.text.toString().trim()
        val tariffStr = etTariff.text.toString().trim()
        val tag = etTag.text.toString().trim()
        val selectedObjectIndex = spinnerObject.selectedItemPosition

        if (name.isEmpty() || type.isEmpty() || initialStr.isEmpty() || tariffStr.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val initial = initialStr.toFloatOrNull() ?: 0f
        val tariff = tariffStr.toFloatOrNull() ?: 0f
        val objectId = objectsList[selectedObjectIndex].id

        val meter = Meter(
            id = meterId,
            name = name,
            type = type,
            initialReading = initial,
            tariff = tariff,
            tag = tag,
            objectId = objectId
        )

        if (meterId == 0L) {
            dbHelper.insertMeter(meter)
            Toast.makeText(this, "Счётчик добавлен", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.updateMeter(meter)
            Toast.makeText(this, "Счётчик обновлён", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
