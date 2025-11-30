package com.example.lab7

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import androidx.appcompat.app.AlertDialog
import android.widget.EditText

import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Button

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class DisplayFragment : Fragment() {

    // Shared ViewModel used by all three fragments
    lateinit var viewModel: MyViewModel

    // UI elements
    private lateinit var listView: ListView
    private lateinit var spinnerFilter: Spinner
    private lateinit var btnExport: Button

    // Custom adapter for showing diary cards
    private lateinit var adapter: DiaryEntryAdapter

    // items = list currently shown in the ListView
    private val items = mutableListOf<DiaryEntry>()

    // allEntries = full list from the database (unfiltered master copy)
    private var allEntries: List<DiaryEntry> = emptyList()

    // Current filter mode (shows either all entries or only selected date)
    private var filterMode: FilterMode = FilterMode.ALL

    // Simple enum to describe the active filter
    enum class FilterMode {
        ALL,
        SELECTED_DATE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Get the shared ViewModel from the Activity
        viewModel = activity?.run {
            ViewModelProvider(this)[MyViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Inflate the layout for the Diary tab
        val view = inflater.inflate(R.layout.fragment_display, container, false)

        // Find views in the layout
        listView = view.findViewById(R.id.lvEntries)
        spinnerFilter = view.findViewById(R.id.spFilter)
        btnExport = view.findViewById(R.id.btnExport)

        // Use our custom adapter to show diary entries as cards
        adapter = DiaryEntryAdapter(requireContext(), items)
        listView.adapter = adapter

        // Set up the filter Spinner with two options
        val options = listOf("All entries", "Selected date only")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        // Load all entries from the database once when the fragment is created
        viewModel.loadEntriesFromDb(requireContext())

        // Observe the LiveData list of entries
        // Whenever the DB changes (insert/update/delete), this will be called.
        viewModel.entries.observe(viewLifecycleOwner) { list ->
            // Keep a master copy of all entries
            allEntries = list ?: emptyList()
            // Apply the current filter (ALL or SELECTED_DATE)
            applyFilter()
        }

        // When the user changes the filter option in the Spinner
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                v: View?,
                position: Int,
                id: Long
            ) {
                // Position 0 = "All entries", 1 = "Selected date only"
                filterMode = when (position) {
                    1 -> FilterMode.SELECTED_DATE
                    else -> FilterMode.ALL
                }
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action needed
            }
        }

        // Export button: write all diary entries to a text file
        btnExport.setOnClickListener {
            exportDiaryToTextFile()
        }

        // Short tap on a diary entry = edit the text
        listView.setOnItemClickListener { _, _, position, _ ->
            // Safety check: ignore invalid positions
            if (position < 0 || position >= items.size) return@setOnItemClickListener

            val entry = items[position]

            // Create an EditText pre-filled with the existing entry text
            val editText = EditText(requireContext()).apply {
                setText(entry.text)
                setSelection(text.length)   // move cursor to the end
                setPadding(32, 32, 32, 32)
            }

            // Show a simple AlertDialog with the EditText inside
            AlertDialog.Builder(requireContext())
                .setTitle("Edit entry")
                .setView(editText)
                .setPositiveButton("Save") { _, _ ->
                    val newText = editText.text.toString()
                    if (newText.isNotBlank()) {
                        // Update the entry in the database via the ViewModel
                        viewModel.updateEntryInDb(requireContext(), entry.id, newText)
                        Toast.makeText(requireContext(), "Entry updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Text cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Long press on a diary entry = show options (Share / Delete)
        listView.setOnItemLongClickListener { _, _, position, _ ->
            // Safety check: ignore invalid positions
            if (position < 0 || position >= items.size) return@setOnItemLongClickListener true

            val entry = items[position]

            // Dialog with options instead of deleting immediately
            val options = arrayOf("Share entry", "Delete entry", "Cancel")
            AlertDialog.Builder(requireContext())
                .setTitle("Entry options")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> { // Share
                            shareEntry(entry)
                        }
                        1 -> { // Delete
                            viewModel.deleteEntryFromDb(requireContext(), entry.id)
                            Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            dialog.dismiss()
                        }
                    }
                }
                .show()

            true
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // When we come back to this tab (e.g. after changing date on Tab 1),
        // re-apply the filter so that "Selected date only" uses the latest date.
        applyFilter()
    }

    // This function decides what should be shown in the ListView
    // based on the current filterMode and the latest selected date.
    private fun applyFilter() {
        // Clear the current displayed items
        items.clear()

        when (filterMode) {
            FilterMode.ALL -> {
                // Show all entries from the master list
                items.addAll(allEntries)
            }
            FilterMode.SELECTED_DATE -> {
                // Only show entries whose date matches the selected date in the ViewModel
                val selectedDate = viewModel.selectedDate.value
                if (!selectedDate.isNullOrBlank()) {
                    items.addAll(allEntries.filter { it.date == selectedDate })
                }
            }
        }

        // Tell the adapter that the data has changed so the UI will refresh
        adapter.notifyDataSetChanged()
    }

    // Share a single diary entry using an implicit Intent (ACTION_SEND)
    private fun shareEntry(entry: DiaryEntry) {
        // Format the text we want to share
        val shareText = "Date: ${entry.date}\n\n${entry.text}"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Diary entry on ${entry.date}")
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        // Use a chooser so the user can pick Messages, Gmail, Notes, etc.
        val chooser = Intent.createChooser(sendIntent, "Share diary entry via")
        startActivity(chooser)
    }

    //  Export all diary entries to a text file in the app's external files directory
    private fun exportDiaryToTextFile() {
        if (allEntries.isEmpty()) {
            Toast.makeText(requireContext(), "No entries to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Use the app's private external storage directory (no extra permission needed)
            val dir = requireContext().getExternalFilesDir(null)
            val file = File(dir, "diary_export.txt")

            val fos = FileOutputStream(file)
            val writer = OutputStreamWriter(fos)

            // Write all entries in a simple readable format
            allEntries.forEachIndexed { index, entry ->
                writer.write("Entry ${index + 1}\n")
                writer.write("Date: ${entry.date}\n")
                writer.write(entry.text)
                writer.write("\n\n------------------------------\n\n")
            }

            writer.flush()
            writer.close()
            fos.close()

            // Show the path so user (and the marker) know where the file went
            Toast.makeText(
                requireContext(),
                "Diary exported to:\n${file.absolutePath}",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Error exporting diary: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
