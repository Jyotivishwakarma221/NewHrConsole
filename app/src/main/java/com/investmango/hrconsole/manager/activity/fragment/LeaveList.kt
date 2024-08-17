package com.investmango.hrconsole.manager.activity.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.DatePicker
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
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentLeaveListBinding
import com.investmango.hrconsole.databinding.LeaveRecyclerBinding
import com.investmango.hrconsole.model.AllLeaveResponse
import com.investmango.hrconsole.model.LeaveItem
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.PaginationScrollListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.util.Calendar


class LeaveList : Fragment(), RecyclerViewInterface<LeaveRecyclerBinding>,
    DatePickerDialog.OnDateSetListener {

    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var leavelist: ArrayList<LeaveItem?>? = arrayListOf()
    private lateinit var binding: FragmentLeaveListBinding
    var leaveType = ""
    private var isLoading = false
    private var isLastPage = false
    var isfiltered = false
    private var currentPage = 0
    lateinit var progressDialog: AwesomeProgressDialog
    lateinit var filterredList: List<LeaveItem?>
    lateinit var layoutManager: LinearLayoutManager

    lateinit var startDate: TextView
    lateinit var endDate: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_leave_list, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0").toString()
        userId = preferences.getLong("userId", 0)

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        try {
            fetchAllLeaves(currentPage)
        } catch (e: Exception) {
            if (isAdded)
                Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            Log.e("execption", "loadMoreData: "+e.message )
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        binding.monthYear.setText(
            resources.getStringArray(R.array.Months).get(month) + " " + year.toString()
        )

        arguments?.takeIf { it.containsKey("Emp") }?.apply {
            Log.e("arguments", "onViewCreated: " + getString("Emp"))

            leaveType = getString("Emp").toString()
        }


        binding.monthYear.setOnClickListener {
//            showMonthYearPicker()
            showMonths()
        }

        binding.filter.setOnClickListener {
            showFilterBox(context)
        }

        binding.recyclerView.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return this@LeaveList.isLastPage
            }

            override fun isLoading(): Boolean {
                return this@LeaveList.isLoading
            }

            override fun loadMoreItems() {
                this@LeaveList.isLoading = true
                currentPage++
                if (!isfiltered)
                    loadMoreData(currentPage)
            }
        })
    }

    private fun loadMoreData(page: Int) {
        // Simulate network delay
        binding.recyclerView.postDelayed({
            // Fetch data from your data source
            try {
                fetchAllLeaves(page)
            } catch (e: Exception) {
                if (isAdded)
                    Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_LONG)
                        .show()
                Log.e("execption", "loadMoreData: "+e.message )
            }
            isLoading = false
            isLastPage = leavelist?.isEmpty() == true // Assume no more data if newItems is empty
        }, 100)
    }

    private fun fetchAllLeaves(page: Int) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        progressDialog?.showDialog()
        val call = apiInterface.getAllLeaves(userId, page, 10)
        Log.e("allLeaves", "fetchAllLeaves: " + userId + "  " + page)
        call.enqueue(object : Callback<AllLeaveResponse> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(
                call: Call<AllLeaveResponse>,
                response: Response<AllLeaveResponse>,
            ) {
                if (response.isSuccessful) {
                    try {
                        progressDialog.dismissDialog()

                        val size = response.body()?.content?.size!!
                        if (size > 0) {
                            for (i in 0..size - 1) {
                                leavelist?.add(response.body()?.content!!.get(i))
                            }

                            binding.recyclerView.adapter?.notifyItemInserted(leavelist?.size!!)
                        }
                        Log.e("allLeaves", "onResponse: " + leavelist?.size)
                        if (leaveType.equals("All")) {
                            filter("All")
                        } else if (leaveType.equals("Half")) {
                            filter("HALF_DAY")
                        } else if (leaveType.equals("Absent")) {
                            filter("ABSENT")
                        } else {
                            if (isAdded)
                                Toast.makeText(context, "Something went wrong.", Toast.LENGTH_SHORT)
                                    .show()

                        }
                    } catch (ex: Exception) {
                        progressDialog?.dismissDialog()

                        Log.e("allLeaves", "onResponse: " + ex)
                        if (isAdded)
                            Toast.makeText(context!!, "Something went wrong.", Toast.LENGTH_SHORT)
                                .show()
                    }
                } else {
                    progressDialog?.dismissDialog()

                    val errorMessage = response.errorBody()!!.string()
                    val errorJson = JSONObject(errorMessage)
                    val message = errorJson.optString("message")
                    if (isAdded)
                        Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
                    Log.e("allLeaves", "onResponse: " + message)
                }
            }

            override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                Log.e("EmployeePerformance", "Server error", t)
                progressDialog?.dismissDialog()

            }
        })
    }

    fun filter(leaveType: String) {
        if (leaveType == "All") {
            setAdapt()
        } else {
            filterredList = leavelist?.filter { it?.leaveType == leaveType }!!
            Log.e("filteringHalfDay", "filter: " + filterredList?.size)
            setAdapt()
            if (filterredList.size == 0) {
                if (isAdded)
                    Toast.makeText(context, "No Data Found.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun setAdapt() {
        binding.recyclerView.adapter = CommonAdapter(this@LeaveList)
        binding.recyclerView.layoutManager = layoutManager
    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): LeaveRecyclerBinding {
        return LeaveRecyclerBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        if (leaveType == "All") {
            return leavelist?.size!!
        } else return filterredList.size
    }

    override fun bindView(viewBind: LeaveRecyclerBinding, position: Int) {
        if (leaveType == "All") {
            if (leavelist?.get(position)?.leaveType == "HALF_DAY") {
                viewBind.id.text = "Half Day "
                viewBind.approovedType.text = "   Half Day "
                viewBind.leavetypeReject.text = "   Half Day "
            } else {
                viewBind.id.text = "Absent "
                viewBind.approovedType.text = " Absent "
                viewBind.leavetypeReject.text = " Absent "
            }

            val size = leavelist?.get(position)?.leaveDates?.size

            if (size!! > 0)
                viewBind.date.setText(leavelist?.get(position)?.leaveDates?.get(0))
            else viewBind.date.setText(
                leavelist?.get(position)?.leaveDates?.get(0) + " -" + leavelist?.get(
                    position
                )?.leaveDates?.get(size - 1)
            )

            if (leavelist?.get(position)?.status == "APPROVED") {
                viewBind.approved.visibility = View.VISIBLE
                viewBind.approovedType.visibility = View.VISIBLE
                viewBind.leavetypeReject.visibility = View.GONE
                viewBind.approvedOrdecline.setText(" Approved by")
                viewBind.approvedBy.text = leavelist?.get(position)?.approvedByName.toString()
            } else if (leavelist?.get(position)?.status == "REJECTED") {
                viewBind.approved.visibility = View.VISIBLE
                viewBind.approovedType.visibility = View.GONE
                viewBind.leavetypeReject.visibility = View.VISIBLE
                viewBind.approvedOrdecline.setText("Rejected by")
                Log.e(
                    "approvedBy",
                    "bindView: " + leavelist?.get(position)?.leaveDates + "  " + leavelist?.get(
                        position
                    )?.approvedByName.toString()
                )
                viewBind.approvedBy.text = leavelist?.get(position)?.approvedByName.toString()
            } else {
                viewBind.approovedType.visibility = View.GONE
                viewBind.leavetypeReject.visibility = View.VISIBLE
                viewBind.approvedOrdecline.setText("Awaiting")
                viewBind.approvedBy.visibility = View.GONE

            }

            Log.e("TAGForlEAVE", "bindView: " + leavelist?.get(position)?.leaveType)

        } else {
            if (filterredList.get(position)?.status == "APPROVED") {
                viewBind.approved.visibility = View.VISIBLE
                viewBind.approovedType.visibility = View.VISIBLE
                viewBind.leavetypeReject.visibility = View.GONE
                viewBind.approvedOrdecline.setText(" Approved by")
                viewBind.approvedBy.text = filterredList.get(position)?.approvedByName.toString()

            } else if (filterredList.get(position)?.status == "REJECTED") {
                viewBind.approovedType.visibility = View.GONE
                viewBind.leavetypeReject.visibility = View.VISIBLE
                viewBind.approvedOrdecline.setText("Rejected by")
                viewBind.approvedBy.text = filterredList.get(position)?.approvedByName.toString()
            } else {
                viewBind.approovedType.visibility = View.GONE
                viewBind.leavetypeReject.visibility = View.VISIBLE
                viewBind.approvedOrdecline.setText("Awaiting")
                viewBind.approvedBy.visibility = View.GONE

            }
            if (filterredList.get(position)?.leaveType == "HALF_DAY") {
                viewBind.id.text = "Half Day "
                viewBind.approovedType.text = "   Half Day "
                viewBind.leavetypeReject.text = "   Half Day "
            } else {
                viewBind.id.text = "Absent "
                viewBind.approovedType.text = " Absent "
                viewBind.leavetypeReject.text = " Absent "

            }

            val size = filterredList.get(position)?.leaveDates?.size
            if (size!! > 0)
                viewBind.date.setText(filterredList?.get(position)?.leaveDates?.get(0))
            else viewBind.date.setText(
                filterredList?.get(position)?.leaveDates?.get(0) + " -" + filterredList?.get(
                    position
                )?.leaveDates?.get(size - 1)
            )

            Log.e("filterredList", "bindView: " + filterredList.get(position)?.approved)
//            if (filterredList.get(position)?.approved == true) {
//                viewBind.approved.visibility = View.VISIBLE
//                viewBind.approvedOrdecline.setText(" Approved by")
//                viewBind.approvedBy.text = filterredList.get(position)?.approvedByName.toString()
//            } else {
//                viewBind.approvedOrdecline.setText("Awaiting")
//                viewBind.approvedBy.visibility = View.GONE
//            }


        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun showMonths() {
        val yearSelected: Int
        val monthSelected: Int

        //Set default values
        val calendar = Calendar.getInstance()
        yearSelected = calendar[Calendar.YEAR]
        monthSelected = calendar[Calendar.MONTH]

        val dialogFragment = MonthYearPickerDialogFragment
            .getInstance(monthSelected, yearSelected)

        fragmentManager?.let { dialogFragment.show(it, null) }
        dialogFragment.setOnDateSetListener { year, monthOfYear ->
            Log.e(
                "monthOfYear",
                "showMonths: " + resources.getStringArray(R.array.Months)
                    .get(monthOfYear) + " " + year
            )
            binding.monthYear.setText(
                resources.getStringArray(R.array.Months)
                    .get(monthOfYear) + " " + year
            )
            try {
                val date = "1/" + monthOfYear + "/" + year
//                    val startDate = DateAndTimeUtility.monthYearToEpoch(monthOfYear, year)
                val startDate = DateAndTimeUtility.dateToEpoch(date)

                val endDate = DateAndTimeUtility.getCurrentinMili()
                Log.e(
                    "startDate",
                    "showMonths: " + startDate + "  " + Instant.now().toEpochMilli()
                )
                getFilterLeave(startDate, endDate, "")
            } catch (e: Exception) {
                if (isAdded)
                    Toast.makeText(context, "something went wrong.", Toast.LENGTH_LONG).show()
                Log.e("showMonths", "showMonths: " + e.message)
            }
        }
    }

    private fun getFilterLeave(startDate: Long, endDate: Long, status: String) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        progressDialog?.showDialog()

        //with only status
        if (startDate == 0L && endDate == 0L && status.trim() != "--") {

            val call = apiInterface.getFilteredLeaveWithoutDate(userId, status)

            Log.e("allLeaves", "fetchAllLeaves: " + userId + "  " + startDate)
            call.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.isSuccessful) {
                        progressDialog?.dismissDialog()

                        try {
                            isfiltered = true
                            val size = response.body()?.content?.size!!
                            if (size > 0) {
                                leavelist?.clear()

                                for (i in 0..size - 1) {
                                    leavelist?.add(response.body()?.content!!.get(i))
                                }
                                binding.noDataFound.visibility = View.GONE
                                binding.recyclerView.visibility = View.VISIBLE

                                binding.recyclerView.adapter?.notifyItemInserted(leavelist?.size!!)
                            } else {
                                binding.noDataFound.visibility = View.VISIBLE
                                binding.recyclerView.visibility = View.GONE

                                if (isAdded)
                                    Toast.makeText(context, "No data found.", Toast.LENGTH_LONG)
                                        .show()
                            }
                            Log.e("allLeaves", "onResponse: " + leavelist?.size)
                            if (leaveType.equals("All")) {
                                filter("All")
                            } else if (leaveType.equals("Half")) {
                                filter("HALF_DAY")
                            } else if (leaveType.equals("Absent")) {
                                filter("ABSENT")
                            } else {
                                if (isAdded)
                                    Toast.makeText(
                                        context,
                                        "Something went wrong.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                            }
                        } catch (ex: Exception) {
                            Log.e("allLeaves", "onResponse: " + ex)
                            if (isAdded)
                                Toast.makeText(
                                    context!!,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }
                    } else {
                        progressDialog?.dismissDialog()

                        val errorMessage = response.errorBody()!!.string()
                        val errorJson = JSONObject(errorMessage)
                        val message = errorJson.optString("message")
                        if (isAdded)
                            Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG)
                                .show()
                        Log.e("allLeaves", "onResponse: " + message)
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()

                    Log.e("EmployeePerformance", "Server error", t)
                }
            })
        }
        //with all three
        if (startDate != 0L && endDate != 0L && status.trim() != "--") {
            val call = apiInterface.getFilteredLeave(userId, startDate, endDate, status)
            Log.e("allLeaves", "fetchAllLeaves: " + userId + "  " + startDate)
            call.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.isSuccessful) {
                        progressDialog?.dismissDialog()

                        isfiltered = true

                        try {
                            val size = response.body()?.content?.size!!
                            if (size > 0) {
                                leavelist?.clear()

                                for (i in 0..size - 1) {
                                    leavelist?.add(response.body()?.content!!.get(i))
                                }

                                binding.recyclerView.adapter = CommonAdapter(this@LeaveList)
                            } else {
                                if (isAdded)
                                    Toast.makeText(context, "No data found.", Toast.LENGTH_LONG)
                                        .show()
                            }
                            Log.e("allLeaves", "onResponse: " + leavelist?.size)
                            if (leaveType.equals("All")) {
                                filter("All")
                            } else if (leaveType.equals("Half")) {
                                filter("HALF_DAY")
                            } else if (leaveType.equals("Absent")) {
                                filter("ABSENT")
                            } else {
                                if (isAdded)
                                    Toast.makeText(
                                        context,
                                        "Something went wrong.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                            }
                        } catch (ex: Exception) {
                            Log.e("allLeaves", "onResponse: " + ex)
                            if (isAdded)
                                Toast.makeText(
                                    context!!,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }
                    } else {
                        progressDialog?.dismissDialog()

                        val errorMessage = response.errorBody()!!.string()
                        val errorJson = JSONObject(errorMessage)
                        val message = errorJson.optString("message")
                        if (isAdded)
                            Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG)
                                .show()
                        Log.e("allLeaves", "onResponse: " + message)
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    Log.e("EmployeePerformance", "Server error", t)
                }
            })
        }
        //with start and end date
        if (status.trim() == "--" && startDate != 0L && endDate != 0L) {
            val call = apiInterface.getFilteredLeaveWithoutStatus(userId, startDate, endDate)
            Log.e("allLeaves", "fetchAllLeaves: " + userId + "  " + startDate)
            call.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.isSuccessful) {
                        progressDialog?.dismissDialog()

                        isfiltered = true

                        try {
                            val size = response.body()?.content?.size!!
                            if (size > 0) {
                                leavelist?.clear()

                                for (i in 0..size - 1) {
                                    leavelist?.add(response.body()?.content!!.get(i))
                                }

                                binding.recyclerView.adapter = CommonAdapter(this@LeaveList)
                            } else {
                                if (isAdded)
                                    Toast.makeText(context, "No data found.", Toast.LENGTH_LONG)
                                        .show()
                            }
                            Log.e("allLeaves", "onResponse: " + leavelist?.size)
                            if (leaveType.equals("All")) {
                                filter("All")
                            } else if (leaveType.equals("Half")) {
                                filter("HALF_DAY")
                            } else if (leaveType.equals("Absent")) {
                                filter("ABSENT")
                            } else {
                                if (isAdded)
                                    Toast.makeText(
                                        context,
                                        "Something went wrong.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                            }
                        } catch (ex: Exception) {
                            Log.e("allLeaves", "onResponse: " + ex)
                            if (isAdded)
                                Toast.makeText(
                                    context!!,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }
                    } else {
                        progressDialog?.dismissDialog()

                        val errorMessage = response.errorBody()!!.string()
                        val errorJson = JSONObject(errorMessage)
                        val message = errorJson.optString("message")
                        if (isAdded)
                            Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG)
                                .show()
                        Log.e("allLeaves", "onResponse: " + message)
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    Log.e("EmployeePerformance", "Server error", t)
                }
            })
        }
        //with status and end date
        if (status.trim() != "--" && startDate == 0L && endDate != 0L) {
            val call = apiInterface.getFilteredLeaveWithOutStartDate(userId, status, endDate)
            Log.e("allLeaves", "fetchAllLeaves: " + userId + "  " + startDate)
            call.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.isSuccessful) {
                        progressDialog?.dismissDialog()
                        isfiltered = true

                        try {
                            val size = response.body()?.content?.size!!
                            if (size > 0) {
                                leavelist?.clear()

                                for (i in 0..size - 1) {
                                    leavelist?.add(response.body()?.content!!.get(i))
                                }

                                binding.recyclerView.adapter = CommonAdapter(this@LeaveList)
                            } else {
                                if (isAdded)
                                    Toast.makeText(context, "No data found.", Toast.LENGTH_LONG)
                                        .show()
                            }
                            Log.e("allLeaves", "onResponse: " + leavelist?.size)
                            if (leaveType.equals("All")) {
                                filter("All")
                            } else if (leaveType.equals("Half")) {
                                filter("HALF_DAY")
                            } else if (leaveType.equals("Absent")) {
                                filter("ABSENT")
                            } else {
                                if (isAdded)
                                    Toast.makeText(
                                        context,
                                        "Something went wrong.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                            }
                        } catch (ex: Exception) {
                            Log.e("allLeaves", "onResponse: " + ex)
                            if (isAdded)
                                Toast.makeText(
                                    context!!,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }
                    } else {
                        progressDialog?.dismissDialog()

                        val errorMessage = response.errorBody()!!.string()
                        val errorJson = JSONObject(errorMessage)
                        val message = errorJson.optString("message")
                        if (isAdded)
                            Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG)
                                .show()
                        Log.e("allLeaves", "onResponse: " + message)
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    Log.e("EmployeePerformance", "Server error", t)
                }
            })
        }
        //With start date only.
        if (status.trim() == "--" && startDate != 0L && endDate == 0L) {
            val call = apiInterface.getFilteredLeaveWithStartDate(userId, startDate)
            Log.e("allLeaves", "fetchAllLeaves: " + userId + "  " + startDate)
            call.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {

                    if (response.isSuccessful) {
                        progressDialog?.dismissDialog()
                        isfiltered = true

                        try {
                            val size = response.body()?.content?.size!!
                            if (size > 0) {
                                leavelist?.clear()

                                for (i in 0..size - 1) {
                                    leavelist?.add(response.body()?.content!!.get(i))
                                }

                                binding.recyclerView.adapter = CommonAdapter(this@LeaveList)
                            } else {
                                if (isAdded)
                                    Toast.makeText(context, "No data found.", Toast.LENGTH_LONG)
                                        .show()
                            }
                            Log.e("allLeaves", "onResponse: " + leavelist?.size)
                            if (leaveType.equals("All")) {
                                filter("All")
                            } else if (leaveType.equals("Half")) {
                                filter("HALF_DAY")
                            } else if (leaveType.equals("Absent")) {
                                filter("ABSENT")
                            } else {
                                if (isAdded)
                                    Toast.makeText(
                                        context,
                                        "Something went wrong.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                            }
                        } catch (ex: Exception) {
                            Log.e("allLeaves", "onResponse: " + ex)
                            if (isAdded)
                                Toast.makeText(
                                    context!!,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }
                    } else {
                        progressDialog?.dismissDialog()
                        val errorMessage = response.errorBody()!!.string()
                        val errorJson = JSONObject(errorMessage)
                        val message = errorJson.optString("message")
                        if (isAdded)
                            Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG)
                                .show()
                        Log.e("allLeaves", "onResponse: " + message)
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    Log.e("EmployeePerformance", "Server error", t)
                }
            })
        }
        //With End date Only
        if (status.trim() == "--" && startDate == 0L && endDate != 0L) {
            val call = apiInterface.getFilteredLeaveWithEndDate(userId, endDate)
            Log.e("allLeaves", "fetchAllLeaves: " + userId + "  " + startDate)
            call.enqueue(object : Callback<AllLeaveResponse> {
                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.isSuccessful) {
                        progressDialog?.dismissDialog()
                        try {
                            isfiltered = true

                            val size = response.body()?.content?.size!!
                            if (size > 0) {
                                leavelist?.clear()

                                for (i in 0..size - 1) {
                                    leavelist?.add(response.body()?.content!!.get(i))
                                }

                                binding.recyclerView.adapter = CommonAdapter(this@LeaveList)
                            } else {
                                if (isAdded)
                                    Toast.makeText(context, "No data found.", Toast.LENGTH_LONG)
                                        .show()
                            }
                            Log.e("allLeaves", "onResponse: " + leavelist?.size)
                            if (leaveType.equals("All")) {
                                filter("All")
                            } else if (leaveType.equals("Half")) {
                                filter("HALF_DAY")
                            } else if (leaveType.equals("Absent")) {
                                filter("ABSENT")
                            } else {
                                if (isAdded)
                                    Toast.makeText(
                                        context,
                                        "Something went wrong.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                            }
                        } catch (ex: Exception) {
                            Log.e("allLeaves", "onResponse: " + ex)
                            if (isAdded)
                                Toast.makeText(
                                    context!!,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }
                    } else {
                        progressDialog?.dismissDialog()
                        val errorMessage = response.errorBody()!!.string()
                        val errorJson = JSONObject(errorMessage)
                        val message = errorJson.optString("message")
                        if (isAdded)
                            Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG)
                                .show()
                        Log.e("allLeaves", "onResponse: " + message)
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    Log.e("EmployeePerformance", "Server error", t)
                }
            })
        }
        //with status and startdate
        if (status.trim() != "--" && startDate != 0L && endDate == 0L) {
            val call = apiInterface.getFilteredLeaveWithOutEndDate(userId, status, startDate)
            Log.e("allLeaves", "fetchAllLeaves: " + userId + "  " + startDate)
            call.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.isSuccessful) {
                        progressDialog?.dismissDialog()
                        isfiltered = true

                        try {
                            val size = response.body()?.content?.size!!
                            if (size > 0) {
                                leavelist?.clear()

                                for (i in 0..size - 1) {
                                    leavelist?.add(response.body()?.content!!.get(i))
                                }

                                binding.recyclerView.adapter = CommonAdapter(this@LeaveList)
                            } else {
                                if (isAdded)
                                    Toast.makeText(context, "No data found.", Toast.LENGTH_LONG)
                                        .show()
                            }
                            Log.e("allLeaves", "onResponse: " + leavelist?.size)
                            if (leaveType.equals("All")) {
                                filter("All")
                            } else if (leaveType.equals("Half")) {
                                filter("HALF_DAY")
                            } else if (leaveType.equals("Absent")) {
                                filter("ABSENT")
                            } else {
                                if (isAdded)
                                    Toast.makeText(
                                        context,
                                        "Something went wrong.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()

                            }
                        } catch (ex: Exception) {
                            Log.e("allLeaves", "onResponse: " + ex)
                            if (isAdded)
                                Toast.makeText(
                                    context!!,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }
                    } else {
                        progressDialog?.dismissDialog()

                        val errorMessage = response.errorBody()!!.string()
                        val errorJson = JSONObject(errorMessage)
                        val message = errorJson.optString("message")
                        if (isAdded)
                            Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG)
                                .show()
                        Log.e("allLeaves", "onResponse: " + message)
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    Log.e("EmployeePerformance", "Server error", t)
                }
            })
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showFilterBox(context: Context?) {

        val dialog1 = BottomSheetDialog(context!!, R.style.BottomSheetDialog)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setCancelable(true)
        dialog1.setCanceledOnTouchOutside(true)
        dialog1.setContentView(R.layout.filter_layput)
        val window = dialog1.window
        window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        val status = dialog1.findViewById<Spinner>(R.id.status)
        val showResult = dialog1.findViewById<TextView>(R.id.showresult)

        val list = resources.getStringArray(R.array.statusType2)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.color_spinner_layout, list)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        status!!.setAdapter(arrayAdapter)

        startDate = dialog1.findViewById(R.id.startDate)!!
        endDate = dialog1.findViewById(R.id.endDate)!!

        startDate!!.setOnClickListener { view: View? ->
            openDatePicker("start")
        }
        endDate!!.setOnClickListener { view: View? ->
            openDatePicker("")
        }
        showResult?.setOnClickListener {
            val startdate = DateAndTimeUtility.dateToEpoch(startDate.text.toString())
            Log.e("startdate", "showFilterBox: " + status.selectedItem.toString())

            val enddate = DateAndTimeUtility.dateToEpoch(endDate.text.toString())
            if (startdate == 0L && enddate == 0L && status.selectedItem.toString().trim() == "--")
                Toast.makeText(context, "Select Date or status", Toast.LENGTH_SHORT).show()
            else {
                getFilterLeave(startdate, enddate, status.selectedItem.toString())
                dialog1.dismiss()
            }

        }
        dialog1.show()
    }


    private fun openDatePicker(type: String) {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Select Date")
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection: Long? ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection!!
            val year = calendar[Calendar.YEAR]
            val month = calendar[Calendar.MONTH]
            val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
            val selectedDate =
                dayOfMonth.toString() + "/" + (month + 1) + "/" + year
            if (type == "start") {
                startDate.setText(selectedDate)
            } else endDate.setText(selectedDate)
        }
        picker.show(requireFragmentManager(), picker.toString())

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        TODO("Not yet implemented")
    }

    override fun onPause() {
        super.onPause()
        leavelist?.clear()
    }
}
