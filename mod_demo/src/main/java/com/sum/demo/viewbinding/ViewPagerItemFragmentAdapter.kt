package com.sum.demo.viewbinding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerItemFragmentAdapter(fm: FragmentManager, val fragments: List<Fragment>) :
    FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }
}