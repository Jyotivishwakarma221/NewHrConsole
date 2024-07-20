package com.investmango.hrconsole.manager.activity

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
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
import com.bumptech.glide.Glide
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment
import com.investmango.hrconsole.EmployeeAction.ManageAssignFragment
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentMessage2Binding
import com.investmango.hrconsole.databinding.MessageRecyclerBinding
import com.investmango.hrconsole.model.MessageResponse
import com.investmango.hrconsole.model.messageItem
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.PaginationScrollListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.util.Calendar

class
MessageFragment : Fragment(), RecyclerViewInterface<MessageRecyclerBinding> {
    private lateinit var layoutManager: LinearLayoutManager
    lateinit var apiInterface: ApiInterface
    var messageItem: ArrayList<messageItem?>? = arrayListOf()
    private lateinit var binding: FragmentMessage2Binding
    var userId: Long = 0
    lateinit var authority: String
    private var isLoading = false
    private var isLastPage = false
    private var currentPage = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        userId = preferences.getLong("userId", 0)
        authority = preferences.getString("Authority", "user").toString()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_message2, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (authority == Constant.USER) {
            binding.addNew.visibility = View.GONE
        }

        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.meassages.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun isLastPage(): Boolean {
                return this@MessageFragment.isLastPage
            }

            override fun isLoading(): Boolean {
                return this@MessageFragment.isLoading
            }

            override fun loadMoreItems() {
                this@MessageFragment.isLoading = true
                currentPage++
                loadMoreData(currentPage)
            }
        })
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        binding.selectMonth.setText(
            resources.getStringArray(R.array.Months).get(month) + " " + year.toString()
        )

        binding.selectMonth.setOnClickListener {
            showMonths()
        }
        binding.addNew.setOnClickListener {
            (activity as ManagerActivity?)!!.replaceFragment(NewMessageFragment())
        }

        binding.backbutton.setOnClickListener { activity?.onBackPressed(); }

        if (isAdded)
        fetchCustomMessages(0)



    }

    private fun loadMoreData(page: Int) {
        // Simulate network delay
        binding.meassages.postDelayed({
            // Fetch data from your data source
            if (isAdded)
            fetchCustomMessages(page)
            isLoading = false
            isLastPage = messageItem?.isEmpty() == true // Assume no more data if newItems is empty
        }, 1500)
    }

    @SuppressLint("NewApi")
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
            binding.selectMonth.setText(
                resources.getStringArray(R.array.Months)
                    .get(monthOfYear) + " " + year
            )

            try{
                val date = "1/" + monthOfYear + "/" + year
//                    val startDate = DateAndTimeUtility.monthYearToEpoch(monthOfYear, year)
            val startDate = DateAndTimeUtility.dateToEpoch(date)

            val endDate = DateAndTimeUtility.getCurrentinMili()
            Log.e(
                "startDate",
                "showMonths: " + startDate + "  " + Instant.now().toEpochMilli()
            )
                getFilterMessage(startDate, endDate)
            } catch (e: Exception) {
                Toast.makeText(context, "something went wrong.", Toast.LENGTH_LONG).show()
                Log.e("showMonths", "showMonths: " + e.message)
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

    fun setAdapter() {
        binding.meassages.adapter = CommonAdapter(this@MessageFragment)

        binding.meassages.layoutManager =
            layoutManager
    }

    private fun fetchCustomMessages(page: Int) {
        try {

            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface

            val call = apiInterface.getNewCustomMessage(userId, page)
            call.enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>,
                ) {
                    if (response.isSuccessful) {
                        try {
                            setAdapter()
                            val size = response.body()?.content?.size!!
                            if (size > 0) {
                                for (i in 0..size - 1) {
                                    messageItem?.add(response.body()?.content!!.get(i))
                                }

                                binding.meassages.adapter?.notifyItemInserted(messageItem?.size!!)
                            } else {
                                Toast.makeText(context, "No messages found.", Toast.LENGTH_LONG)
                                    .show()

                            }
                        } catch (ex: Exception) {
                            Log.e("allLeaves", "onResponse: " + ex)
                            Toast.makeText(context!!, "Something went wrong.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Log.e(
                            "messages",
                            "Failed to fetch messages. Please try again later." + response.errorBody()
                        )
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Log.e(
                        "messages",
                        "Failed to fetch messages. Please check your internet connection." + t.message
                    )
                }
            })
        }catch (e:Exception){
            Log.e("Exception", "fetchCustomMessages: "+e )
        }
    }

    private fun getFilterMessage(StartDate: Long, endDate: Long) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface

        val call = apiInterface.getFilteredMessage(userId, StartDate, endDate)
        call.enqueue(object : Callback<MessageResponse> {
            override fun onResponse(
                call: Call<MessageResponse>,
                response: Response<MessageResponse>,
            ) {
                if (response.isSuccessful) {

                    try {
                        val size = response.body()?.content?.size!!
                        if (size > 0) {
                            messageItem?.clear()

                            for (i in 0..size - 1) {
                                messageItem?.add(response.body()?.content!!.get(i))
                            }

                            setAdapter()
                        } else {
                            Toast.makeText(context, "No data found.", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("messages", "onResponse: " + e.message)
                    }
                } else {
                    Log.e(
                        "messages",
                        "Failed to fetch messages. Please try again later." + response.errorBody()
                    )
                }
            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Log.e(
                    "messages",
                    "Failed to fetch messages. Please check your internet connection." + t.message
                )
            }
        })
    }


    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): MessageRecyclerBinding {
        return MessageRecyclerBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return messageItem?.size!!
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun bindView(viewBind: MessageRecyclerBinding, position: Int) {
        viewBind.Heading.text = messageItem?.get(position)?.purpose
        viewBind.description.text = messageItem?.get(position)?.message
        viewBind.messgFrom.text = messageItem?.get(position)?.assignByName
        if (messageItem?.get(position)?.image != null)
            context?.let {
                Glide.with(it).load(Uri.parse(messageItem?.get(position)?.image))
                    .into(viewBind.profilephoto)
            }

        viewBind.dateTime.text = SimpleDateFormat(
            "dd/MM/yyyy",
            java.util.Locale.US
        ).format(messageItem?.get(position)?.createdDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()

    messageItem?.clear()}
}