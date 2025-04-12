package com.investmango.hrconsole.EmployeeAction

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.AttendanceListRecycBinding
import com.investmango.hrconsole.databinding.FragmentBreakListBinding
import com.investmango.hrconsole.databinding.FragmentMeetingBreakBinding
import com.investmango.hrconsole.databinding.MeetBreaksBinding
import com.investmango.hrconsole.model.BreakMeetItemItem
import com.investmango.hrconsole.model.BreakMeeting
import com.investmango.hrconsole.model.BreaksListResponse
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.model.breakItem
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.PaginationScrollListener
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Locale

class MeetingBreakFragment : Fragment(), RecyclerViewInterface<MeetBreaksBinding> {
    private lateinit var binding: FragmentMeetingBreakBinding
    lateinit var layoutManager: LinearLayoutManager
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var authority: String? = null
    var token: String = ""
    private var isLoading = false
    private var isLastPage = false
    private var currentPage = 0
    private var list: ArrayList<BreakMeetItemItem> = arrayListOf()
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

        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (authority == Constant.MANAGER) {
            employeList = getChildActiveUser()
        } else if (authority == Constant.ADMIN) {
            employeList = getAllActiveUser()
        }
        BreakList(0)
        setAdapt()
        binding.recycler.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return this@MeetingBreakFragment.isLastPage
            }

            override fun isLoading(): Boolean {
                return this@MeetingBreakFragment.isLoading
            }

            override fun loadMoreItems() {
                this@MeetingBreakFragment.isLoading = true
                currentPage++
//                if (!isFiltered)
                loadMoreData(currentPage)
//                else HandleFiltered(currentPage)
            }
        })

        binding.backbutton.setOnClickListener { activity?.onBackPressed(); }


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_meeting_break, container, false)
        return binding.root
    }

    fun setAdapt() {
        try {
            binding.recycler.adapter = CommonAdapter(this)
            binding.recycler.layoutManager = layoutManager
        } catch (e: Exception) {
            Log.e("Exception", "setAdapt: " + e.message)
        }
    }

    private fun loadMoreData(page: Int) {
        // Simulate network delay
        binding.recycler.postDelayed({
            BreakList(page)
            isLoading = false
        }, 1500)
    }

    private fun BreakList(page: Int) {

        try {
            progressDialog.showDialog()
            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface
            val user = childuserId.takeIf { it.toInt() != 0 } ?: userId.toInt()

            val call = apiInterface.getMeetingBreak(page, user.toInt(), 31)
            call.enqueue(object : Callback<BreakMeeting> {
                override fun onResponse(
                    call: Call<BreakMeeting>,
                    response: Response<BreakMeeting>,
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        try {
                            if (isAdded) // check isAdded to ensure fragment is still added
                                progressDialog.dismissDialog()
                        } catch (e: Exception) {
                            Log.e("Exception", "onResponse: " + e.message)
                        }
                        val content = response.body()?.breakMeetItem?.filterNotNull() ?: emptyList()
                        if (content!!.isNotEmpty()) {
                            binding.noDataFound.visibility = View.GONE
                            binding.recycler.visibility = View.VISIBLE

                            list.addAll(content)  // Add all new items to the list
                            binding.recycler.adapter?.notifyDataSetChanged()  // Notify adapter of data change
                        } else {
                            binding.noDataFound.visibility = View.VISIBLE
                            binding.recycler.visibility = View.GONE
                            try {
                                Toast.makeText(
                                    requireContext(),
                                    "No Data Found.",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                                "No Data Found.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<BreakMeeting>, t: Throwable) {
                    progressDialog.dismissDialog()
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

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): MeetBreaksBinding {
        return MeetBreaksBinding.inflate(layoutInflater, viewGroup, false)

    }

    override fun getListCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun bindView(viewBind: MeetBreaksBinding, position: Int) {
    viewBind.date.text=DateAndTimeUtility.getDATEFromLong(list.get(position).createdDate)
    viewBind.source.text=list.get(position).source
    viewBind.feedback.text=list.get(position).feedback
    viewBind.ChannelPartName.text=list.get(position).channelPartnerName

        if (list.get(position).clientPhone!="" || list.get(position).clientPhone!=null){
            viewBind.phoneNum.text=list.get(position).clientPhone
        }else{
            viewBind.phoneNum.text=""
        }
    }


}