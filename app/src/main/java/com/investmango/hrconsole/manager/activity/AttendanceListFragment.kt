package com.investmango.hrconsole.manager.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.AttendanceListRecycBinding
import com.investmango.hrconsole.databinding.FragmentAttendanceListBinding
import com.investmango.hrconsole.model.AttendanceResponse
import com.investmango.hrconsole.model.attendanceDay
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
import kotlin.math.log


class AttendanceListFragment : Fragment(), RecyclerViewInterface<AttendanceListRecycBinding> {

    private lateinit var binding: FragmentAttendanceListBinding
    lateinit var layoutManager: LinearLayoutManager
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    private var isLoading = false
    private var isLastPage = false
    private var currentPage = 0
    private var list: ArrayList<attendanceDay> = arrayListOf()
    var isFiltered: Boolean = false
    lateinit var progressDialog: AwesomeProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0").toString()
        userId = preferences.getLong("userId", 0)

        progressDialog = AwesomeProgressDialog(requireContext())
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)

        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_attendance_list, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        DateAndTimeUtility.getDate()
        binding.monthYear.setText(
            resources.getStringArray(R.array.Months).get(month) + " " + year.toString()
        )


        binding.monthYear.setOnClickListener {
            showMonths()
        }
        binding.backbutton.setOnClickListener { activity?.onBackPressed(); }

        attendanceList(0)
        setAdapt()
        binding.recycler.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return this@AttendanceListFragment.isLastPage
            }

            override fun isLoading(): Boolean {
                return this@AttendanceListFragment.isLoading
            }

            override fun loadMoreItems() {
                this@AttendanceListFragment.isLoading = true
                currentPage++
                if (!isFiltered)
                    loadMoreData(currentPage)
            }
        })

    }

    private fun loadMoreData(page: Int) {
        // Simulate network delay
        binding.recycler.postDelayed({
            // Fetch data from your data source

            attendanceList(page)
            isLoading = false
//            isLastPage = leavelist?.isEmpty() == true // Assume no more data if newItems is empty
        }, 1500)
    }

    fun attendanceList(page: Int) {
        try {
            progressDialog.showDialog()
            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface
            val call = apiInterface.getAttendance(userId, page)
            call.enqueue(object : Callback<AttendanceResponse> {
                override fun onResponse(
                    call: Call<AttendanceResponse>,
                    response: Response<AttendanceResponse>,
                ) {
                    if (response.isSuccessful && response.body() != null && progressDialog != null) {
                        try {
                            if (isAdded) // check isAdded to ensure fragment is still added
                                progressDialog.dismissDialog()
                        }catch (e:Exception){
                            Log.e("Exception", "onResponse: "+ e.message)
                        }
                        val content = response.body()?.content?.filterNotNull() ?: emptyList()
                        if (content.isNotEmpty()) {
                            list.addAll(content)  // Add all new items to the list
                            binding.recycler.adapter?.notifyDataSetChanged()  // Notify adapter of data change
//                            for (i in 0..size - 1) {
//                                response.body()?.content?.get(i)?.let { list?.add(it) }
//                            }
//                            Log.e("attendance", "onResponse: " + list)
//
////                            binding.recycler.adapter?.notifyItemInserted(list.size)
//                            binding.recycler.adapter?.notifyDataSetChanged()
                        } else {
                            try {
                                val errorMessage = response.errorBody()!!.string()
                                Log.e(
                                    "attendance",
                                    "API call not successful. Error Message: $errorMessage"
                                )
                                // Extract the message from the JSON response
                                val errorJson = JSONObject(errorMessage)
                                val message = errorJson.optString("message")
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            } catch (e: IOException) {
                                e.printStackTrace()
                                Log.e("attendance", "Error parsing error response: " + e.message)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Log.e("attendance", "Error parsing error response: " + e.message)
                            }
                        }
                    } else {

                        if (isAdded) {
                            progressDialog.dismissDialog()
                            Toast.makeText(
                                requireContext(),
                                "Something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<AttendanceResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    Log.e("attendance", "onFailure: ")
                    Toast.makeText(
                        context,
                        "Server error " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUsernewAttendancebyMonth(startDate: Long, endDate: Long) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()
        val call = apiInterface.getUsernewAttendancebyMonth(userId, startDate, endDate, 30)
        call.enqueue(object : Callback<AttendanceResponse> {
            override fun onResponse(
                call: Call<AttendanceResponse>,
                response: Response<AttendanceResponse>,
            ) {

                progressDialog?.dismissDialog()
                if (response.isSuccessful && response.body() != null) {
                    isFiltered = true
                    val size = response.body()?.content?.size!!
                    Log.e("attendance", "onResponse: " + size)
                    list.clear()
                    if (size > 0) {
                        binding.noDataFound.visibility = View.GONE
                        binding.recycler.visibility = View.VISIBLE
                        for (i in 0..size - 1) {
                            response.body()?.content?.get(i)?.let { list?.add(it) }
                        }
                        binding.recycler.adapter = CommonAdapter(this@AttendanceListFragment)
                    } else {
                        binding.noDataFound.visibility = View.VISIBLE
                        binding.recycler.visibility = View.GONE
                    }

                } else {
                    progressDialog.dismissDialog()
                    try {
                        val errorMessage = response.errorBody()!!.string()
                        Log.e(
                            "attendance",
                            "API call not successful. Error Message: "
                        )
                        // Extract the message from the JSON response
                        val errorJson = JSONObject(errorMessage)
                        val message = errorJson.optString("message")
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e("attendance", "Error parsing error response: " + e.message)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.e("attendance", "Error parsing error response: " + e.message)
                    }
                }

            }

            override fun onFailure(call: Call<AttendanceResponse>, t: Throwable) {
                progressDialog?.dismissDialog()
                Log.e("attendance", "onFailure: ")
                Toast.makeText(
                    context,
                    "Server error " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

        })

    }


    fun setAdapt() {
        try {
            binding.recycler.adapter = CommonAdapter(this)
            binding.recycler.layoutManager = layoutManager
        }catch (e:Exception){
            Log.e("Exception", "setAdapt: "+ e.message)
        }

    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    fun showMonths() {
        val yearSelected: Int
        var monthSelected: Int
        //Set default values
        val calendar = Calendar.getInstance()
        yearSelected = calendar[Calendar.YEAR]
        monthSelected = calendar[Calendar.MONTH]

        val dialogFragment = MonthYearPickerDialogFragment
            .getInstance(monthSelected, yearSelected)

        fragmentManager?.let {
            dialogFragment.show(it, null)
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
                val monthSelectedd = monthOfYear + 1

                val yearMonth: YearMonth = YearMonth.of(year, monthSelectedd)
                val endOfMonth: LocalDate = yearMonth.atEndOfMonth()
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val formattedDate = endOfMonth.format(formatter)
                try {

                    val date = "1/" + monthSelectedd + "/" + year
//                    val startDate = DateAndTimeUtility.monthYearToEpoch(monthOfYear, year)
                    val startDate = DateAndTimeUtility.dateToEpoch(date)


                    val endDate = DateAndTimeUtility.convertToEpochMillis(formattedDate)
                    Log.e(
                        "startDate",
                        "showMonths: " + startDate + "  " + endDate + " " + formattedDate
                    )
                    getUsernewAttendancebyMonth(startDate, endDate)
                } catch (e: Exception) {
                    Toast.makeText(context, "something went wrong.", Toast.LENGTH_LONG).show()
                    Log.e("showMonths", "showMonths: " + e.message)
                }
            }
        }

    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): AttendanceListRecycBinding {
        return AttendanceListRecycBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun bindView(viewBind: AttendanceListRecycBinding, position: Int) {
       val attendanceDay =list.get(position)
        if (attendanceDay.late != null) {
            if (attendanceDay.late == true) {
                viewBind.blueBg.setBackgroundResource(R.drawable.red_one_sde)
            } else viewBind.blueBg.setBackgroundResource(R.drawable.blue_one_side)
        }

        if (attendanceDay.inTime != null && attendanceDay.inTime!=0L ) {
            Log.e(
                "intime",
                "bindView: 1 " + attendanceDay.inTime + " " + attendanceDay.date.toString() + " " + attendanceDay.dayType
            )

            viewBind.date.text = attendanceDay.date.toString()
            viewBind.Absent.visibility = View.GONE
            viewBind.present.visibility = View.VISIBLE
            val intime = DateAndTimeUtility.getTimeInHourFromLong(attendanceDay.inTime)
            viewBind.inTime.text = intime

            if (attendanceDay.outTime != 0L) {
                val outtime = DateAndTimeUtility.getTimeInHourFromLong(attendanceDay.outTime)
                viewBind.outTime.text = outtime
                Log.e("inTime", "bindView: out" + outtime)
            }else{
                viewBind.outTime.text=" "
            }
            if (attendanceDay.leaveType != null) {
                viewBind.leavetypehalfDay.visibility = View.VISIBLE
                viewBind.Absent.visibility = View.GONE
                viewBind.leavetypehalfDay.text = "  " + attendanceDay.leaveType
            } else {
                viewBind.leavetypehalfDay.visibility = View.GONE
                viewBind.Absent.visibility = View.GONE
            }

        } else if (attendanceDay.inTime == null && attendanceDay.outTime == null) {
            viewBind.date.text = attendanceDay.date.toString()
            viewBind.present.visibility = View.GONE

//            Log.e(
//                "intime",
//                "bindView: 2 " + attendanceDay.inTime + " " + list[position].date.toString() + " " + attendanceDay.dayType
//            )

            if (attendanceDay.dayType != null) {
                viewBind.present.visibility = View.GONE
                viewBind.Absent.visibility = View.VISIBLE
                viewBind.leavetypehalfDay.visibility = View.GONE
                viewBind.Absent.text = attendanceDay.dayType

            }
            else if (attendanceDay.leaveType != null) {
                viewBind.leavetypehalfDay.visibility = View.VISIBLE
                viewBind.Absent.visibility = View.GONE
                viewBind.leavetypehalfDay.text = "  " + attendanceDay.leaveType
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        list.clear()
    }
}