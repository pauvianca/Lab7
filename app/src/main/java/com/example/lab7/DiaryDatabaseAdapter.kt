package com.example.lab7

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

/**
 * Small helper / wrapper around DiaryDatabaseHelper
 *
 * Responsibilites:
 * - open/close the SQLiteDatabase
 * - provide simple functions to insert / query / update / delete entries
 *
 * All higher-level logic stays in MyViewModel and the Fragments
 */

class DiaryDatabaseAdapter(context: Context) {

    private val dbHelper = DiaryDatabaseHelper(context)
    private var db: SQLiteDatabase? = null

    /**
     * Open the database for read/write operations.
     * Returns this so we can chain calls:
     *      val adapter = DiaryDatabaseAdapter(context).open()
     */
    fun open(): DiaryDatabaseAdapter {
        db = dbHelper.writableDatabase
        return this
    }

    /**
     * Close the helper and free DB resources.
     */
    fun close() {
        dbHelper.close()
    }

    /**
     * Insert a new diary entry (Data + text )
     * Returns the ID of the newly inserted row.
     */

    fun insertEntry(date: String, text: String): Long {
        val values = ContentValues().apply {
            put(DiaryDatabaseHelper.COL_DATE, date)
            put(DiaryDatabaseHelper.COL_TEXT, text)
        }
        return db!!.insert(DiaryDatabaseHelper.TABLE_NAME, null, values)
    }

    /**
     * Return a Cursor over all diary entries, newest first.
     * The ViewModel converts this Cursor into a list of DiaryEntry objects.
     */
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

    /**
     * Delete a single diary entry by id.
     * Returns the number of rows removed ( 0 or 1)
     */

    fun deleteEntry(id: Long): Int {
        return db!!.delete(
            DiaryDatabaseHelper.TABLE_NAME,
            "${DiaryDatabaseHelper.COL_ID}=?",
            arrayOf(id.toString())
        )
    }

    /**
     * Update the diary text for a given entry id.
     * Returns the number of rows affected ( 0 or 1)
     */

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
