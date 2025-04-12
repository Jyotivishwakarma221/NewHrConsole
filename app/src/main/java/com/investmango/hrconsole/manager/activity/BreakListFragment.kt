package com.investmango.hrconsole.manager.activity

import android.annotation.SuppressLint
import android.content.Context
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
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.investmango.hrconsole.EmployeeAction.MeetingBreakFragment
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.AttendanceListRecycBinding
import com.investmango.hrconsole.databinding.FragmentBreakListBinding
import com.investmango.hrconsole.model.BreaksListResponse
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.model.breakItem
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.PaginationScrollListener
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class BreakListFragment : Fragment(), RecyclerViewInterface<AttendanceListRecycBinding> {

    private lateinit var binding: FragmentBreakListBinding
    lateinit var layoutManager: LinearLayoutManager
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var authority: String? = null
    var token: String = ""
    private var isLoading = false
    private var isLastPage = false
    private var currentPage = 0
    private var list: ArrayList<breakItem> = arrayListOf()
    private var allActiveUsers: List<TotalEmpResponseItem?>? = null
    var employeList: List<String>? = null
    var isFiltered: Boolean = false
    lateinit var progressDialog: AwesomeProgressDialog
    var startingDate: Long = 0
    var endingDate: Long = 0
    var childuserId: Long = 0
    var startDate: TextView? = null
    var endDate: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        userId = preferences.getLong("userId", 0)
        authority = preferences.getString("Authority", "")

        progressDialog = AwesomeProgressDialog(requireContext())
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)

        if (authority == Constant.MANAGER) {
            employeList = getChildActiveUser()
        } else if (authority == Constant.ADMIN) {
            employeList = getAllActiveUser()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_break_list, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        DateAndTimeUtility.getDate()
        binding.monthYear.setText(
            resources.getStringArray(R.array.Months).get(month) + " " + year.toString()
        )

        binding.filter.setOnClickListener {
            if (authority == Constant.MANAGER || authority == Constant.ADMIN) {
                showManagerOrAdminFilter(context!!)
            } else if (authority == Constant.USER) showFilterBox(
                context!!
            )
        }
//        binding.monthYear.setOnClickListener {
//            showMonths()
//        }
        binding.backbutton.setOnClickListener { activity?.onBackPressed(); }

        attendanceList(0)
        setAdapt()
        binding.recycler.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return this@BreakListFragment.isLastPage
            }

            override fun isLoading(): Boolean {
                return this@BreakListFragment.isLoading
            }

            override fun loadMoreItems() {
                this@BreakListFragment.isLoading = true
                currentPage++
                if (!isFiltered)
                    loadMoreData(currentPage)
                else HandleFiltered(currentPage)
            }
        })

    }

    fun setAdapt() {
        try {

            binding.recycler.setHasFixedSize(true) // Optimizes performance
            binding.recycler.adapter = CommonAdapter(this@BreakListFragment)
            binding.recycler.layoutManager = layoutManager

        } catch (e: Exception) {
            Log.e("Exception", "setAdapt: " + e.message)
        }

    }

    private fun getChildActiveUser(): List<String> {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val userId = preferences.getLong("userId", 0)
        val userNames: ArrayList<String> = ArrayList<String>()

        val call = apiInterface.getTotalEmp(userId, true)
        call.enqueue(object : Callback<List<TotalEmpResponseItem?>> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem?>>,
                response: Response<List<TotalEmpResponseItem?>>,
            ) {
                if (response.isSuccessful) {
                    allActiveUsers = response.body()
                    for (i in 0..allActiveUsers!!.size - 1) {
                        userNames.add(allActiveUsers!![i]!!.userName.uppercase(Locale.getDefault()))
                    }
                } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem?>>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
        return userNames
    }

    private fun getAllActiveUser(): List<String> {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        val userNames: ArrayList<String> = ArrayList<String>()

        val call = apiInterface.getAllEmployee(true)
        call.enqueue(object : Callback<List<TotalEmpResponseItem?>> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem?>>,
                response: Response<List<TotalEmpResponseItem?>>,
            ) {
                if (response.isSuccessful) {
                    allActiveUsers = response.body()
                    for (i in 0..allActiveUsers!!.size - 1) {
                        userNames.add(allActiveUsers!![i]!!.userName.uppercase(Locale.getDefault()))
                    }
                } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem?>>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
        return userNames
    }

    private fun loadMoreData(page: Int) {
        // Simulate network delay
        binding.recycler.postDelayed({
            attendanceList(page)
            isLoading = false
        }, 1500)
    }

    private fun HandleFiltered(page: Int) {
        binding.recycler.postDelayed({
            getUsernewAttendancebyMonth(startingDate, endingDate, page)
            isLoading = false
        }, 1500)
    }

    fun attendanceList(page: Int) {
        try {
            progressDialog.showDialog()
            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface
            val user = childuserId.takeIf { it.toInt() != 0 } ?: userId.toInt()

            val call = apiInterface.getBreak(page, user.toInt(), 31)
            call.enqueue(object : Callback<BreaksListResponse> {
                override fun onResponse(
                    call: Call<BreaksListResponse>,
                    response: Response<BreaksListResponse>,
                ) {
                    if (isAdded) {
                        progressDialog.dismissDialog()
                    }

                    if (response.isSuccessful && response.body() != null) {
                        val content = response.body()?.content?.filterNotNull() ?: emptyList()

                        // Clear list if this is page 0 (first load)
                        if (page == 0) {
                            list.clear()
                        }

                        if (content.isNotEmpty()) {
                            binding.noDataFound.visibility = View.GONE
                            binding.recycler.visibility = View.VISIBLE
                            list.addAll(content)
                            binding.recycler.adapter?.notifyDataSetChanged()
                        } else {
                            if (list.isEmpty()) {
                                binding.noDataFound.visibility = View.VISIBLE
                                binding.recycler.visibility = View.GONE
                            }
                            isLastPage = true
                        }
                    } else {
                        if (isAdded) {
                            Toast.makeText(
                                requireContext(),
                                "Something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<BreaksListResponse>, t: Throwable) {
                    if (isAdded) {
                        progressDialog.dismissDialog()
                        Toast.makeText(
                            requireContext(),
                            "Server error " + t.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.e("attendance", "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUsernewAttendancebyMonth(startDate: Long, endDate: Long, page: Int) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()
        val user = childuserId.takeIf { it.toInt() != 0 } ?: userId.toInt()

        val call: Call<BreaksListResponse> = when {
            startDate != 0L && endDate == 0L -> apiInterface.getBreaksByStartMonth(
                startDate, 31, user.toInt(), page
            )
            startDate == 0L && endDate != 0L -> apiInterface.getBreaksByEndMonth(
                endDate, 31, user.toInt(), page
            )
            startDate != 0L && endDate != 0L -> apiInterface.getBreaksByBothMonth(
                startDate, endDate, 31, user.toInt(), page
            )
            else -> apiInterface.getBreak(page, user.toInt(), 31)
        }

        call.enqueue(object : Callback<BreaksListResponse> {
            override fun onResponse(
                call: Call<BreaksListResponse>,
                response: Response<BreaksListResponse>,
            ) {
                if (isAdded) {
                    progressDialog.dismissDialog()
                }
                isLoading = false

                if (response.isSuccessful && response.body() != null) {
                    isFiltered = true
                    val content = response.body()?.content?.filterNotNull() ?: emptyList()

                    // Clear list if this is page 0 (first load)
                    if (page == 0) {
                        list.clear()
                    }

                    if (content.isNotEmpty()) {
                        list.addAll(content)
                        binding.noDataFound.visibility = View.GONE
                        binding.recycler.visibility = View.VISIBLE
                        binding.recycler.adapter?.notifyDataSetChanged()
                    } else {
                        isLastPage = true
                        if (list.isEmpty()) {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.recycler.visibility = View.GONE
                        }
                    }
                } else {
                    try {
                        val errorMessage = response.errorBody()?.string()
                        Log.e("attendance", "API call not successful: $errorMessage")

                        errorMessage?.let {
                            val errorJson = JSONObject(it)
                            val message = errorJson.optString("message")
                            if (isAdded) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("attendance", "Error parsing error response: " + e.message)
                    }
                }
            }

            override fun onFailure(call: Call<BreaksListResponse>, t: Throwable) {
                if (isAdded) {
                    progressDialog.dismissDialog()
                    Toast.makeText(
                        requireContext(),
                        "Server error " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("attendance", "onFailure: " + t.message)
            }
        })
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
        val statusText = dialog1.findViewById<TextView>(R.id.statusText)

        val childSpinner = dialog1.findViewById<Spinner>(R.id.personal)
        val showResult = dialog1.findViewById<TextView>(R.id.showresult)
        statusSpin?.visibility = View.GONE
        statusText?.visibility = View.GONE

        startDate = dialog1.findViewById<TextView>(R.id.startDate)
        endDate = dialog1.findViewById<TextView>(R.id.endDate)

        startDate?.setOnClickListener(View.OnClickListener { openDatePicker("start") })
        endDate?.setOnClickListener(View.OnClickListener { openDatePicker("") })

        showResult!!.setOnClickListener {
            val startdate = DateAndTimeUtility.dateToEpoch(startDate?.getText().toString())
            Log.e("startdate", "showFilterBox: $startdate")

            val enddate = DateAndTimeUtility.dateToEpoch(endDate?.getText().toString())

            if (startdate == 0L && enddate == 0L && childuserId == 0L
            ) Toast.makeText(context, "Select Date or status", Toast.LENGTH_SHORT).show()
            else {
                Log.e("childuserId", "onClick: $childuserId")
                if (childuserId != 0L) {
                    list.clear()
                    getUsernewAttendancebyMonth(
                        startdate,
                        enddate, 0
                    )
                    dialog1.dismiss()
                }
            }
        }
        dialog1.show()
        if (employeList != null && !employeList!!.isEmpty()) {
            val arrayAdapter2 = ArrayAdapter(
                requireContext(),
                R.layout.color_spinner_layout,
                employeList!!
            )
            arrayAdapter2.setDropDownViewResource(R.layout.spinner_dropdown_layout)

            assert(childSpinner != null)
            childSpinner!!.adapter = arrayAdapter2
            if (!employeList!!.isEmpty()) {
                childSpinner!!.setSelection(0)
            }

            childSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long,
                ) {
                    childuserId = allActiveUsers!![position]!!.id
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        } else Toast.makeText(context, "Unable to Load employee.", Toast.LENGTH_SHORT).show()
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
        val statusText = dialog1.findViewById<TextView>(R.id.statustxt)
        val showResult = dialog1.findViewById<TextView>(R.id.showresult)
        statusSpin?.visibility = View.GONE
        statusText?.visibility = View.GONE


        startDate = dialog1.findViewById<TextView>(R.id.startDate)
        endDate = dialog1.findViewById<TextView>(R.id.endDate)

        startDate?.setOnClickListener(View.OnClickListener { openDatePicker("start") })
        endDate?.setOnClickListener(View.OnClickListener { openDatePicker("") })

        showResult!!.setOnClickListener {
            val startdate = DateAndTimeUtility.dateToEpoch(startDate?.getText().toString())
            Log.e(
                "startdate",
                "showFilterBox: "
            )
            startingDate = startdate
            val enddate = DateAndTimeUtility.dateToEpoch(endDate?.getText().toString())
            endingDate = enddate
//            if (startdate == 0L && enddate == 0L) Toast.makeText(context, "Select Date or child", Toast.LENGTH_SHORT).show()
//            else {
            getUsernewAttendancebyMonth(startingDate, endingDate, 0)
            dialog1.dismiss()
//            }
        }
        dialog1.show()
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
                startDate?.setText(selectedDate)
            } else endDate?.setText(selectedDate)
        }
        picker.show(requireFragmentManager(), picker.toString())

    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): AttendanceListRecycBinding {
        return AttendanceListRecycBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return list.size
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun bindView(viewBind: AttendanceListRecycBinding, position: Int) {
        val breakItem = list[position]

        // Ensure visibility is always set properly
        viewBind.BreakTime.visibility = View.VISIBLE
        viewBind.reasonlay.visibility = View.VISIBLE

        // Set default values to avoid displaying stale data
        viewBind.date.text = ""
        viewBind.inTime.text = ""
        viewBind.outTime.text = ""
        viewBind.BreakTime.text = ""

        // Bind new data
        viewBind.date.text = DateAndTimeUtility.getDATEFromLong(breakItem.startTime)
        viewBind.inTime.text = DateAndTimeUtility.getTimeInHourFromLong(breakItem.startTime)
        viewBind.reason.text = breakItem.reason
        viewBind.reason.setOnClickListener {
            if (viewBind.reason.text.toString().equals("Meeting")){
                (activity as (ManagerActivity)).replaceFragment(MeetingBreakFragment())

            }
        }

        if (breakItem.endTime != null && breakItem.endTime != 0L) {
            viewBind.outTime.text = DateAndTimeUtility.getTimeInHourFromLong(breakItem.endTime)

            // Calculate and set break time difference
            viewBind.BreakTime.text =
                DateAndTimeUtility.getDifferenceInMinutes(breakItem.startTime, breakItem.endTime)
                    .toString() + " mins"
        } else {
            viewBind.outTime.text = "-"  // Show a placeholder if endTime is null
            viewBind.BreakTime.text = "0 mins"
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("breakList", "onResume: " +list + binding.recycler.adapter)
    }

}