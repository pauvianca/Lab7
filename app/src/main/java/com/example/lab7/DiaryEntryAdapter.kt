package com.example.lab7

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class DiaryEntryAdapter(
    context: Context,
    private val entries: MutableList<DiaryEntry>
) : ArrayAdapter<DiaryEntry>(context, 0, entries) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.diary_list_item, parent, false)

        val tvDate = view.findViewById<TextView>(R.id.tvItemDate)
        val tvText = view.findViewById<TextView>(R.id.tvItemText)

        val entry = entries[position]

        tvDate.text = entry.date
        tvText.text = entry.text

        return view
    }
}
