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
    var tag: String = "",
    var objectId: Long = 0
)

data class Reading(
    var id: Long = 0,
    var meterId: Long = 0,
    var value: Float = 0f,
    var date: String = ""
)

data class Goal(
    var id: Long = 0,
    var meterId: Long = 0,
    var targetAmount: Float = 0f,
    var currentAmount: Float = 0f,
    var month: String = "",
    var achieved: Boolean = false
)

data class ObjectData(
    var id: Long = 0,
    var name: String = "",
    var reminderDay: Int = 1,
    var isDefault: Boolean = false
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "meter.db", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE objects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                reminder_day INTEGER DEFAULT 1,
                is_default INTEGER DEFAULT 0
            )
        """)
        db.execSQL("INSERT INTO objects (name, is_default) VALUES ('Моя квартира', 1)")

        db.execSQL("""
            CREATE TABLE meters (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                initial_reading REAL DEFAULT 0,
                tariff REAL DEFAULT 0,
                tag TEXT DEFAULT '',
                object_id INTEGER NOT NULL,
                FOREIGN KEY(object_id) REFERENCES objects(id) ON DELETE CASCADE
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.execSQL("""
                CREATE TABLE objects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    reminder_day INTEGER DEFAULT 1,
                    is_default INTEGER DEFAULT 0
                )
            """)
            db.execSQL("INSERT INTO objects (name, is_default) VALUES ('Моя квартира', 1)")
            db.execSQL("ALTER TABLE meters ADD COLUMN object_id INTEGER DEFAULT 1")
        }
    }

    // Objects
    fun getAllObjects(): List<ObjectData> {
        val list = mutableListOf<ObjectData>()
        val db = readableDatabase
        val cursor = db.query("objects", null, null, null, null, null, "name ASC")
        while (cursor.moveToNext()) {
            list.add(
                ObjectData(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    reminderDay = cursor.getInt(cursor.getColumnIndexOrThrow("reminder_day")),
                    isDefault = cursor.getInt(cursor.getColumnIndexOrThrow("is_default")) == 1
                )
            )
        }
        cursor.close()
        return list
    }

    fun insertObject(obj: ObjectData): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", obj.name)
            put("reminder_day", obj.reminderDay)
            put("is_default", if (obj.isDefault) 1 else 0)
        }
        return db.insert("objects", null, values)
    }

    fun updateObject(obj: ObjectData) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", obj.name)
            put("reminder_day", obj.reminderDay)
            put("is_default", if (obj.isDefault) 1 else 0)
        }
        db.update("objects", values, "id = ?", arrayOf(obj.id.toString()))
    }

    fun deleteObject(id: Long) {
        val db = writableDatabase
        db.delete("objects", "id = ?", arrayOf(id.toString()))
    }

    // Meters
    fun insertMeter(meter: Meter): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", meter.name)
            put("type", meter.type)
            put("initial_reading", meter.initialReading)
            put("tariff", meter.tariff)
            put("tag", meter.tag)
            put("object_id", meter.objectId)
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
            put("object_id", meter.objectId)
        }
        db.update("meters", values, "id = ?", arrayOf(meter.id.toString()))
    }

    fun getAllMeters(objectId: Long? = null): List<Meter> {
        val meters = mutableListOf<Meter>()
        val db = readableDatabase
        val selection = if (objectId != null) "object_id = ?" else null
        val args = if (objectId != null) arrayOf(objectId.toString()) else null
        val cursor = db.query("meters", null, selection, args, null, null, "name ASC")
        while (cursor.moveToNext()) {
            meters.add(
                Meter(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                    initialReading = cursor.getFloat(cursor.getColumnIndexOrThrow("initial_reading")),
                    tariff = cursor.getFloat(cursor.getColumnIndexOrThrow("tariff")),
                    tag = cursor.getString(cursor.getColumnIndexOrThrow("tag")) ?: "",
                    objectId = cursor.getLong(cursor.getColumnIndexOrThrow("object_id"))
                )
            )
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
                tag = cursor.getString(cursor.getColumnIndexOrThrow("tag")) ?: "",
                objectId = cursor.getLong(cursor.getColumnIndexOrThrow("object_id"))
            )
        } else {
            null
        }.also { cursor.close() }
    }

    fun deleteMeter(id: Long) {
        val db = writableDatabase
        db.delete("meters", "id = ?", arrayOf(id.toString()))
    }

    // Readings
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
        val cursor = db.query(
            "readings", null, "meter_id = ?", arrayOf(meterId.toString()), null, null, "date ASC"
        )
        while (cursor.moveToNext()) {
            readings.add(
                Reading(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    meterId = cursor.getLong(cursor.getColumnIndexOrThrow("meter_id")),
                    value = cursor.getFloat(cursor.getColumnIndexOrThrow("value")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                )
            )
        }
        cursor.close()
        return readings
    }

    fun getAllReadings(): List<Reading> {
        val readings = mutableListOf<Reading>()
        val db = readableDatabase
        val cursor = db.query("readings", null, null, null, null, null, "meter_id, date ASC")
        while (cursor.moveToNext()) {
            readings.add(
                Reading(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    meterId = cursor.getLong(cursor.getColumnIndexOrThrow("meter_id")),
                    value = cursor.getFloat(cursor.getColumnIndexOrThrow("value")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                )
            )
        }
        cursor.close()
        return readings
    }

    fun deleteReading(id: Long) {
        val db = writableDatabase
        db.delete("readings", "id = ?", arrayOf(id.toString()))
    }

    // Goals
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
            goals.add(
                Goal(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    meterId = cursor.getLong(cursor.getColumnIndexOrThrow("meter_id")),
                    targetAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("target_amount")),
                    currentAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("current_amount")),
                    month = cursor.getString(cursor.getColumnIndexOrThrow("month")),
                    achieved = cursor.getInt(cursor.getColumnIndexOrThrow("achieved")) == 1
                )
            )
        }
        cursor.close()
        return goals
    }

    fun deleteGoal(id: Long) {
        val db = writableDatabase
        db.delete("goals", "id = ?", arrayOf(id.toString()))
    }
}
