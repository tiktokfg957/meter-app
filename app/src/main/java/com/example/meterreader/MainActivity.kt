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
    private lateinit var btnSupport: Button   // новая кнопка
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
        btnSupport = findViewById(R.id.btnSupport)   // инициализация
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

        // Кнопка поддержки
        btnSupport.setOnClickListener {
            startActivity(Intent(this, SupportChatActivity::class.java))
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

        checkOnboarding()
        loadMeters()
    }

    override fun onResume() {
        super.onResume()
        loadMeters()
        loadTip()
    }

    // ... остальные методы (loadMeters, updateTotalForCurrentMonth, loadTip, checkOnboarding, exportToCSV, exportToExcel, isProActive) остаются без изменений ...
}
