package com.example.lab7

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

    lateinit var viewModel: MyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run {
            ViewModelProvider(this)[MyViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        val view = inflater.inflate(R.layout.fragment_entry, container, false)

        val tvEntryDate = view.findViewById<TextView>(R.id.tvEntryDate)
        val etDiaryText = view.findViewById<EditText>(R.id.etDiaryText)
        val btnClear = view.findViewById<Button>(R.id.btnClear)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // observe selected date
        viewModel.selectedDate.observe(viewLifecycleOwner, { date ->
            if (!date.isNullOrBlank()) {
                tvEntryDate.text = "Date: $date"
            } else {
                tvEntryDate.text = "Date: (please select on first tab)"
            }
        })

        btnClear.setOnClickListener {
            etDiaryText.setText("")
            viewModel.setCurrentText("")
        }

        btnSave.setOnClickListener { v ->
            val date = viewModel.selectedDate.value ?: ""
            val text = etDiaryText.text.toString()

            if (date.isBlank()) {
                Toast.makeText(v.context, "Please select a date first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (text.isBlank()) {
                Toast.makeText(v.context, "Please write something", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.setCurrentText(text)
            viewModel.saveCurrentEntryToDb(requireContext())


            etDiaryText.setText("")

            Toast.makeText(v.context, "Entry saved", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
