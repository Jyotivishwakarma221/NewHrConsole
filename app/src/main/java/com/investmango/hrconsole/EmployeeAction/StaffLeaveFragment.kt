package com.investmango.hrconsole.EmployeeAction

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.bumptech.glide.Glide
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentStaffLeaveBinding
import com.investmango.hrconsole.databinding.LeaveRequestRecyclerBinding
import com.investmango.hrconsole.manager.activity.ManagerActivity
import com.investmango.hrconsole.model.ContentItem
import com.investmango.hrconsole.model.LeaveReqResponse
import com.investmango.hrconsole.model.LeaveRequestUpdateStatus
import com.investmango.hrconsole.service.Constant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StaffLeaveFragment : Fragment(), RecyclerViewInterface<LeaveRequestRecyclerBinding> {
    private lateinit var binding: FragmentStaffLeaveBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var authority: String = ""
    var list: List<ContentItem?> = arrayListOf()
    lateinit var progressDialog: AwesomeProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0").toString()
        userId = preferences.getLong("userId", 0)
        authority = preferences.getString("Authority", "user")!!

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_staff_leave, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize SharedPreferences and retrieve the token and userId


        if (authority.equals(Constant.MANAGER))
            LeaveRequest()
        else if (authority.equals(Constant.ADMIN))
            AllLeaveRequest()

        binding.leaveHistory.setOnClickListener {
            (activity as ManagerActivity?)!!.replaceFragment(LeaveHistoryFragment())

        }
    }

    private fun LeaveRequest() {
        try {
            progressDialog?.showDialog()
            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface

            val call: Call<LeaveReqResponse> =
                apiInterface.getPendingLeaves(userId, true, "PENDING", 0, 10)
            call?.enqueue(object : Callback<LeaveReqResponse> {
                override fun onResponse(
                    call: Call<LeaveReqResponse>,
                    response: Response<LeaveReqResponse>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismissDialog()

                        Log.e(
                            "getLeaves",
                            "onResponse: 1" + response.body()?.content?.size + " " + authority
                        )

                        list = response.body()!!.content!!

                        binding.leaverequest.adapter = CommonAdapter(this@StaffLeaveFragment)
                        binding.leaverequest.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismissDialog()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LeaveReqResponse>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        } catch (e: Exception) {
            Log.d("TAG", "LeaveRequest: " + e)
        }
    }

    private fun AllLeaveRequest() {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        progressDialog?.showDialog()
        val call: Call<LeaveReqResponse> =
            apiInterface.newgetAllPendingLeave()
        call?.enqueue(object : Callback<LeaveReqResponse> {
            override fun onResponse(
                call: Call<LeaveReqResponse>,
                response: Response<LeaveReqResponse>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    progressDialog?.dismissDialog()

                    Log.e(
                        "getLeaves",
                        "onResponse: 2" + response.body()?.content?.size + " " + authority
                    )

                    list = response.body()!!.content!!

                    binding.leaverequest.adapter = CommonAdapter(this@StaffLeaveFragment)
                    binding.leaverequest.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


                } else {
                    Log.e("getmeetings", "onResponse: " + response.body().toString())
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LeaveReqResponse>, t: Throwable) {
                progressDialog?.dismissDialog()
                if (isAdded)
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    private fun approveLeaves(
        selectedStatus: LeaveRequestUpdateStatus.Status,
        leaveId: Long,
        userid: Int,
    ) {

        val requestBody = LeaveRequestUpdateStatus()
        requestBody.id = leaveId

        if (authority.equals(Constant.MANAGER))
            requestBody.managerStatus = selectedStatus
        else if (authority.equals(Constant.ADMIN))
            requestBody.status = selectedStatus
        try {
            progressDialog?.showDialog()

            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface
            Log.e(
                "getLeaves",
                "onResponse:  " + requestBody.id + " " + requestBody.managerStatus + " " + userid.toLong()
            )

            val call = apiInterface.ApproveLeaves(requestBody, userid.toLong())
            Log.e("API Error", "Error in API response: " + userid)


            call.enqueue(object : Callback<Void?> {
                override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                    Log.e("API Response", "Response code: " + response.code())
                    if (response.isSuccessful) {
                        progressDialog?.dismissDialog()

                        LeaveRequest()
                        if (selectedStatus == LeaveRequestUpdateStatus.Status.APPROVED) {
                            Toast.makeText(requireContext(), "Leave approved.", Toast.LENGTH_SHORT)
                                .show()
                        } else if (selectedStatus == LeaveRequestUpdateStatus.Status.REJECTED) {
                            Toast.makeText(activity!!, "Leave rejected.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        progressDialog?.dismissDialog()

                        try {
                            val errorMessage =
                                if (response.errorBody() != null) response.errorBody()!!
                                    .string() else "Unknown error"
                            Log.e("API Error", "Error in API response: $errorMessage")
//                    Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                        } catch (e: IOException) {
                            e.printStackTrace()
//                    Toast.makeText(context, "Error processing request", Toast.LENGTH_SHORT)
//                        .show()
                        }
                    }
                }

                override fun onFailure(call: Call<Void?>, t: Throwable) {
                    Log.e("API Failure", "API Request Failed: " + t.message)
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        Toast.makeText(
                            context,
                            "Network Error. Please try again.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                }
            })
        } catch (e: Exception) {
            Log.d("TAG", "approveLeaves: ", e)
        }
    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): LeaveRequestRecyclerBinding {
        return LeaveRequestRecyclerBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return list.size
    }

    override fun bindView(viewBind: LeaveRequestRecyclerBinding, position: Int) {
        viewBind.name.text = list.get(position)?.userName

        if (list.get(position)?.userprofile != "" && list.get(position)?.userprofile != null) {
            if (isAdded)
                Glide.with(context!!).load(list.get(position)?.userprofile!!)
                    .into(viewBind.profilepic)
            viewBind.profilepic.visibility = View.VISIBLE
            viewBind.initialAvatar.visibility = View.GONE
        } else {
            viewBind.initialAvatar.setName(list.get(position)?.userName!!)
            viewBind.profilepic.visibility = View.GONE
            viewBind.initialAvatar.visibility = View.VISIBLE
        }

        if (!list.get(position)?.reason.equals("string"))
            viewBind.Reason.text =
                list.get(position)?.reason

        if (!list.get(position)?.managerStatus.equals("string"))
            viewBind.managerStatus.text = list.get(position)?.managerStatus

        viewBind.leaveType.text = list.get(position)?.leaveType
        viewBind.duration.text = list.get(position)?.leaveDates?.size.toString() + " Day(s)"
        var date = ""
        for (i in 0..list.get(position)?.leaveDates?.size!! - 1) {
            date = list.get(position)?.leaveDates?.get(i) + " "
        }
        viewBind.leaveDae.text = date
        viewBind.time.text = list.get(position)?.createdDate?.let {
            convertTimestampToReadableFormat(
                it
            )
        }
        viewBind.Reason.setOnClickListener {
            showReasonAlert(list.get(position)?.reason!!)
        }
        viewBind.accept.setOnClickListener {
            list.get(position)?.id?.let { it1 ->
                list.get(position)!!.userId?.let { it2 ->
                    Log.e("Accept", "bindView: " + it1 + " " + it1)
                    approveLeaves(
                        LeaveRequestUpdateStatus.Status.APPROVED,
                        it1.toLong(), it2
                    )
                }
            }
        }
        viewBind.decline.setOnClickListener {
            list.get(position)?.id?.let { it1 ->
                list.get(position)!!.userId?.let { it2 ->
                    approveLeaves(
                        LeaveRequestUpdateStatus.Status.REJECTED,
                        it1.toLong(), it2
                    )
                }
            }
        }
        viewBind.showImg.setOnClickListener {
            if (list.get(position)!!.fileUrl != "" && list.get(position)!!.fileUrl != null) {
                try {
                    val urlIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(list.get(position)!!.fileUrl)
                    )
                    startActivity(urlIntent)
                } catch (e: java.lang.Exception) {
                    Toast.makeText(context, "Try again Later.", Toast.LENGTH_SHORT).show()
                    Log.e("Exception", "onClick: $e")
                }
            }
        }

        if (list.get(position)!!.fileUrl != "" && list.get(position)!!.fileUrl != null) {
            viewBind.showImg.setVisibility(View.VISIBLE)
        } else viewBind.showImg.setVisibility(View.GONE)
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

    private fun convertTimestampToReadableFormat(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}