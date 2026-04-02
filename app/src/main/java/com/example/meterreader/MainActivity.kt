package com.example.meterreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.opencsv.CSVWriter

class MainActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnStats: Button
    private lateinit var btnGoals: Button
    private lateinit var btnExportCSV: Button
    private lateinit var btnExportExcel: Button
    private lateinit var btnSettings: Button
    private lateinit var tvTotalMonth: TextView
    private lateinit var spinnerObjects: Spinner
    private lateinit var cardTip: android.view.View
    private lateinit var tvTipTitle: TextView
    private lateinit var tvTipText: TextView
    private lateinit var dbHelper: DatabaseHelper

    private var currentObjectId = 0L
    private var objectsList = listOf<ObjectData>()
    private var objectAdapter: ArrayAdapter<ObjectData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        btnStats = findViewById(R.id.btnStats)
        btnGoals = findViewById(R.id.btnGoals)
        btnExportCSV = findViewById(R.id.btnExportCSV)
        btnExportExcel = findViewById(R.id.btnExportExcel)
        btnSettings = findViewById(R.id.btnSettings)
        tvTotalMonth = findViewById(R.id.tvTotalMonth)
        spinnerObjects = findViewById(R.id.spinnerObjects)
        cardTip = findViewById(R.id.cardTip)
        tvTipTitle = findViewById(R.id.tvTipTitle)
        tvTipText = findViewById(R.id.tvTipText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        objectsList = dbHelper.getAllObjects()
        objectAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, objectsList)
        objectAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerObjects.adapter = objectAdapter

        val defaultObject = objectsList.find { it.isDefault } ?: objectsList.firstOrNull()
        if (defaultObject != null) {
            currentObjectId = defaultObject.id
            spinnerObjects.setSelection(objectsList.indexOf(defaultObject))
        }

        spinnerObjects.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                currentObjectId = objectsList[position].id
                loadMeters()
                loadTip()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddMeterActivity::class.java))
        }

        btnStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        btnGoals.setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }

        btnExportCSV.setOnClickListener {
            exportToCSV()
        }

        btnExportExcel.setOnClickListener {
            if (isProActive()) {
                exportToExcel()
            } else {
                Toast.makeText(this, "Функция доступна в PRO версии. Активируйте в настройках", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val btnVK = findViewById<Button>(R.id.btnVK)
        btnVK.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/club236967018"))
            startActivity(intent)
        }

        val tvProBadge = findViewById<TextView>(R.id.tvProBadge)
        tvProBadge.setOnClickListener {
            val dialog = ProDialogFragment()
            dialog.show(supportFragmentManager, "pro_dialog")
        }

        // Реклама полностью удалена

        checkOnboarding()
        loadMeters()
    }

    override fun onResume() {
        super.onResume()
        loadMeters()
        loadTip()
    }

    private fun loadMeters() {
        val meters = dbHelper.getAllMeters(currentObjectId)
        val adapter = MeterAdapter(
            meters = meters,
            onItemClick = { meter ->
                val intent = Intent(this, AddReadingActivity::class.java)
                intent.putExtra("meter_id", meter.id)
                startActivity(intent)
            },
            onItemLongClick = { meter ->
                val intent = Intent(this, AddMeterActivity::class.java)
                intent.putExtra("meter_id", meter.id)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter
        updateTotalForCurrentMonth()
    }

    private fun updateTotalForCurrentMonth() {
        val meters = dbHelper.getAllMeters(currentObjectId)
        val readings = dbHelper.getAllReadings()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        var totalMonth = 0f

        for (meter in meters) {
            val allReadings = readings.filter { it.meterId == meter.id }.sortedBy { it.date }
            val monthReadings = allReadings.filter { it.date.startsWith(currentMonth) }
            if (monthReadings.isNotEmpty()) {
                val lastOfMonth = monthReadings.last()
                val previous = allReadings.lastOrNull { it.date < currentMonth }
                val prevValue = previous?.value ?: meter.initialReading
                val diff = lastOfMonth.value - prevValue
                totalMonth += diff * meter.tariff
            }
        }
        tvTotalMonth.text = "%.2f ₽".format(totalMonth)
    }

    private fun loadTip() {
        val meters = dbHelper.getAllMeters(currentObjectId)
        val readings = dbHelper.getAllReadings()
        val calendar = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.MONTH, -1)
        val prevMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)

        var tipMessage: String? = null
        for (meter in meters) {
            val monthReadings = readings.filter { it.meterId == meter.id && it.date.startsWith(currentMonth) }.sortedBy { it.date }
            val prevMonthReadings = readings.filter { it.meterId == meter.id && it.date.startsWith(prevMonth) }.sortedBy { it.date }
            if (monthReadings.isNotEmpty() && prevMonthReadings.isNotEmpty()) {
                val currentDiff = monthReadings.last().value - (monthReadings.getOrNull(monthReadings.size - 2)?.value ?: meter.initialReading)
                val prevDiff = prevMonthReadings.last().value - (prevMonthReadings.getOrNull(prevMonthReadings.size - 2)?.value ?: meter.initialReading)
                if (prevDiff > 0) {
                    val change = (currentDiff - prevDiff) / prevDiff * 100
                    if (change > 20) {
                        tipMessage = when (meter.type) {
                            "Вода" -> "Расход воды вырос на ${"%.0f".format(change)}% по сравнению с прошлым месяцем. Проверьте краны и сантехнику!"
                            "Свет" -> "Расход электроэнергии вырос на ${"%.0f".format(change)}%. Возможно, оставлены включёнными приборы в режиме ожидания."
                            "Газ" -> "Расход газа вырос на ${"%.0f".format(change)}%. Проверьте, нет ли утечек."
                            else -> "Расход по счётчику ${meter.name} вырос на ${"%.0f".format(change)}%."
                        }
                        break
                    }
                }
            }
        }

        if (tipMessage != null) {
            tvTipText.text = tipMessage
            cardTip.visibility = android.view.View.VISIBLE
        } else {
            cardTip.visibility = android.view.View.GONE
        }
    }

    private fun checkOnboarding() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastVersion = prefs.getInt("last_version_code", 0)
        val currentVersion = packageManager.getPackageInfo(packageName, 0).versionCode
        if (currentVersion > lastVersion && lastVersion < 14) {
            AlertDialog.Builder(this)
                .setTitle("Что нового в 1.0.6?")
                .setMessage("✨ Мультиобъектный учёт – теперь вы можете вести показания для квартиры, дачи и других объектов.\n📊 Расширенные виджеты – следите за расходами прямо с рабочего стола.\n💡 Персонализированные советы по экономии.\n🎨 Улучшенная анимация и подсказки.")
                .setPositiveButton("Понятно", null)
                .show()
            prefs.edit().putInt("last_version_code", currentVersion).apply()
        }
    }

    private fun exportToCSV() {
        try {
            val meters = dbHelper.getAllMeters(currentObjectId)
            val readings = dbHelper.getAllReadings()
            val downloadsDir = getExternalFilesDir(null)?.absolutePath ?: filesDir.absolutePath
            val file = File(downloadsDir, "meter_readings_${System.currentTimeMillis()}.csv")

            FileWriter(file).use { writer ->
                CSVWriter(writer).use { csvWriter ->
                    csvWriter.writeNext(arrayOf(
                        "ID счётчика", "Название", "Тип", "Дата", "Показания", "Разница", "Стоимость"
                    ))
                    for (meter in meters) {
                        val meterReadings = readings.filter { it.meterId == meter.id }
                        for (i in meterReadings.indices) {
                            val reading = meterReadings[i]
                            val diff = if (i > 0) reading.value - meterReadings[i-1].value else reading.value - meter.initialReading
                            val cost = diff * meter.tariff
                            csvWriter.writeNext(arrayOf(
                                meter.id.toString(), meter.name, meter.type, reading.date,
                                reading.value.toString(), diff.toString(), "%.2f".format(cost)
                            ))
                        }
                    }
                }
            }
            Toast.makeText(this, "CSV сохранён: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportToExcel() {
        try {
            val meters = dbHelper.getAllMeters(currentObjectId)
            val readings = dbHelper.getAllReadings()
            val workbook: Workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Показания")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("ID счётчика")
            headerRow.createCell(1).setCellValue("Название")
            headerRow.createCell(2).setCellValue("Тип")
            headerRow.createCell(3).setCellValue("Дата")
            headerRow.createCell(4).setCellValue("Показания")
            headerRow.createCell(5).setCellValue("Разница")
            headerRow.createCell(6).setCellValue("Стоимость")
            var rowNum = 1
            for (meter in meters) {
                val meterReadings = readings.filter { it.meterId == meter.id }
                for (i in meterReadings.indices) {
                    val reading = meterReadings[i]
                    val diff = if (i > 0) reading.value - meterReadings[i-1].value else reading.value - meter.initialReading
                    val cost = diff * meter.tariff
                    val row = sheet.createRow(rowNum++)
                    row.createCell(0).setCellValue(meter.id.toDouble())
                    row.createCell(1).setCellValue(meter.name)
                    row.createCell(2).setCellValue(meter.type)
                    row.createCell(3).setCellValue(reading.date)
                    row.createCell(4).setCellValue(reading.value.toDouble())
                    row.createCell(5).setCellValue(diff.toDouble())
                    row.createCell(6).setCellValue(cost.toDouble())
                }
            }
            val downloadsDir = getExternalFilesDir(null)?.absolutePath ?: filesDir.absolutePath
            val file = File(downloadsDir, "meter_readings_${System.currentTimeMillis()}.xlsx")
            file.outputStream().use { outputStream -> workbook.write(outputStream) }
            workbook.close()
            Toast.makeText(this, "Excel сохранён: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isProActive(): Boolean {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isPro = prefs.getBoolean("isPro", false)
        val expiry = prefs.getLong("proExpiryDate", 0)
        return isPro || expiry > System.currentTimeMillis()
    }
}
