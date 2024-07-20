package com.investmango.hrconsole.EmployeeAction

import TotalMemberFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentMemberStatusBinding
import com.investmango.hrconsole.model.Content


class MemberStatus : Fragment() {
    lateinit var apiInterface: ApiInterface
    var listoftotalEmp: List<Content> = arrayListOf()

    private lateinit var binding: FragmentMemberStatusBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_member_status, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = childFragmentManager?.let { ViewPagerAdap(it, 0) }

        // add fragment to the list
        adapter?.addFragment("Present Employees",TotalMemberFragment())
        adapter?.addFragment("Total Employees",TotalMemberFragment())
        binding.viewPager.adapter = adapter
        binding.tabs.setupWithViewPager(binding.viewPager)

//        binding.tabs.addOnTabSelectedListener()
//        prepareViewPager(binding.viewPager,arrayList);
//        PresentEmpRes()

    }

    private fun prepareViewPager(viewPager: ViewPager, arrayList: ArrayList<String>) {
        // Initialize main adapter
//        val adapter = fragmentManager?.let { ViewPagerAdap(it) }

        // Use for loop
        for (i in arrayList.indices) {
            // Initialize bundle
            val bundle = Bundle()


            // Put title
            bundle.putString("title", arrayList[i])

        }
        // set adapter
//        viewPager.adapter = adapter
    }


}
