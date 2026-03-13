package com.example.meterreader

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Meter(
    var id: Long = 0,
    var name: String = "",
    var type: String = "",
    var initialReading: Float = 0f
)

data class Reading(
    var id: Long = 0,
    var meterId: Long = 0,
    var value: Float = 0f,
    var date: String = ""
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "meter.db", null, 1) {
    
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE meters (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                initial_reading REAL DEFAULT 0
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
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS readings")
        db.execSQL("DROP TABLE IF EXISTS meters")
        onCreate(db)
    }
    
    fun insertMeter(meter: Meter): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", meter.name)
            put("type", meter.type)
            put("initial_reading", meter.initialReading)
        }
        return db.insert("meters", null, values)
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
                initialReading = cursor.getFloat(cursor.getColumnIndexOrThrow("initial_reading"))
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
                initialReading = cursor.getFloat(cursor.getColumnIndexOrThrow("initial_reading"))
            )
        } else {
            null
        }.also { cursor.close() }
    }
    
    fun deleteMeter(id: Long) {
        val db = writableDatabase
        db.delete("meters", "id = ?", arrayOf(id.toString()))
    }
    
    fun insertReading(reading: Reading): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("meter_id", reading.meterId)
            put("value", reading.value)
            put("date", reading.date)
        }
        return db.insert("readings", null, values)
    }
    
    fun getReadingsForMeter(meterId: Long): List<Reading> {
        val readings = mutableListOf<Reading>()
        val db = readableDatabase
        val cursor = db.query(
            "readings", 
            null, 
            "meter_id = ?", 
            arrayOf(meterId.toString()), 
            null, null, 
            "date ASC"
        )
        
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
}
