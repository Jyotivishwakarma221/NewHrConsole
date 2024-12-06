package com.investmango.hrconsole.EmployeeAction

import android.os.Bundle
import android.provider.Settings.Global.putInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdap(supportFragmentManager: FragmentManager, selected: Int) :
    FragmentStatePagerAdapter(supportFragmentManager) {
    var name: ArrayList<String> = arrayListOf()
    var icons: ArrayList<Int> = arrayListOf()
    var fragmentlist: ArrayList<Fragment> = arrayListOf()

    override fun getCount(): Int {
        return name.size
    }

    override fun getItem(position: Int): Fragment {
        fragmentlist.get(position).arguments = Bundle().apply {
            // Our object is just an integer :-P
            putString("Emp", name.get(position))
        }
        return fragmentlist.get(position)

//        if (position == 0) {
//            return TotalMemberFragment()
//        } else return TotalMemberFragment()

    }


    override fun getPageTitle(position: Int): CharSequence {
        // return title of the tab
        return name[position]
    }

    fun addFragment(s: String, fragment: Fragment) {
        name.add(s)
        fragmentlist.add(fragment)
    }
    fun addWithIcon(s: String, iconResId: Int,fragment: Fragment) {
        name.add(s)
        fragmentlist.add(fragment)
        icons.add(iconResId)

    }
//
//    override fun getItemCount(): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun createFragment(position: Int): Fragment {
//        TODO("Not yet implemented")
//    }
}