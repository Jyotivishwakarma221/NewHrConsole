package com.investmango.hrconsole.EmployeeAction.EmplyDetail

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentEmpDetailsBinding
import com.investmango.hrconsole.model.MonthlyPerformanceResp
import java.time.LocalDate

class EmpDetailsFragment : Fragment() {
    private lateinit var binding: FragmentEmpDetailsBinding
    private val userId: Long = 0
    lateinit var languages: Array<String>
    private val apiInterface: ApiInterface? = null
    var empPerformanceList: MonthlyPerformanceResp? = null


    private val token: String? = null
    var month: String? = null
    var descriptionData: Array<String> = arrayOf("C", "C++", "Java", "DSA")

    private val preferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_emp_details, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        languages = resources.getStringArray(R.array.Months)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentdate = LocalDate.now()
            val thismonth = currentdate.month
            month = thismonth.toString()

            Log.e("performance", "onResponse: $month")
        }
//        binding.months.setAdapter(
//            ArrayAdapter<Any?>(
//                context!!,
//                android.R.layout.simple_list_item_1,
//                languages
//            )
//        )
//        fetchPerformanceReport()


//        binding.months.setOnItemClickListener { parent, view1, position, id ->
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                month = parent.getItemAtPosition(position) as String
////                fetchPerformanceReport()
//            }
//        }

//        stepBar()
//        lineGraph()
    }

}