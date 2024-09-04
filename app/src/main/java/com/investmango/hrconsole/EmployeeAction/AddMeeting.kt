package com.investmango.hrconsole.EmployeeAction

import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.investmango.hrconsole.EmployeeAction.EmplyDetail.AdapterForTimeSlot
import com.investmango.hrconsole.EmployeeAction.EmplyDetail.FilteredMeetingFragment
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.CalenderHolderBinding
import com.investmango.hrconsole.databinding.FragmentAddMemberBinding
import com.investmango.hrconsole.manager.activity.ManagerActivity
import com.investmango.hrconsole.model.MeetingItem
import com.investmango.hrconsole.model.MeetingListResponse
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.model.calenderDate
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class AddMeeting : Fragment(), RecyclerViewInterface<CalenderHolderBinding> {
    val daysList = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    var list: ArrayList<calenderDate> = arrayListOf()
    var currentDate: Int = 1
    private lateinit var binding: FragmentAddMemberBinding
    val timeSlots = mutableListOf<String>()
    var meetingLis: List<MeetingItem?> = arrayListOf()
    var meetingList2: ArrayList<MeetingItem?> = arrayListOf()
    var meetingList3: List<MeetingItem?>? = emptyList()
    lateinit var apiInterface: ApiInterface
    lateinit var date: String
    lateinit var startDate: TextView
    lateinit var endDate: TextView
    var childuserId: Long = 0
    var ViewOf: String = ""
    var employeList: ArrayList<String>? = arrayListOf()
    var authority: String = ""
    lateinit var progressDialog: AwesomeProgressDialog
    var allActiveUsers: List<TotalEmpResponseItem?>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)

        authority = preferences.getString("Authority", "0").toString()

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)
        progressDialog.showDialog()

        if (arguments != null)
            ViewOf = arguments?.getString("ViewOf").toString()

        generateTimeSlots()

        if (authority.equals(Constant.MANAGER))
            getChildActiveUser()
        else if (authority.equals(Constant.ADMIN))
            getAllActiveUser()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_member, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getMeetings()
        if (authority.equals(Constant.USER))
            binding.createMeeting.visibility = View.GONE
        else {
            binding.createMeeting.visibility = View.VISIBLE

        }
        if (ViewOf.equals("Own")) {
            binding.createMeeting.visibility = View.GONE
        } else binding.createMeeting.visibility = View.VISIBLE


        binding.createMeeting.setOnClickListener {
            (activity as ManagerActivity?)!!.replaceFragment(CreateNewFragment())
        }

        binding.Filter.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ViewOf.equals("Own")) {
                    showFilterBox(context!!)
                } else {
                    if (authority.equals(Constant.USER))
                        showFilterBox(context!!)
                    else
                        showManagerOrAdminFilter(context!!)
                }
            }
        }


        val cal = Calendar.getInstance()
