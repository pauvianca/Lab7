package com.example.lab7

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Simple FragmentStateAdapter used by the ViewPager2
 *
 * Position mapping:
 * 0 = Date tab
 * 1 = EntryFragment (diary entry)
 * 2 = DisplayFragment (diary list)
 */

class PageAdapter(fa: FragmentActivity, private val mNumOfTabs: Int) :
    FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = mNumOfTabs

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DateFragment()
            1 -> EntryFragment()
            2 -> DisplayFragment()
            else -> DateFragment()
        }
    }
}

