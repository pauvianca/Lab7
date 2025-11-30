package com.example.lab7

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLiteOpenHelper subclass for creating and upgrading the diary database.
 *
 * This class only knows about the TABLE NAME and columns - it does not
 * contain any UI logic.
 */


class DiaryDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        // Called once when the DB is first created.
        // Create a simple table with an auto-increment ID, a date, and text.
        // date (stored as text dd-MM-yyyy )
        db.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                    "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COL_DATE TEXT NOT NULL, " +
                    "$COL_TEXT TEXT NOT NULL" +
                    ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        // For this, the upgrade is simple strategy: drop the table and recreate it
        // In a real app, it should migrate data here instead.
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        // Basic DB constants: easy to reuse from other classes.
        const val DATABASE_NAME = "diaryDB"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "diaryEntries"
        const val COL_ID = "id"
        const val COL_DATE = "date"
        const val COL_TEXT = "text"
    }
}
