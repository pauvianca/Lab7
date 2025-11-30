package com.example.lab7

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar

/**
 * First tab: lets the user pick a date
 *
 * The selected date is stored in the shared MyViewModel so that
 * the Entry and Diary tabs can react to it.
 */

class DateFragment : Fragment() {

    lateinit var viewModel: MyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get the shared ViewModel from the Activity
        viewModel = activity?.run {
            ViewModelProvider(this)[MyViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Inflate UI for the data tab
        val view = inflater.inflate(R.layout.fragment_data, container, false)

        val tvSelectedDate = view.findViewById<TextView>(R.id.tvSelectedDate)
        val datePicker = view.findViewById<DatePicker>(R.id.datePicker)
        val btnSetDate = view.findViewById<Button>(R.id.btnSetDate)

        // When the ViewModel's selectedDate changes, update the label.
        viewModel.selectedDate.observe(viewLifecycleOwner, { date ->
            if (!date.isNullOrBlank()) {
                tvSelectedDate.text = "Selected date: $date"
            } else {
                tvSelectedDate.text = "Selected date: (none yet)"
            }
        })


        // Optional: set DatePicker to today's date
        val cal = Calendar.getInstance()
        datePicker.init(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
            null
        )

        // When the button is pressed, update the ViewModel with the chosen date.
        btnSetDate.setOnClickListener {
            val day = datePicker.dayOfMonth
            val month = datePicker.month + 1  // months are 0-based
            val year = datePicker.year
            val dateString = String.format("%02d-%02d-%04d", day, month, year)
            viewModel.setDate(dateString)
        }

        return view
    }
}
