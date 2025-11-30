package com.example.lab7

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class DisplayFragment : Fragment() {

    lateinit var viewModel: MyViewModel
    private lateinit var listView: ListView
    private lateinit var adapter: DiaryEntryAdapter
    private val items = mutableListOf<DiaryEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run {
            ViewModelProvider(this)[MyViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        val view = inflater.inflate(R.layout.fragment_display, container, false)

        listView = view.findViewById(R.id.lvEntries)
        adapter = DiaryEntryAdapter(requireContext(), items)
        listView.adapter = adapter

        // Load from DB on start
        viewModel.loadEntriesFromDb(requireContext())

        // Observe LiveData and update list
        viewModel.entries.observe(viewLifecycleOwner, { list ->
            items.clear()
            if (list != null) {
                items.addAll(list)
            }
            adapter.notifyDataSetChanged()
        })

        // Long press to delete
        listView.setOnItemLongClickListener { _, _, position, _ ->
            if (position < 0 || position >= items.size) return@setOnItemLongClickListener true

            val entryToDelete = items[position]
            viewModel.deleteEntryFromDb(requireContext(), entryToDelete.id)
            Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show()
            true
        }

        return view
    }
}
