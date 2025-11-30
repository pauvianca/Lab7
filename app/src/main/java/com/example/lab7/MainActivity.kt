package com.example.lab7

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Main layout contains a Toolbar + TabLayout + ViewPager2
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager2>(R.id.pager)

    // ViewPager2 adapter holds the three fragments (Date, Entry, Diary)
        val adapter = PageAdapter(this, 3)
        viewPager.adapter = adapter

        // Attach TabLayout to ViewPager2 and set tab titles
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Date"
                1 -> "Entry"
                2 -> "Diary"
                else -> "Page"
            }
        }.attach()
    }
}
