package com.investmango.hrconsole.EmployeeAction

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPager2Adap(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    var name: ArrayList<String> = arrayListOf()
    var icons: ArrayList<Int> = arrayListOf()
    var fragmentlist: ArrayList<Fragment> = arrayListOf()

    override fun getItemCount(): Int {
        return fragmentlist.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentlist.get(position)
    }


    fun addWithIcon(s: String, iconResId: Int, fragment: Fragment) {
        name.add(s)
        fragmentlist.add(fragment)
        icons.add(iconResId)

    }

}