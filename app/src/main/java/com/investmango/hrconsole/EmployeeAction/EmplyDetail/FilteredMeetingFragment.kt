package com.investmango.hrconsole.EmployeeAction.EmplyDetail

import android.app.ProgressDialog
import android.content.Context
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.investmango.hrconsole.EmployeeAction.Member_list_Adapter
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FilterMeetingItemBinding
import com.investmango.hrconsole.databinding.FragmentFilteredMeetingBinding
import com.investmango.hrconsole.model.MeetingItem
import com.investmango.hrconsole.model.MeetingListResponse
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.PaginationScrollListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilteredMeetingFragment : Fragment(), RecyclerViewInterface<FilterMeetingItemBinding> {

    private lateinit var binding: FragmentFilteredMeetingBinding
    lateinit var apiInterface: ApiInterface
    var meetingLis: List<MeetingItem?> = arrayListOf()
    var startdate: Long = 0
    var enddate: Long = 0
    var childId: Long = 0
    private var isLoading = false
    private var isLastPage = false
    lateinit var layoutManager: LinearLayoutManager
    private var currentPage = 0
    var status: String = ""
    private var progressDialog: ProgressDialog? = null

    var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        userId = preferences.getLong("userId", 0)

        progressDialog = ProgressDialog(activity, R.style.CustomProgressDialog)
        progressDialog!!.setMessage("Please wait ...")
        progressDialog!!.setCancelable(false)

        if (arguments != null) {
            if (arguments!!.getString("ViewOf") == "child") {
                startdate = arguments!!.getLong("startdate")
                enddate = arguments!!.getLong("enddate")
                childId = arguments!!.getLong("childId")
                status = arguments!!.getString("status")!!

            } else {
                startdate = arguments!!.getLong("startdate")
                enddate = arguments!!.getLong("enddate")
                status = arguments!!.getString("status")!!

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_filtered_meeting, container, false)
        return binding.root
        // Inflate the layout for this fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        if (childId != 0L) {
            userId = childId
        }
        try {
            getMeetings(currentPage)
        } catch (e: Exception) {
            if (isAdded)
                Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
        }
        binding.recyclerForMember.addOnScrollListener(object :
            PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return this@FilteredMeetingFragment.isLastPage
            }

            override fun isLoading(): Boolean {
                return this@FilteredMeetingFragment.isLoading
            }

            override fun loadMoreItems() {
                this@FilteredMeetingFragment.isLoading = true
                currentPage++
                loadMoreData(currentPage)
            }
        })

    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): FilterMeetingItemBinding {
        return FilterMeetingItemBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return meetingLis.size
    }

    override fun bindView(viewBind: FilterMeetingItemBinding, position: Int) {

        viewBind.DateOfMeeting.text =
            DateAndTimeUtility.getDateMonthFromLong(meetingLis.get(position)?.meetingTime)

        viewBind.title.text = meetingLis.get(position)?.purpose
        viewBind.title.text = meetingLis.get(position)?.purpose
        viewBind.TimeOfMeeting.text =
            DateAndTimeUtility.convertEpochToTime(meetingLis.get(position)?.meetingTime)
        viewBind.hostname.text = meetingLis.get(position)?.assignByName
        viewBind.location.text = meetingLis.get(position)?.location
        viewBind.iBttn.setOnClickListener {
            context?.let { it1 -> showMeetingDetails(it1, meetingLis.get(position)) }
        }
    }

    private fun showMeetingDetails(context: Context, item: MeetingItem?) {
        val dialog1 = BottomSheetDialog(context, R.style.BottomSheetDialog)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setCancelable(true)
        dialog1.setCanceledOnTouchOutside(true)
        dialog1.setContentView(R.layout.meeting_detail)
        val window = dialog1.window!!
        window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        val title = dialog1.findViewById<TextView>(R.id.title)
        val dateTime = dialog1.findViewById<TextView>(R.id.dateTime)
        val members = dialog1.findViewById<RecyclerView>(R.id.members)
        val details = dialog1.findViewById<TextView>(R.id.details)


        title?.text = item?.purpose.toString()
        dateTime?.text = DateAndTimeUtility.getDateAndTimeFromLong(item?.createdTime)
        details?.text = item?.description.toString()
        members?.layoutManager = GridLayoutManager(context, 2)
        Log.e("adapter", "showMeetingDetails: " + item?.assignedUsers)
        members?.adapter = item?.assignedUsers?.let { Member_list_Adapter(it) }


        dialog1.show()
    }

    private fun loadMoreData(page: Int) {
        // Simulate network delay
        binding.recyclerForMember.postDelayed({
            // Fetch data from your data source
            try {
                getMeetings(page)
            } catch (e: Exception) {
                if (isAdded)
                    Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }
            isLoading = false
            isLastPage = meetingLis.isEmpty() == true // Assume no more data if newItems is empty
        }, 1500)
    }

    private fun getMeetings(page: Int) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        progressDialog?.show()
        //with only status
        if (startdate == 0L && enddate == 0L && status.trim() != "--") {
            val call: Call<MeetingListResponse>? =
                apiInterface.getFilteredWithstatus(userId, status, page, 10)
            call?.enqueue(object : Callback<MeetingListResponse?> {
                override fun onResponse(
                    call: Call<MeetingListResponse?>,
                    response: Response<MeetingListResponse?>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismiss()

                        meetingLis = response.body()!!.content!!
                        if (!meetingLis.isEmpty())
                            setAdapt()
                        else {
                            binding.noMeeting.visibility = View.VISIBLE
                            binding.recyclerForMember.visibility = View.GONE
                        }

                        Log.e("getmeetings", "onResponse: " + response.body())
                    } else {
                        progressDialog?.dismiss()

                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MeetingListResponse?>, t: Throwable) {
                    progressDialog?.dismiss()

                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }
        //with all three
        if (startdate != 0L && enddate != 0L && status.trim() != "--") {
            val call: Call<MeetingListResponse>? =
                apiInterface.getFilteredOwnMeeting(userId, status, startdate, enddate, page, 10)
            call?.enqueue(object : Callback<MeetingListResponse?> {
                override fun onResponse(
                    call: Call<MeetingListResponse?>,
                    response: Response<MeetingListResponse?>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismiss()

                        meetingLis = response.body()!!.content!!
                        if (!meetingLis.isEmpty())
                            setAdapt()
                        else {
                            binding.noMeeting.visibility = View.VISIBLE
                            binding.recyclerForMember.visibility = View.GONE
                        }

                        Log.e("getmeetings", "onResponse: " + response.body())
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismiss()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MeetingListResponse?>, t: Throwable) {
                    progressDialog?.dismiss()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }

        //with start and end date
        if (status.trim() == "--" && startdate != 0L && enddate != 0L) {
            val call: Call<MeetingListResponse>? =
                apiInterface.getFilteredwithoutStaus(userId, startdate, enddate, page, 10)
            call?.enqueue(object : Callback<MeetingListResponse?> {
                override fun onResponse(
                    call: Call<MeetingListResponse?>,
                    response: Response<MeetingListResponse?>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismiss()

                        meetingLis = response.body()!!.content!!
                        if (!meetingLis.isEmpty())
                            setAdapt()
                        else {
                            binding.noMeeting.visibility = View.VISIBLE
                            binding.recyclerForMember.visibility = View.GONE
                        }

                        Log.e("getmeetings", "onResponse: " + response.body())
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismiss()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MeetingListResponse?>, t: Throwable) {
                    progressDialog?.dismiss()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }

        //with status and end date
        if (status.trim() != "--" && startdate == 0L && enddate != 0L) {
            val call: Call<MeetingListResponse>? =
                apiInterface.getFilteredwithoutStartdate(userId, status, enddate, page, 10)
            call?.enqueue(object : Callback<MeetingListResponse?> {
                override fun onResponse(
                    call: Call<MeetingListResponse?>,
                    response: Response<MeetingListResponse?>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismiss()

                        meetingLis = response.body()!!.content!!
                        if (!meetingLis.isEmpty())
                            setAdapt()
                        else {
                            binding.noMeeting.visibility = View.VISIBLE
                            binding.recyclerForMember.visibility = View.GONE
                        }

                        Log.e("getmeetings", "onResponse: " + response.body())
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismiss()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MeetingListResponse?>, t: Throwable) {
                    progressDialog?.dismiss()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }

        //With start date only.
        if (status.trim() == "--" && startdate != 0L && enddate == 0L) {
            val call: Call<MeetingListResponse>? =
                apiInterface.getFilteredwithStartdate(userId, startdate, page, 10)
            call?.enqueue(object : Callback<MeetingListResponse?> {
                override fun onResponse(
                    call: Call<MeetingListResponse?>,
                    response: Response<MeetingListResponse?>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismiss()

                        meetingLis = response.body()!!.content!!
                        if (!meetingLis.isEmpty())
                            setAdapt()
                        else {
                            binding.noMeeting.visibility = View.VISIBLE
                            binding.recyclerForMember.visibility = View.GONE
                        }

                        Log.e("getmeetings", "onResponse: " + response.body())
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismiss()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MeetingListResponse?>, t: Throwable) {
                    progressDialog?.dismiss()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }

        //With End date Only
        if (status.trim() == "--" && startdate == 0L && enddate != 0L) {
            val call: Call<MeetingListResponse>? =
                apiInterface.getFilteredwithEnddate(userId, enddate, page, 10)
            call?.enqueue(object : Callback<MeetingListResponse?> {
                override fun onResponse(
                    call: Call<MeetingListResponse?>,
                    response: Response<MeetingListResponse?>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismiss()

                        meetingLis = response.body()!!.content!!
                        if (!meetingLis.isEmpty())
                            setAdapt()
                        else {
                            binding.noMeeting.visibility = View.VISIBLE
                            binding.recyclerForMember.visibility = View.GONE
                        }

                        Log.e("getmeetings", "onResponse: " + response.body())
                    } else {

                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismiss()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MeetingListResponse?>, t: Throwable) {
                    progressDialog?.dismiss()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }

        //with status and startdate
        if (status.trim() != "--" && startdate != 0L && enddate == 0L) {
            val call: Call<MeetingListResponse>? =
                apiInterface.getFilteredwithourEnddate(userId, status, startdate, page, 10)
            call?.enqueue(object : Callback<MeetingListResponse?> {
                override fun onResponse(
                    call: Call<MeetingListResponse?>,
                    response: Response<MeetingListResponse?>,
                ) {
                    if (response.body() != null && response.isSuccessful()) {
                        progressDialog?.dismiss()

                        meetingLis = response.body()!!.content!!
                        if (!meetingLis.isEmpty())
                            setAdapt()
                        else {
                            binding.noMeeting.visibility = View.VISIBLE
                            binding.recyclerForMember.visibility = View.GONE
                        }

                        Log.e("getmeetings", "onResponse: " + response.body())
                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog?.dismiss()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MeetingListResponse?>, t: Throwable) {
                    progressDialog?.dismiss()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        }


    }

    private fun setAdapt() {
        binding.noMeeting.visibility = View.GONE
        binding.recyclerForMember.visibility = View.VISIBLE

        binding.recyclerForMember.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerForMember.adapter = CommonAdapter(this)
    }

}