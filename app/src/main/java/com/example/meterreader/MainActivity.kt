package com.example.meterreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yandex.mobile.ads.AdRequest
import com.yandex.mobile.ads.AdRequestError
import com.yandex.mobile.ads.AdSize
import com.yandex.mobile.ads.banner.BannerAdView
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
    private lateinit var dbHelper: DatabaseHelper

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

        recyclerView.layoutManager = LinearLayoutManager(this)

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

        // Кнопка VK-паблика
        val btnVK = findViewById<Button>(R.id.btnVK)
        btnVK.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/club236967018"))
            startActivity(intent)
        }

        // PRO-бейдж
        val tvProBadge = findViewById<TextView>(R.id.tvProBadge)
        tvProBadge.setOnClickListener {
            val dialog = ProDialogFragment()
            dialog.show(supportFragmentManager, "pro_dialog")
        }

        // Рекламный баннер
        val banner = BannerAdView(this).apply {
            setAdUnitId("R-M-18995591-1")
            setAdSize(AdSize.fixedSize(320, 50))
            setAdListener(object : BannerAdView.AdListener {
                override fun onAdLoaded() {
                    findViewById<FrameLayout>(R.id.bannerContainer).visibility = View.VISIBLE
                }
                override fun onAdFailedToLoad(error: AdRequestError) {
                    findViewById<FrameLayout>(R.id.bannerContainer).visibility = View.GONE
                }
                override fun onAdClicked() {}
            })
            loadAd(AdRequest.Builder().build())
        }
        val bannerContainer = findViewById<FrameLayout>(R.id.bannerContainer)
        bannerContainer.addView(banner)

        loadMeters()
    }

    override fun onResume() {
        super.onResume()
        loadMeters()
    }

    override fun onDestroy() {
        super.onDestroy()
        val banner = findViewById<FrameLayout>(R.id.bannerContainer)?.getChildAt(0) as? BannerAdView
        banner?.destroy()
    }

    private fun loadMeters() {
        val meters = dbHelper.getAllMeters()
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
        val meters = dbHelper.getAllMeters()
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

    private fun exportToCSV() {
        try {
            val meters = dbHelper.getAllMeters()
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
                            val diff = if (i > 0) {
                                reading.value - meterReadings[i-1].value
                            } else {
                                reading.value - meter.initialReading
                            }
                            val cost = diff * meter.tariff

                            csvWriter.writeNext(arrayOf(
                                meter.id.toString(),
                                meter.name,
                                meter.type,
                                reading.date,
                                reading.value.toString(),
                                diff.toString(),
                                "%.2f".format(cost)
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
            val meters = dbHelper.getAllMeters()
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
                    val diff = if (i > 0) {
                        reading.value - meterReadings[i-1].value
                    } else {
                        reading.value - meter.initialReading
                    }
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

            file.outputStream().use { outputStream ->
                workbook.write(outputStream)
            }
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