//        cal.set(Calendar.MONTH, Calendar.JANUARY)
        currentDate = cal.get(Calendar.DAY_OF_MONTH)
        var daysInMonth = cal.get(Calendar.DAY_OF_WEEK)

        date = currentDate.toString()

        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (x in currentDate..maxDay) {

            list.add(calenderDate(x.toString(), daysList.get(daysInMonth - 1)))
            if (daysInMonth - 1 < 6) {
                daysInMonth += 1
            } else daysInMonth = 1
        }

        Log.e("currentDate", "onViewCreated: " + list + "  " + daysInMonth)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showManagerOrAdminFilter(context: Context) {
        val dialog1 = BottomSheetDialog(context, R.style.BottomSheetDialog)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setCancelable(true)
        dialog1.setCanceledOnTouchOutside(true)
        dialog1.setContentView(R.layout.manger_admin_filter)
        val window = dialog1.window!!
        window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        val statusSpin = dialog1.findViewById<Spinner>(R.id.status)
        val childSpinner = dialog1.findViewById<Spinner>(R.id.personal)
        val showResult = dialog1.findViewById<TextView>(R.id.showresult)


        val list2 = resources.getStringArray(R.array.MeetingType)

        val arrayAdapter =
            ArrayAdapter<Any?>(requireContext(), R.layout.color_spinner_layout, list2)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        assert(statusSpin != null)
        statusSpin!!.adapter = arrayAdapter


        val arrayAdapter2 = ArrayAdapter(
            requireContext(),
            R.layout.color_spinner_layout,
            employeList as MutableList<String>
        )
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


//        arrayAdapter2.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        childSpinner!!.adapter = arrayAdapter2

        childSpinner.setSelection(0)

        childSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long,
            ) {
                childuserId = allActiveUsers?.get(position)?.getId()!!
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        startDate = dialog1.findViewById(R.id.startDate)!!
        endDate = dialog1.findViewById(R.id.endDate)!!

        startDate.setOnClickListener { openDatePicker("start") }
        endDate.setOnClickListener { openDatePicker("") }

        showResult!!.setOnClickListener {
            val startdate = DateAndTimeUtility.dateToEpoch(startDate.text.toString())
            Log.e("startdate", "showFilterBox: $childuserId")

            val enddate = DateAndTimeUtility.dateToEpoch(endDate.text.toString())

            if (startdate == 0L && enddate == 0L && statusSpin!!.selectedItem.toString()
                    .trim { it <= ' ' } == "--"
            ) Toast.makeText(context, "Select Date or status", Toast.LENGTH_SHORT).show()
            else {

                val fragment = FilteredMeetingFragment()
                val bundle = Bundle()
                bundle.putLong("startdate", startdate)
                bundle.putLong("enddate", enddate)
                bundle.putLong("childId", childuserId)
                bundle.putString("ViewOf", "child")
                bundle.putString("status", statusSpin.getSelectedItem().toString())
                fragment.arguments = bundle

                (activity as ManagerActivity?)!!.replaceFragment(fragment)

                dialog1.dismiss()
            }
        }
        dialog1.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showFilterBox(context: Context) {
        val dialog1 = BottomSheetDialog(context, R.style.BottomSheetDialog)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setCancelable(true)
        dialog1.setCanceledOnTouchOutside(true)
        dialog1.setContentView(R.layout.filter_layput)
        val window = dialog1.window!!
        window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )


        val statusSpin = dialog1.findViewById<Spinner>(R.id.status)
        val showResult = dialog1.findViewById<TextView>(R.id.showresult)
        val list2 = resources.getStringArray(R.array.MeetingType)

        val arrayAdapter =
            ArrayAdapter<Any?>(requireContext(), R.layout.color_spinner_layout, list2)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        assert(statusSpin != null)
        statusSpin!!.adapter = arrayAdapter

        startDate = dialog1.findViewById(R.id.startDate)!!!!
        endDate = dialog1.findViewById(R.id.endDate)!!!!

        startDate.setOnClickListener { openDatePicker("start") }
        endDate.setOnClickListener { openDatePicker("") }

        showResult!!.setOnClickListener {
            val startdate = DateAndTimeUtility.dateToEpoch(startDate.text.toString())
            Log.e(
                "startdate",
                "showFilterBox: " + statusSpin!!.selectedItem.toString().trim { it <= ' ' })

            val enddate = DateAndTimeUtility.dateToEpoch(endDate.text.toString())
            if (startdate == 0L && enddate == 0L && statusSpin!!.selectedItem.toString()
                    .trim { it <= ' ' } == "--"
            ) Toast.makeText(context, "Select Date or status", Toast.LENGTH_SHORT).show()
            else {
                val fragment = FilteredMeetingFragment()
                val bundle = Bundle()
                bundle.putLong("startdate", startdate)
                bundle.putLong("enddate", enddate)
                bundle.putString("ViewOf", "own")
                bundle.putString("status", statusSpin.getSelectedItem().toString())
                fragment.arguments = bundle

                (activity as ManagerActivity?)!!.replaceFragment(fragment)
                dialog1.dismiss()
            }
        }
        dialog1.show()
    }

    private fun getAllActiveUser() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface

        val call = apiInterface.getAllEmployee(true)
        call.enqueue(object : Callback<List<TotalEmpResponseItem?>> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem?>>,
                response: Response<List<TotalEmpResponseItem?>>,
            ) {
                if (response.isSuccessful) {
                    employeList?.clear()
                    allActiveUsers = response.body()
                    for (i in 0..allActiveUsers!!.size - 1) {
                        employeList?.add(allActiveUsers!![i]!!.userName.uppercase(Locale.getDefault()))
                    }
                } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem?>>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    fun AcceptMeet(meetId: Int) {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface

        val call = apiInterface.AcceptMeet(meetId, true)
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>,
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Meeting Accepted .", Toast.LENGTH_SHORT).show()

                } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    private fun getChildActiveUser() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val userId = preferences.getLong("userId", 0)


        val call = apiInterface.getTotalEmp(userId, true)
        call.enqueue(object : Callback<List<TotalEmpResponseItem?>> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem?>>,
                response: Response<List<TotalEmpResponseItem?>>,
            ) {
                if (response.isSuccessful) {
                    allActiveUsers = response.body()
                    employeList?.clear()
                    allActiveUsers = response.body()
                    for (i in 0..allActiveUsers!!.size - 1) {
                        employeList?.add(allActiveUsers!![i]!!.userName.uppercase(Locale.getDefault()))
                    }
                } else Toast.makeText(context, "Something went wrrong", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem?>>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    private fun openDatePicker(type: String) {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Select Date")
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection: Long? ->
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = selection!!
            val year = calendar[java.util.Calendar.YEAR]
            val month = calendar[java.util.Calendar.MONTH]
            val dayOfMonth = calendar[java.util.Calendar.DAY_OF_MONTH]
            val selectedDate =
                dayOfMonth.toString() + "/" + (month + 1) + "/" + year
            if (type == "start") {
                startDate.setText(selectedDate)
            } else endDate.setText(selectedDate)
        }
        picker.show(requireFragmentManager(), picker.toString())

    }

    private fun getMeetings() {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface

        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val userId = preferences.getLong("userId", 0)

        val call: Call<MeetingListResponse>? = apiInterface.getTodayMeeting(userId)
        call?.enqueue(object : Callback<MeetingListResponse?> {
            override fun onResponse(
                call: Call<MeetingListResponse?>,
                response: Response<MeetingListResponse?>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    meetingLis = response.body()!!.content!!
                    if (isAdded)
                        setAdapt()
                    Log.e("getmeetings", "onResponse: " + response.body())
                } else {
                    Log.e("getmeetings", "onResponse: " + response.body().toString())

                    Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MeetingListResponse?>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    fun setAdapt() {
        binding.recyclerForCalender.adapter = CommonAdapter(this)
        if (isAdded)
            binding.recyclerForCalender.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    fun setAdapter2() {
        binding.recyclerFortime.adapter = AdapterForTimeSlot(this, timeSlots, meetingList3!!)
        progressDialog.dismissDialog()

        if (isAdded)
            binding.recyclerFortime.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): CalenderHolderBinding {
        return CalenderHolderBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return list.size
    }

    override fun bindView(viewBind: CalenderHolderBinding, position: Int) {
        viewBind.date.setText(list.get(position).date)
        viewBind.weekDay.setText(list.get(position).dayOfDay)

//            for (i in 0..meetingLis.size) {
//                var date = DateAndTimeUtility.getDateFromLong(meetingLis.get(i)?.meetingTime)
//
//            if (date.toString() == list.get(position).date) {
//
//                meetingList2.add(meetingLis.get(position))
//            }


        if (date == list.get(position).date) {

            binding.recyclerForCalender.callOnClick()
            binding.recyclerForCalender.requestFocus()
            viewBind.layout.setBackgroundResource(R.drawable.blue_bg)
            if (!meetingLis.isEmpty()) {
                meetingList3 = meetingLis.filter {
                    DateAndTimeUtility.getDateFromLong(it?.meetingTime).toString() == list.get(
                        position
                    ).date
                }
            }
            Log.e("meetingList", "bindView: " + meetingList3)
            setAdapter2()

        }


        viewBind.layout.setOnClickListener {
            progressDialog.showDialog()
            date = list.get(position).date
            setAdapt()
            setAdapter2()

        }
    }


    fun generateTimeSlots(): List<String> {
        for (hour in 10..22) {
            val period = if (hour < 12) "am" else "pm"
            val displayHour = if (hour <= 12) hour else hour - 12
            timeSlots.add(String.format("%02d %s", displayHour, period))
        }
        return timeSlots
    }

    fun EditMeeting(item: MeetingItem) {
        val frag = CreateNewFragment()
        val bundle = Bundle()
        bundle.putSerializable("meeting", item)
        frag.arguments = bundle
        (activity as ManagerActivity?)!!.replaceFragment(frag)

    }
}