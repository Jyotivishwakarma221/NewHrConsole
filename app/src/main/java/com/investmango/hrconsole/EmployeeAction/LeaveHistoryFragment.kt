package com.investmango.hrconsole.EmployeeAction

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
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
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentLeaveHistoryBinding
import com.investmango.hrconsole.databinding.LeaveHistroyRecyBinding
import com.investmango.hrconsole.model.AllLeaveResponse
import com.investmango.hrconsole.model.ContentItem
import com.investmango.hrconsole.model.LeaveItem
import com.investmango.hrconsole.model.LeaveReqResponse
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.PaginationScrollListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class LeaveHistoryFragment : Fragment(), RecyclerViewInterface<LeaveHistroyRecyBinding> {
    private lateinit var binding: FragmentLeaveHistoryBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var authority: String = ""
    private var isLoading = false
    var childuserId: Long = 0
    var isfiltered = false
    lateinit var startDate: TextView
    lateinit var endDate: TextView
    var startdate: Long = 0
    var enddate: Long = 0
    var status: String = "--"
    var employeList: ArrayList<String>? = arrayListOf()
    var allActiveUsers: List<TotalEmpResponseItem?>? = null
    private var isLastPage = false
    lateinit var progressDialog: AwesomeProgressDialog
    private var currentPage = 0
    var list: ArrayList<ContentItem?> = arrayListOf()
    var Filteredlist: ArrayList<LeaveItem?> = arrayListOf()
    lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0").toString()
        authority = preferences.getString("Authority", "")!!
        userId = preferences.getLong("userId", 0)



        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)

        if (authority.equals(Constant.MANAGER))
            getChildActiveUser()
        else if (authority.equals(Constant.ADMIN))
            getAllActiveUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_leave_history, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize SharedPreferences and retrieve the token and userId


        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.leaverequest.layoutManager = layoutManager

        binding.leaverequest.adapter = CommonAdapter(this@LeaveHistoryFragment)


        if (authority.equals(Constant.MANAGER))
            leaveHistory(currentPage)
        else if (authority.equals(Constant.ADMIN))
            AdminleaveHistory(currentPage)

        binding.filter.setOnClickListener {
            showManagerOrAdminFilter(context!!)
        }


        binding.leaverequest.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return this@LeaveHistoryFragment.isLastPage
            }

            override fun isLoading(): Boolean {
                return this@LeaveHistoryFragment.isLoading
            }

            override fun loadMoreItems() {
                this@LeaveHistoryFragment.isLoading = true
                currentPage++
                if (!isfiltered)
                    loadMoreData(currentPage)
                else leaveHistory(startdate, enddate, currentPage, status)

            }
        })


    }

    private fun loadMoreData(page: Int) {
        // Simulate network delay
        binding.leaverequest.postDelayed({
            // Fetch data from your data source

            if (authority.equals(Constant.MANAGER))
                leaveHistory(page)
            else if (authority.equals(Constant.ADMIN))
                AdminleaveHistory(page)

            isLoading = false
            isLastPage = list.isEmpty() == true // Assume no more data if newItems is empty
        }, 100)
    }

    private fun leaveHistory(page: Int) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        progressDialog?.showDialog()
        val call: Call<LeaveReqResponse> =
            apiInterface.getLeaves(userId, true, page, 10)
        call?.enqueue(object : Callback<LeaveReqResponse> {
            override fun onResponse(
                call: Call<LeaveReqResponse>,
                response: Response<LeaveReqResponse>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    progressDialog?.dismissDialog()
                    Log.e("getLeaves", "onResponse: " + response.body()?.content?.size)

                    val listt = response.body()!!.content!!
                    if (listt.size == 0) {
                        binding.noDataFound.visibility = View.VISIBLE
                        binding.leaverequest.visibility = View.GONE
                    } else {
                        binding.noDataFound.visibility = View.GONE
                        binding.leaverequest.visibility = View.VISIBLE

                        for (i in 0..listt.size - 1) {
                            list.add(listt.get(i))
                        }

                        binding.leaverequest.adapter?.notifyItemInserted(list.size)
                    }
                } else {
                    progressDialog?.dismissDialog()

                    Log.e("getmeetings", "onResponse: " + response.body().toString())

                    Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LeaveReqResponse>, t: Throwable) {
                progressDialog?.dismissDialog()
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    private fun AdminleaveHistory(page: Int) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        progressDialog?.showDialog()
        val call: Call<LeaveReqResponse> =
            apiInterface.getFilteredLeave(page, 10)
        call?.enqueue(object : Callback<LeaveReqResponse> {
            override fun onResponse(
                call: Call<LeaveReqResponse>,
                response: Response<LeaveReqResponse>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    progressDialog?.dismissDialog()
                    Log.e("getLeaves", "onResponse: " + response.body()?.content?.size)

                    val listt = response.body()!!.content!!
                    if (listt.size == 0) {
                        binding.noDataFound.visibility = View.VISIBLE
                        binding.leaverequest.visibility = View.GONE
                    } else {
                        binding.noDataFound.visibility = View.GONE
                        binding.leaverequest.visibility = View.VISIBLE

                        for (i in 0..listt.size - 1) {
                            list.add(listt.get(i))
                        }

                        binding.leaverequest.adapter?.notifyItemInserted(list.size)
                    }
                } else {
                    progressDialog?.dismissDialog()

                    Log.e("getmeetings", "onResponse: " + response.body().toString())

                    Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LeaveReqResponse>, t: Throwable) {
                progressDialog?.dismissDialog()
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    private fun leaveHistory(startDate: Long, enddate: Long, page: Int, status: String) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()

        //with status Only
        if (startDate == 0L && enddate == 0L && status.trim() != "--") {
            val call: Call<AllLeaveResponse>? =
                apiInterface.getFilteredLeaveWithoutDate(childuserId, status, page)
            call?.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog.dismissDialog()

                        Log.e("getLeaves", "onResponse: " + response.body()?.content?.size)
                        isfiltered = true

                        val Filteredlist2 = response.body()!!.content!!
                        for (i in 0..Filteredlist2.size - 1) {
                            Filteredlist.add(Filteredlist2.get(i))
                        }
                        list.clear()
                        if (Filteredlist.isEmpty()) {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.leaverequest.visibility = View.GONE
                        } else {
                            binding.noDataFound.visibility = View.GONE
                            binding.leaverequest.visibility = View.VISIBLE
                            binding.leaverequest.adapter = CommonAdapter(this@LeaveHistoryFragment)
                        }
                    } else {
                        progressDialog.dismissDialog()

                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    progressDialog.dismissDialog()

                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }

        //with only CHILD
        if (startDate == 0L && enddate == 0L && status.trim() == "--") {
            val call: Call<AllLeaveResponse>? =
                apiInterface.getFilteredLeaveWithChild(childuserId, page)
            call?.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismissDialog()

                        Log.e("getLeaves", "onResponse: " + response.body()?.content?.size)
                        isfiltered = true

                        val Filteredlist2 = response.body()!!.content!!
                        for (i in 0..Filteredlist2.size - 1) {
                            Filteredlist.add(Filteredlist2.get(i))
                        }
                        list.clear()
                        if (Filteredlist.isEmpty()) {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.leaverequest.visibility = View.GONE
                        } else {
                            binding.noDataFound.visibility = View.GONE
                            binding.leaverequest.visibility = View.VISIBLE
                            binding.leaverequest.adapter = CommonAdapter(this@LeaveHistoryFragment)
                        }
                    } else {
                        progressDialog.dismissDialog()

                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    progressDialog?.dismissDialog()

                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }

        //with all three
        if (startDate != 0L && enddate != 0L && status.trim() != "--") {
            val call: Call<AllLeaveResponse>? =
                apiInterface.getFilteredLeave(childuserId, startDate, enddate, status, page)
            call?.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismissDialog()

                        isfiltered = true

                        Log.e("getLeaves", "onResponse: " + response.body()?.content?.size)

                        val Filteredlist2 = response.body()!!.content!!
                        for (i in 0..Filteredlist2.size - 1) {
                            Filteredlist.add(Filteredlist2.get(i))
                        }
                        list.clear()
                        if (Filteredlist.isEmpty()) {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.leaverequest.visibility = View.GONE
                        } else {
                            binding.noDataFound.visibility = View.GONE
                            binding.leaverequest.visibility = View.VISIBLE
                            binding.leaverequest.adapter = CommonAdapter(this@LeaveHistoryFragment)
                        }
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismissDialog()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }

        //with status and end date
        if (status.trim() != "--" && startDate == 0L && enddate != 0L) {
            val call: Call<AllLeaveResponse>? =
                apiInterface.getFilteredLeaveWithOutStartDate(childuserId, status, enddate, page)
            call?.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        Log.e("getLeaves", "onResponse: " + response.body()?.content?.size)
                        progressDialog?.dismissDialog()
                        isfiltered = true

                        val Filteredlist2 = response.body()!!.content!!
                        for (i in 0..Filteredlist2.size - 1) {
                            Filteredlist.add(Filteredlist2[i])
                        }
                        list.clear ()
                        if (Filteredlist.isEmpty()) {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.leaverequest.visibility = View.GONE
                        } else {
                            binding.noDataFound.visibility = View.GONE
                            binding.leaverequest.visibility = View.VISIBLE
                            binding.leaverequest.adapter = CommonAdapter(this@LeaveHistoryFragment)
                        }
                    } else {
                        progressDialog?.dismissDialog()

                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }

        //with status and start date
        if (status.trim() != "--" && startDate != 0L && enddate == 0L) {
            val call: Call<AllLeaveResponse>? =
                apiInterface.getFilteredLeaveWithOutEndDate(childuserId, status, startDate, page)
            call?.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismissDialog()
                        isfiltered = true

                        Log.e("getLeaves", "onResponse: " + childuserId)

                        val Filteredlist2 = response.body()!!.content!!
                        for (i in 0..Filteredlist2.size - 1) {
                            Filteredlist.add(Filteredlist2.get(i))
                        }
                        list.clear()
                        if (Filteredlist.isEmpty()) {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.leaverequest.visibility = View.GONE
                        } else {
                            binding.noDataFound.visibility = View.GONE
                            binding.leaverequest.visibility = View.VISIBLE
                            binding.leaverequest.adapter = CommonAdapter(this@LeaveHistoryFragment)
                        }
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismissDialog()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })

        }

        //with start and end date
        if (status.trim() == "--" && startDate != 0L && enddate != 0L) {
            val call: Call<AllLeaveResponse>? =
                apiInterface.getFilteredLeaveWithoutStatus(childuserId, startDate, enddate, page)
            call?.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismissDialog()
                        isfiltered = true

                        Log.e("getLeaves", "onResponse: " + childuserId)

                        val Filteredlist2 = response.body()!!.content!!
                        for (i in 0..Filteredlist2.size - 1) {
                            Filteredlist.add(Filteredlist2.get(i))
                        }
                        list.clear()
                        if (Filteredlist.isEmpty()) {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.leaverequest.visibility = View.GONE
                        } else {
                            binding.noDataFound.visibility = View.GONE
                            binding.leaverequest.visibility = View.VISIBLE
                            binding.leaverequest.adapter = CommonAdapter(this@LeaveHistoryFragment)
                        }
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismissDialog()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })

        }

        //with startDate
        if (status.trim() == "--" && startDate != 0L && enddate == 0L) {
            val call: Call<AllLeaveResponse>? =
                apiInterface.getFilteredLeaveWithStartDate(childuserId, startDate, page)
            call?.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismissDialog()
                        isfiltered = true

                        Log.e("getLeaves", "onResponse: " + childuserId)

                        val Filteredlist2 = response.body()!!.content!!
                        for (i in 0..Filteredlist2.size - 1) {
                            Filteredlist.add(Filteredlist2.get(i))
                        }
                        list.clear()
                        if (Filteredlist.isEmpty()) {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.leaverequest.visibility = View.GONE
                        } else {
                            binding.noDataFound.visibility = View.GONE
                            binding.leaverequest.visibility = View.VISIBLE
                            binding.leaverequest.adapter = CommonAdapter(this@LeaveHistoryFragment)
                        }
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismissDialog()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })

        }

        //with endDate
        if (status.trim() == "--" && startDate == 0L && enddate != 0L) {
            val call: Call<AllLeaveResponse>? =
                apiInterface.getFilteredLeaveWithEndDate(childuserId, enddate, page)
            call?.enqueue(object : Callback<AllLeaveResponse> {
                override fun onResponse(
                    call: Call<AllLeaveResponse>,
                    response: Response<AllLeaveResponse>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismissDialog()
                        isfiltered = true

                        Log.e("getLeaves", "onResponse: " + childuserId)

                        val Filteredlist2 = response.body()!!.content!!
                        for (i in 0..Filteredlist2.size - 1) {
                            Filteredlist.add(Filteredlist2.get(i))
                        }
                        list.clear()
                        if (Filteredlist.isEmpty()) {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.leaverequest.visibility = View.GONE
                        } else {
                            binding.noDataFound.visibility = View.GONE
                            binding.leaverequest.visibility = View.VISIBLE
                            binding.leaverequest.adapter = CommonAdapter(this@LeaveHistoryFragment)
                        }
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismissDialog()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AllLeaveResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })

        }
    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): LeaveHistroyRecyBinding {
        return LeaveHistroyRecyBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        if (list != null && list.isNotEmpty())
            return list.size
        else
            return Filteredlist.size
    }

    override fun bindView(viewBind: LeaveHistroyRecyBinding, position: Int) {
//        if (list.get(position)?.comment!=null)
        if (!list.isEmpty()) {

            viewBind.comment.text = list.get(position)?.leaveType.toString()
            viewBind.name.text = list.get(position)?.userName

            if (!list.get(position)?.reason.equals("string"))
                viewBind.Reason.text = list.get(position)?.reason

            if (list.get(position)?.status == "PENDING" || list.get(position)?.status == "REJECTED") {
                viewBind.pendingOrReject.text = list.get(position)?.status
                viewBind.pendingOrReject.visibility = View.VISIBLE
                viewBind.approveLay.visibility = View.GONE
            } else {

                viewBind.approvedBy.text = list.get(position)?.approvedByName.toString()
                viewBind.pendingOrReject.visibility = View.GONE
                viewBind.approveLay.visibility = View.VISIBLE
            }


            var size = list.get(position)?.leaveDates?.size!! - 1
            Log.e("leaveSize", "bindView: " + list.get(position)?.leaveDates?.size + " " + size)

            if (size > 1)
                viewBind.duration.text =
                    list.get(position)?.leaveDates?.get(0) + " - " + list.get(position)?.leaveDates?.get(
                        size
                    ).toString()
            else viewBind.duration.text = list.get(position)?.leaveDates?.get(0)

            viewBind.Reason.setOnClickListener {
                showReasonAlert(list.get(position)?.reason!!)
            }
        } else if (Filteredlist.isNotEmpty()) {
            viewBind.comment.text = Filteredlist.get(position)?.leaveType.toString()
            viewBind.name.text = Filteredlist.get(position)?.userName

            if (!Filteredlist.get(position)?.reason.equals("string"))
                viewBind.Reason.text = Filteredlist.get(position)?.reason

            if (Filteredlist.get(position)?.status == "PENDING" || Filteredlist.get(position)?.status == "REJECTED") {
                viewBind.pendingOrReject.text = Filteredlist.get(position)?.status
                viewBind.pendingOrReject.visibility = View.VISIBLE
                viewBind.approveLay.visibility = View.GONE
            } else {

                viewBind.approvedBy.text = Filteredlist.get(position)?.approvedByName.toString()
                viewBind.pendingOrReject.visibility = View.GONE
                viewBind.approveLay.visibility = View.VISIBLE
            }


            var size = Filteredlist.get(position)?.leaveDates?.size!!
            Log.e(
                "leaveSize",
                "  leave bindView: " + Filteredlist.get(position)?.leaveDates?.size + " " + size
            )
            if (size <= 1) {
                viewBind.duration.text = Filteredlist.get(position)?.leaveDates?.get(0)
                Log.e("leaveSize", "  leave  " + Filteredlist.get(position)?.leaveDates?.get(0))

            } else {
                viewBind.duration.text =
                    Filteredlist.get(position)?.leaveDates?.get(0) + " - " + Filteredlist.get(
                        position
                    )?.leaveDates?.get(size - 1)
            }

            viewBind.Reason.setOnClickListener {
                showReasonAlert(Filteredlist.get(position)?.reason!!)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun showManagerOrAdminFilter(context: Context) {
        val dialog1 = BottomSheetDialog(context, R.style.BottomSheetDialog)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setCancelable(true)
        dialog1.setCanceledOnTouchOutside(true)
        dialog1.setContentView(R.layout.manger_admin_filter)
        val window = dialog1.window!!
        window.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        val statusSpin = dialog1.findViewById<Spinner>(R.id.status)
        val statusText = dialog1.findViewById<TextView>(R.id.statusText)
        val childSpinner = dialog1.findViewById<Spinner>(R.id.personal)
        val showResult = dialog1.findViewById<TextView>(R.id.showresult)


        val list2 = resources.getStringArray(R.array.statusType2)

        val arrayAdapter =
            ArrayAdapter<Any?>(requireContext(), R.layout.color_spinner_layout, list2)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        assert(statusSpin != null)
        statusSpin!!.adapter = arrayAdapter

//        statusSpin.visibility = View.GONE

        val arrayAdapter2 = ArrayAdapter(
            requireContext(),
            R.layout.color_spinner_layout,
            employeList as MutableList<String>
        )
        Log.e("employeList", "showManagerOrAdminFilter: " + employeList)
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
                Log.e("getLeaves", "onResponse: " + childuserId)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        startDate = dialog1.findViewById(R.id.startDate)!!
        endDate = dialog1.findViewById(R.id.endDate)!!

        startDate.setOnClickListener { openDatePicker("start") }
        endDate.setOnClickListener { openDatePicker("") }

        showResult!!.setOnClickListener {
            startdate = DateAndTimeUtility.dateToEpoch(startDate.text.toString())
            Log.e("startdate", "showFilterBox: $childuserId")
            status = statusSpin.selectedItem.toString()
            enddate = DateAndTimeUtility.dateToEpoch(endDate.text.toString())

//            if (startdate == 0L && enddate == 0L)
//                Toast.makeText(context, "Select Date or status", Toast.LENGTH_SHORT).show()
//            else {
            Filteredlist.clear()
            leaveHistory(startdate, enddate, 0, status)
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
                startDate.setText(selectedDate)
            } else endDate.setText(selectedDate)
        }
        picker.show(requireFragmentManager(), picker.toString())

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
    fun showReasonAlert(rsn: String) {
        // Create an alert builder
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)

        // set the custom layout
        val customLayout: View = layoutInflater.inflate(R.layout.custom_progress2, null)
        builder.setView(customLayout)

        val reasonTxt = customLayout.findViewById<TextView>(R.id.Reason)
        val okBtn = customLayout.findViewById<TextView>(R.id.ok_btn)
        reasonTxt.setText(rsn)
        reasonTxt.movementMethod = ScrollingMovementMethod()

        val dialog = builder.create()
        okBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


}