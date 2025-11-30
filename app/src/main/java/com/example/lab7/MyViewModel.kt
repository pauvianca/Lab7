package com.example.lab7

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.Context
import android.database.Cursor

/**
 * Shared ViewModel used by all three fragments.
 *
 * Responsibility:
 * - hold the currently selected date
 * - hold the current entry text (if needed)
 * - expose a LiveData list of DiaryEntry objects
 * - know how to load/save/update/delete entries in the SQLite DB
 */

data class DiaryEntry(
    val id: Long,
    val date: String,
    val text: String
)

class MyViewModel : ViewModel() {

    // Currently selected date from the Date tab.
    val selectedDate = MutableLiveData<String>()

    // Optional: currently typed text, if user wants to share it
    val currentText = MutableLiveData<String>()

    // List list of all entries from the database.
    //DisplayFragment observes this to update the ListView.
    val entries = MutableLiveData<MutableList<DiaryEntry>>()

    init {
        selectedDate.value = ""
        currentText.value = ""
        entries.value = mutableListOf()
    }

    fun setDate(date: String) {
        selectedDate.value = date
    }

    fun setCurrentText(text: String) {
        currentText.value = text
    }

    /**
     * Legacy in-memory add (not used now because SQLite is used on this)
     * but kept to show how this could support a non-persistent version
     */

    fun addEntry() {
        val date = selectedDate.value ?: ""
        val text = currentText.value ?: ""

        // do nothing if incomplete
        if (date.isBlank() || text.isBlank()) return

        val list = entries.value ?: mutableListOf()
        val newId = if (list.isEmpty()) 1L else (list.maxOf { it.id } + 1)
        list.add(DiaryEntry(newId, date, text))

        entries.value = list
        currentText.value = ""
    }

    /**
     * Load all diary entries from the SQLite database
     * and push them into the LiveData List
     */
    fun loadEntriesFromDb(context: Context) {
        val adapter = DiaryDatabaseAdapter(context).open()
        val cursor: Cursor = adapter.getAllEntries()

        val list = mutableListOf<DiaryEntry>()

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(DiaryDatabaseHelper.COL_ID)
            val dateIndex = cursor.getColumnIndex(DiaryDatabaseHelper.COL_DATE)
            val textIndex = cursor.getColumnIndex(DiaryDatabaseHelper.COL_TEXT)

            do {
                val id = cursor.getLong(idIndex)
                val date = cursor.getString(dateIndex)
                val text = cursor.getString(textIndex)
                list.add(DiaryEntry(id, date, text))
            } while (cursor.moveToNext())

        }

        cursor.close()
        adapter.close()

        entries.value = list
    }

    /**
     * Save the current entry (selectedDate + currentText)
     * into the SQLite database, then refresh the list.
     */

    fun saveCurrentEntryToDb(context: Context) {
        val date = selectedDate.value ?: ""
        val text = currentText.value ?: ""
        if (date.isBlank() || text.isBlank()) return

        val adapter = DiaryDatabaseAdapter(context).open()
        adapter.insertEntry(date, text)
        adapter.close()

        // refresh list from DB so UI + LiveData are in sync
        loadEntriesFromDb(context)
        currentText.value = ""
    }

    /**
     * Delete an entry and reload from DB 
     */

    fun updateEntryInDb(context: Context, id: Long, newText: String) {
        if (newText.isBlank()) return

        val adapter = DiaryDatabaseAdapter(context).open()
        adapter.updateEntry(id, newText)
        adapter.close()

        // reload from DB to keep LiveData in sync
        loadEntriesFromDb(context)
    }


    fun deleteEntryFromDb(context: Context, id: Long) {
        val adapter = DiaryDatabaseAdapter(context).open()
        adapter.deleteEntry(id)
        adapter.close()

        loadEntriesFromDb(context)
    }}
