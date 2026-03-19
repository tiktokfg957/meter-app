package com.example.meterreader

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Meter(
    var id: Long = 0,
    var name: String = "",
    var type: String = "",
    var initialReading: Float = 0f,
    var tariff: Float = 0f,
    var tag: String = ""  // новое поле для тега
)

data class Reading(
    var id: Long = 0,
    var meterId: Long = 0,
    var value: Float = 0f,
    var date: String = ""
)

data class Goal(
    var id: Long = 0,
    var meterId: Long = 0,      // для какого счётчика (0 – общая цель на все)
    var targetAmount: Float = 0f, // целевая сумма экономии (в рублях)
    var currentAmount: Float = 0f, // текущая накопленная экономия
    var month: String = "",       // месяц в формате "yyyy-MM"
    var achieved: Boolean = false
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "meter.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE meters (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                initial_reading REAL DEFAULT 0,
                tariff REAL DEFAULT 0,
                tag TEXT DEFAULT ''
            )
        """)

        db.execSQL("""
            CREATE TABLE readings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                meter_id INTEGER NOT NULL,
                value REAL NOT NULL,
                date TEXT NOT NULL,
                FOREIGN KEY(meter_id) REFERENCES meters(id) ON DELETE CASCADE
            )
        """)

        db.execSQL("""
            CREATE TABLE goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                meter_id INTEGER,
                target_amount REAL NOT NULL,
                current_amount REAL DEFAULT 0,
                month TEXT NOT NULL,
                achieved INTEGER DEFAULT 0,
                FOREIGN KEY(meter_id) REFERENCES meters(id) ON DELETE CASCADE
            )
        """)

        insertDefaultCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // версия 2 добавила tariff
            db.execSQL("ALTER TABLE meters ADD COLUMN tariff REAL DEFAULT 0")
        }
        if (oldVersion < 3) {
            // версия 3 добавила tag и таблицу goals
            db.execSQL("ALTER TABLE meters ADD COLUMN tag TEXT DEFAULT ''")
            db.execSQL("""
                CREATE TABLE goals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    meter_id INTEGER,
                    target_amount REAL NOT NULL,
                    current_amount REAL DEFAULT 0,
                    month TEXT NOT NULL,
                    achieved INTEGER DEFAULT 0,
                    FOREIGN KEY(meter_id) REFERENCES meters(id) ON DELETE CASCADE
                )
            """)
        }
    }

    private fun insertDefaultCategories(db: SQLiteDatabase) {
        val expenseCategories = listOf("Еда", "Транспорт", "Развлечения", "Здоровье", "Одежда", "Связь", "Коммунальные услуги", "Образование", "Подарки", "Прочее")
        val incomeCategories = listOf("Зарплата", "Подработка", "Подарки", "Инвестиции", "Прочее")

        for (name in expenseCategories) {
            val values = ContentValues().apply {
                put("name", name)
                put("type", "expense")
                put("icon", getEmojiForCategory(name))
                put("is_system", 1)
            }
            db.insert("categories", null, values)
        }
        for (name in incomeCategories) {
            val values = ContentValues().apply {
                put("name", name)
                put("type", "income")
                put("icon", getEmojiForCategory(name))
                put("is_system", 1)
            }
            db.insert("categories", null, values)
        }
    }

    private fun getEmojiForCategory(name: String): String {
        return when (name) {
            "Еда" -> "🍔"
            "Транспорт" -> "🚗"
            "Развлечения" -> "🎬"
            "Здоровье" -> "💊"
            "Одежда" -> "👕"
            "Связь" -> "📱"
            "Коммунальные услуги" -> "💡"
            "Образование" -> "📚"
            "Подарки" -> "🎁"
            "Прочее" -> "📌"
            "Зарплата" -> "💰"
            "Подработка" -> "💼"
            "Инвестиции" -> "📈"
            else -> "📌"
        }
    }

    // Методы для категорий (оставлены без изменений, но в этом классе они не нужны для ЖКХ – можно удалить)
    // ... (здесь идут методы getAllCategories, insertCategory и т.д., но для ЖКХ они не нужны, поэтому я их опускаю для краткости, но в реальном файле они могут быть. Если они есть, их можно оставить.)

    // Методы для метров
    fun insertMeter(meter: Meter): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", meter.name)
            put("type", meter.type)
            put("initial_reading", meter.initialReading)
            put("tariff", meter.tariff)
            put("tag", meter.tag)
        }
        return db.insert("meters", null, values)
    }

    fun updateMeter(meter: Meter) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", meter.name)
            put("type", meter.type)
            put("initial_reading", meter.initialReading)
            put("tariff", meter.tariff)
            put("tag", meter.tag)
        }
        db.update("meters", values, "id = ?", arrayOf(meter.id.toString()))
    }

    fun getAllMeters(): List<Meter> {
        val meters = mutableListOf<Meter>()
        val db = readableDatabase
        val cursor = db.query("meters", null, null, null, null, null, "name ASC")
        while (cursor.moveToNext()) {
            meters.add(Meter(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                initialReading = cursor.getFloat(cursor.getColumnIndexOrThrow("initial_reading")),
                tariff = cursor.getFloat(cursor.getColumnIndexOrThrow("tariff")),
                tag = cursor.getString(cursor.getColumnIndexOrThrow("tag")) ?: ""
            ))
        }
        cursor.close()
        return meters
    }

    fun getMeter(id: Long): Meter? {
        val db = readableDatabase
        val cursor = db.query("meters", null, "id = ?", arrayOf(id.toString()), null, null, null)
        return if (cursor.moveToFirst()) {
            Meter(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                initialReading = cursor.getFloat(cursor.getColumnIndexOrThrow("initial_reading")),
                tariff = cursor.getFloat(cursor.getColumnIndexOrThrow("tariff")),
                tag = cursor.getString(cursor.getColumnIndexOrThrow("tag")) ?: ""
            )
        } else {
            null
        }.also { cursor.close() }
    }

    fun deleteMeter(id: Long) {
        val db = writableDatabase
        db.delete("meters", "id = ?", arrayOf(id.toString()))
    }

    // Методы для показаний (без изменений, но оставим)
    fun insertReading(reading: Reading): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("meter_id", reading.meterId)
            put("value", reading.value)
            put("date", reading.date)
        }
        return db.insert("readings", null, values)
    }

    fun updateReading(reading: Reading) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("meter_id", reading.meterId)
            put("value", reading.value)
            put("date", reading.date)
        }
        db.update("readings", values, "id = ?", arrayOf(reading.id.toString()))
    }

    fun getReadingsForMeter(meterId: Long): List<Reading> {
        val readings = mutableListOf<Reading>()
        val db = readableDatabase
        val cursor = db.query("readings", null, "meter_id = ?", arrayOf(meterId.toString()), null, null, "date ASC")
        while (cursor.moveToNext()) {
            readings.add(Reading(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                meterId = cursor.getLong(cursor.getColumnIndexOrThrow("meter_id")),
                value = cursor.getFloat(cursor.getColumnIndexOrThrow("value")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            ))
        }
        cursor.close()
        return readings
    }

    fun getAllReadings(): List<Reading> {
        val readings = mutableListOf<Reading>()
        val db = readableDatabase
        val cursor = db.query("readings", null, null, null, null, null, "meter_id, date ASC")
        while (cursor.moveToNext()) {
            readings.add(Reading(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                meterId = cursor.getLong(cursor.getColumnIndexOrThrow("meter_id")),
                value = cursor.getFloat(cursor.getColumnIndexOrThrow("value")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            ))
        }
        cursor.close()
        return readings
    }

    fun deleteReading(id: Long) {
        val db = writableDatabase
        db.delete("readings", "id = ?", arrayOf(id.toString()))
    }

    // Методы для целей
    fun insertGoal(goal: Goal): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("meter_id", goal.meterId)
            put("target_amount", goal.targetAmount)
            put("current_amount", goal.currentAmount)
            put("month", goal.month)
            put("achieved", if (goal.achieved) 1 else 0)
        }
        return db.insert("goals", null, values)
    }

    fun updateGoal(goal: Goal) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("meter_id", goal.meterId)
            put("target_amount", goal.targetAmount)
            put("current_amount", goal.currentAmount)
            put("month", goal.month)
            put("achieved", if (goal.achieved) 1 else 0)
        }
        db.update("goals", values, "id = ?", arrayOf(goal.id.toString()))
    }

    fun getGoalsForMonth(month: String, meterId: Long? = null): List<Goal> {
        val goals = mutableListOf<Goal>()
        val db = readableDatabase
        val selection = if (meterId == null) "month = ?" else "month = ? AND meter_id = ?"
        val args = if (meterId == null) arrayOf(month) else arrayOf(month, meterId.toString())
        val cursor = db.query("goals", null, selection, args, null, null, null)
        while (cursor.moveToNext()) {
            goals.add(Goal(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                meterId = cursor.getLong(cursor.getColumnIndexOrThrow("meter_id")),
                targetAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("target_amount")),
                currentAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("current_amount")),
                month = cursor.getString(cursor.getColumnIndexOrThrow("month")),
                achieved = cursor.getInt(cursor.getColumnIndexOrThrow("achieved")) == 1
            ))
        }
        cursor.close()
        return goals
    }

    fun deleteGoal(id: Long) {
        val db = writableDatabase
        db.delete("goals", "id = ?", arrayOf(id.toString()))
    }
}
