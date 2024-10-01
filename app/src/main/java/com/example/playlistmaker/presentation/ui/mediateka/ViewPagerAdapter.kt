package com.example.playlistmaker.presentation.ui.mediateka

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.util.TAB_COUNT

class ViewPagerAdapter(fm:FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount(): Int {
        return TAB_COUNT
    }

    override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> FavoritesFragment.newInstance()
                else-> PlayListFragment.newInstance()
            }
    }
}