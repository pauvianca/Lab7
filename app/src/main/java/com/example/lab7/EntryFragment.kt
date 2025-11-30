package com.example.lab7

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class EntryFragment : Fragment() {

    // Shared ViewModel used to communicate between fragments
    lateinit var viewModel: MyViewModel

    // Keys for SharedPreferences (for saving the draft text)
    private val PREFS_NAME = "DiaryPrefs"
    private val KEY_DRAFT_TEXT = "draft_text"

    private lateinit var tvEntryDate: TextView
    private lateinit var etDiaryText: EditText
    private lateinit var btnClear: Button
    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Get the shared ViewModel from the Activity
        viewModel = activity?.run {
            ViewModelProvider(this)[MyViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Inflate the layout for the Entry tab
        val view = inflater.inflate(R.layout.fragment_entry, container, false)

        // Find views
        tvEntryDate = view.findViewById(R.id.tvEntryDate)
        etDiaryText = view.findViewById(R.id.etDiaryText)
        btnClear = view.findViewById(R.id.btnClear)
        btnSave = view.findViewById(R.id.btnSave)

        // Show the currently selected date from the ViewModel
        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            if (!date.isNullOrBlank()) {
                tvEntryDate.text = "Date: $date"
            } else {
                tvEntryDate.text = "Date: (please select on first tab)"
            }
        }

        // Load any saved draft text from SharedPreferences
        loadDraft()

        // Clear button simply clears the EditText and the draft
        btnClear.setOnClickListener {
            etDiaryText.setText("")
            viewModel.setCurrentText("")
            clearDraft()
        }

        // Save button inserts the entry into the DB using the ViewModel
        btnSave.setOnClickListener { v ->
            val date = viewModel.selectedDate.value ?: ""
            val text = etDiaryText.text.toString()

            // Basic validation: user must pick a date and write something
            if (date.isBlank()) {
                Toast.makeText(v.context, "Please select a date first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (text.isBlank()) {
                Toast.makeText(v.context, "Please write something", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Store the text in ViewModel and save to DB
            viewModel.setCurrentText(text)
            viewModel.saveCurrentEntryToDb(requireContext())

            // Clear text box and draft after successful save
            etDiaryText.setText("")
            clearDraft()

            Toast.makeText(v.context, "Entry saved", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        // When leaving this fragment (switch tab / app goes to background),
        // save whatever is currently written as a draft.
        saveDraft()
    }

    // Save the current diary text into SharedPreferences
    private fun saveDraft() {
        val text = etDiaryText.text.toString()
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        if (text.isNotBlank()) {
            editor.putString(KEY_DRAFT_TEXT, text)
        } else {
            // If text is empty, remove any previous draft
            editor.remove(KEY_DRAFT_TEXT)
        }
        editor.apply()
    }

    // Load the draft text from SharedPreferences (if there is one)
    private fun loadDraft() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val draft = prefs.getString(KEY_DRAFT_TEXT, null)

        if (!draft.isNullOrBlank()) {
            etDiaryText.setText(draft)
            // Optionally move cursor to end
            etDiaryText.setSelection(draft.length)
        }
    }

    // Clear the saved draft (used after a successful save or clear)
    private fun clearDraft() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_DRAFT_TEXT).apply()
    }
}
