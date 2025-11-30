package com.example.lab7

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * Custom ArrayAdapter for displaying diary entries in the ListView.
 *
 * Why ArrayAdapter?
 *- It provides a lightweight way to bind a list of objects to a ListView.
 *- We only need to override getView(), since ArrayAdapter manages the list for us.
 *
 * Responsibilities:
 *- Inflate diary_list_item.xml for each visible row (reusing convertView when possible)
 *- Bind the DiaryEntry's date + text to the TextViews
 *
 * What this adapter DOES NOT do:
 * - handle clicking, editing, or deleting entries
 * - filter entries
 * - modify the underlying SQLite database
 *
 * All user interactions are handled in DisplayFragment.
 */

class DiaryEntryAdapter(
    context: Context,
    private val entries: MutableList<DiaryEntry>
) : ArrayAdapter<DiaryEntry>(context, 0, entries) {

    /**
     * Called for each row that becomes visible on screen.
     * Reuses convertView when provided (ListView recycling).
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Use an existing recycled view if available, otherwise inflate a new one.
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.diary_list_item, parent, false)

        // UI references inside the row layout
        val tvDate = view.findViewById<TextView>(R.id.tvItemDate)
        val tvText = view.findViewById<TextView>(R.id.tvItemText)

        // Get the corresponding diary entry
        val entry = entries[position]

        // Bind values
        tvDate.text = entry.date
        tvText.text = entry.text

        return view
    }
}
