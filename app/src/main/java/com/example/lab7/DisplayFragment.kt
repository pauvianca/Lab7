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

import android.text.Editable
import android.text.TextWatcher

/**
 * Third tab: displays the diary entries saved in the SQLite database.
 *
 * Responsibilities:
 *  - Load and observe entries from MyViewModel (which manages the DB)
 *  - Provide two filters:
 *        1) All entries
 *        2) Selected date only (matches the date chosen in Tab 1)
 *  - Provide a text search that filters diary content in real time
 *  - Support editing entries on tap
 *  - Support sharing / deleting entries on long-press
 *  - Export all entries to a .txt file stored in external app storage
 *
 * This fragment does not directly interact with the database.
 * All DB operations (insert/update/delete/load) are handled by MyViewModel.
 */
class DisplayFragment : Fragment() {

    // Shared ViewModel used by all three fragments
    lateinit var viewModel: MyViewModel

    // UI elements
    private lateinit var listView: ListView
    private lateinit var spinnerFilter: Spinner
    private lateinit var btnExport: Button

    // Custom adapter for showing diary cards
    private lateinit var adapter: DiaryEntryAdapter

    // items = list currently shown in the ListView after filtering/search
    private val items = mutableListOf<DiaryEntry>()

    // allEntries = full list from database (master copy that is never modified)
    private var allEntries: List<DiaryEntry> = emptyList()

    // Current filter mode (ALL or SELECTED_DATE)
    private var filterMode: FilterMode = FilterMode.ALL

    // Search box for filtering by text
    private lateinit var etSearch: EditText

    // The current search query typed by the user
    private var currentSearchQuery: String = ""

    // Two filtering modes used by the Spinner
    enum class FilterMode {
        ALL,
        SELECTED_DATE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Get the shared ViewModel from the parent Activity
        viewModel = activity?.run {
            ViewModelProvider(this)[MyViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Inflate the layout for the Diary tab
        val view = inflater.inflate(R.layout.fragment_display, container, false)

        // Find UI components in the layout
        listView = view.findViewById(R.id.lvEntries)
        spinnerFilter = view.findViewById(R.id.spFilter)
        btnExport = view.findViewById(R.id.btnExport)
        etSearch = view.findViewById(R.id.etSearch)

        // Initialise the adapter that displays the filtered list
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

        /**
         * SEARCH BOX:
         * Whenever the user types in the search bar, update the current query
         * and re-apply filtering to the master list (allEntries).
         */
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update the current search query as the user types
                currentSearchQuery = s?.toString() ?: ""
                applyFilter()
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })

        // Load all entries from the DB once when this fragment is created
        viewModel.loadEntriesFromDb(requireContext())

        /**
         * Observe changes to the LiveData list of entries.
         * This triggers whenever an entry is added/edited/deleted in the DB.
         */
        viewModel.entries.observe(viewLifecycleOwner) { list ->
            // Store the full list from DB (master copy)
            allEntries = list ?: emptyList()
            applyFilter()
        }

        /**
         * FILTER SPINNER:
         * Switch between showing all entries or only those matching the selected date.
         */
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                v: View?,
                position: Int,
                id: Long
            ) {
                filterMode = when (position) {
                    1 -> FilterMode.SELECTED_DATE   // "Selected date only"
                    else -> FilterMode.ALL          // "All entries"
                }
                applyFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Export all diary entries to a text file
        btnExport.setOnClickListener {
            exportDiaryToTextFile()
        }

        /**
         * SHORT TAP:
         * Edit an entry using an AlertDialog with an EditText.
         */
        listView.setOnItemClickListener { _, _, position, _ ->
            if (position < 0 || position >= items.size) return@setOnItemClickListener
            val entry = items[position]

            // Create a text box pre-filled with the existing diary text
            val editText = EditText(requireContext()).apply {
                setText(entry.text)
                setSelection(text.length)
                setPadding(32, 32, 32, 32)
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Edit entry")
                .setView(editText)
                .setPositiveButton("Save") { _, _ ->
                    val newText = editText.text.toString()
                    if (newText.isNotBlank()) {
                        // Update in database using ViewModel
                        viewModel.updateEntryInDb(requireContext(), entry.id, newText)
                        Toast.makeText(requireContext(), "Entry updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Text cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        /**
         * LONG PRESS:
         * Show a dialog with options:
         *  - Share entry (implicit ACTION_SEND intent)
         *  - Delete entry
         */
        listView.setOnItemLongClickListener { _, _, position, _ ->
            if (position < 0 || position >= items.size) return@setOnItemLongClickListener true
            val entry = items[position]

            val options = arrayOf("Share entry", "Delete entry", "Cancel")
            AlertDialog.Builder(requireContext())
                .setTitle("Entry options")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> shareEntry(entry)
                        1 -> {
                            viewModel.deleteEntryFromDb(requireContext(), entry.id)
                            Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show()
                        }
                        else -> dialog.dismiss()
                    }
                }
                .show()

            true
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh filter when returning to this tab (e.g., date changed on Tab 1)
        applyFilter()
    }

    /**
     * Apply the currently selected date filter and search query.
     * Steps:
     *  1) Filter by ALL vs SELECTED_DATE
     *  2) Filter by search text (case-insensitive)
     *  3) Refresh adapter with the filtered list
     */
    private fun applyFilter() {
        items.clear()

        // --- Step 1: date filter ---
        val baseList = when (filterMode) {
            FilterMode.ALL -> allEntries
            FilterMode.SELECTED_DATE -> {
                val selectedDate = viewModel.selectedDate.value
                if (selectedDate.isNullOrBlank()) {
                    emptyList()
                } else {
                    allEntries.filter { it.date == selectedDate }
                }
            }
        }

        // --- Step 2: search filter ---
        val query = currentSearchQuery.trim().lowercase()
        val finalList =
            if (query.isEmpty()) baseList
            else baseList.filter { entry ->
                entry.text.lowercase().contains(query) ||
                        entry.date.lowercase().contains(query)
            }

        // --- Step 3: update UI ---
        items.addAll(finalList)
        adapter.notifyDataSetChanged()
    }

    /**
     * Share one diary entry using an implicit intent.
     * Allows user to pick Gmail, Messages, Notes, etc.
     */
    private fun shareEntry(entry: DiaryEntry) {
        val shareText = "Date: ${entry.date}\n\n${entry.text}"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Diary entry on ${entry.date}")
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val chooser = Intent.createChooser(sendIntent, "Share diary entry via")
        startActivity(chooser)
    }

    /**
     * Export all entries to a text file (diary_export.txt)
     * stored inside the app's private external files directory.
     * No WRITE permission is required for this location.
     */
    private fun exportDiaryToTextFile() {
        if (allEntries.isEmpty()) {
            Toast.makeText(requireContext(), "No entries to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dir = requireContext().getExternalFilesDir(null)
            val file = File(dir, "diary_export.txt")

            val fos = FileOutputStream(file)
            val writer = OutputStreamWriter(fos)

            // Write all entries in a readable format
            allEntries.forEachIndexed { index, entry ->
                writer.write("Entry ${index + 1}\n")
                writer.write("Date: ${entry.date}\n")
                writer.write(entry.text)
                writer.write("\n\n------------------------------\n\n")
            }

            writer.flush()
            writer.close()
            fos.close()

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
