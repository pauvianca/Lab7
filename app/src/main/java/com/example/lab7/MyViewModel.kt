package com.example.lab7

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.Context
import android.database.Cursor


data class DiaryEntry(
    val id: Long,
    val date: String,
    val text: String
)

class MyViewModel : ViewModel() {

    val selectedDate = MutableLiveData<String>()
    val currentText = MutableLiveData<String>()
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
