package com.example.lab7

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class DiaryDatabaseAdapter(context: Context) {

    private val dbHelper = DiaryDatabaseHelper(context)
    private var db: SQLiteDatabase? = null

    fun open(): DiaryDatabaseAdapter {
        db = dbHelper.writableDatabase
        return this
    }

    fun close() {
        dbHelper.close()
    }

    fun insertEntry(date: String, text: String): Long {
        val values = ContentValues().apply {
            put(DiaryDatabaseHelper.COL_DATE, date)
            put(DiaryDatabaseHelper.COL_TEXT, text)
        }
        return db!!.insert(DiaryDatabaseHelper.TABLE_NAME, null, values)
    }

    fun getAllEntries(): Cursor {
        return db!!.query(
            DiaryDatabaseHelper.TABLE_NAME,
            arrayOf(
                DiaryDatabaseHelper.COL_ID,
                DiaryDatabaseHelper.COL_DATE,
                DiaryDatabaseHelper.COL_TEXT
            ),
            null,
            null,
            null,
            null,
            "${DiaryDatabaseHelper.COL_ID} DESC"   // newest first
        )
    }

    fun deleteEntry(id: Long): Int {
        return db!!.delete(
            DiaryDatabaseHelper.TABLE_NAME,
            "${DiaryDatabaseHelper.COL_ID}=?",
            arrayOf(id.toString())
        )
    }

    fun updateEntry(id: Long, newText: String): Int {
        val values = ContentValues().apply {
            put(DiaryDatabaseHelper.COL_TEXT, newText)
        }
        return db!!.update(
            DiaryDatabaseHelper.TABLE_NAME,
            values,
            "${DiaryDatabaseHelper.COL_ID} = ?",
            arrayOf(id.toString())
        )
    }

}
