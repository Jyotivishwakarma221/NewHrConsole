package com.investmango.hrconsole.EmployeeAction

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.google.android.material.datepicker.MaterialDatePicker
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentCreateNewBinding
import com.investmango.hrconsole.databinding.MemberLayoutBinding
import com.investmango.hrconsole.model.AssignMeeting
import com.investmango.hrconsole.model.AssignedUsersItem
import com.investmango.hrconsole.model.Departments
import com.investmango.hrconsole.model.MeetingItem
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar


class CreateNewFragment : Fragment(), RecyclerViewInterface<MemberLayoutBinding> {

    private lateinit var binding: FragmentCreateNewBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var list: List<String?> = arrayListOf()
    var departments = ""
    var selectedList: ArrayList<TotalEmpResponseItem> = arrayListOf()
    var finalList: ArrayList<TotalEmpResponseItem> = arrayListOf()
    var assignedUser: ArrayList<AssignedUsersItem> = arrayListOf()
    var authority: String = ""
    lateinit var progressBar: ProgressDialog
    private val uniqueItems = mutableSetOf<TotalEmpResponseItem>()
    var allActiveUsers: List<TotalEmpResponseItem?>? = null
    lateinit var meetingItem: MeetingItem
    var editing: Boolean = false


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        authority = preferences.getString("Authority", "0").toString()
        userId = preferences.getLong("userId", 0)


        progressBar = ProgressDialog(context)
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("Please wait...")
        progressBar.setCancelable(true)


        if (authority.equals(Constant.MANAGER))
            getChildActiveUser()
        else if (authority.equals(Constant.ADMIN)) {
            getDepartments()
            getAllActiveUser()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_new, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBundle()

        setlocationSpinner()
        setAdapt()

        binding.dateSelected.setOnClickListener {
            setDatePicker()
        }

        binding.selectedTime.setOnClickListener {
            setClock()
        }
        binding.delete.setOnClickListener {
            delete()
            activity?.onBackPressed()

        }
        binding.addMore.setOnClickListener {
            if (authority.equals(Constant.MANAGER))
                showMemberList()
            else if (authority.equals(Constant.ADMIN)) {
                showMemberListAdmin()

            }
        }
        binding.removeall.setOnClickListener {
            if (assignedUser != null && !assignedUser.isEmpty())
                assignedUser.clear()
            else if (finalList != null && !finalList.isEmpty())
                finalList.clear()
            binding.memberRecycler.adapter?.notifyDataSetChanged()
        }


        binding.createNow.setOnClickListener {
            if (!binding.dateSelected.text.trim().equals("Select Date") &&
                !binding.dateSelected.text.equals("") &&
                !binding.selectedTime.text.trim().equals("Select Time" )&&
                !binding.selectedTime.text.equals("") &&
                !binding.description.text.toString().equals("") &&
                !binding.title.text.toString().equals("") && !binding.location.text.toString().trim().equals(""))
            {
                Log.e("location", "onViewCreated: "+ !binding.location.text.trim().equals(""))
                val assignMeeting = AssignMeeting()
                assignMeeting.meetingTime =
                    DateAndTimeUtility.convertToEpochMillis(
                        binding.dateSelected.text.toString(),
                        binding.selectedTime.text.toString()
                    )
                assignMeeting.location = binding.location.text.toString()
                assignMeeting.description = binding.description.text.toString()
                assignMeeting.purpose = binding.title.text.toString()
                if (selectedList != null && !selectedList.isEmpty()) {
                    addUniqueItems()
                    Log.e(
                        "getTiming",
                        "onViewCreated: " + binding.dateSelected.text.toString() + "  " + binding.selectedTime.text.toString()
                    )

                    val userIds: ArrayList<Long> = arrayListOf()

                    for (i in 0..finalList.size - 1) {
                        userIds.add(finalList.get(i).id)
                    }
                    userIds.add(userId)
                    assignMeeting.userIds = userIds
                    assignMeeting(assignMeeting)
                }
                else if (assignedUser != null && !assignedUser.isEmpty()) {
                    addUniqueItems()
                    Log.e(
                        "getTiming",
                        "onViewCreated: " + binding.dateSelected.text.toString() + "  " + binding.selectedTime.text.toString()
                    )

                    val userIds: ArrayList<Long> = arrayListOf()

                    for (i in 0..assignedUser.size - 1) {
                        userIds.add(assignedUser.get(i).assignToId!!)
                    }
//                    userIds.add(userId)
                    assignMeeting.id= meetingItem.id!!
                    assignMeeting.userIds = userIds
                    EditMeeting(assignMeeting)
                } else if (departments != "") {
                    assignMeetingDepartment(assignMeeting, departments)
                } else {
                    Toast.makeText(context, "Add some member for Meeting.", Toast.LENGTH_LONG)
                        .show()
                }

            } else Toast.makeText(context, "Fill all details.", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBundle() {
        if (arguments != null) {
            editing = true
            meetingItem = arguments?.getSerializable("meeting") as MeetingItem
            Log.d("getBundle", "getBundle: " + meetingItem.meetingTime)
            setData()
            binding.removeall.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setData() {
        if (meetingItem != null) {
            binding.title.setText(meetingItem.purpose)
            binding.description.setText(meetingItem.description)
            binding.location.setText(meetingItem.location)
            binding.dateSelected.setText(DateAndTimeUtility.getDATEFromLong(meetingItem.meetingTime))
            binding.selectedTime.setText(DateAndTimeUtility.getTimeInHourFromLong(meetingItem.meetingTime))
            binding.location.setText(meetingItem.location)

            for (i in 0 until meetingItem.assignedUsers!!.size) {
                meetingItem.assignedUsers!!.get(i)?.let { assignedUser.add(it) }
            }
            setAdapt()
        }
    }

    private fun assignMeeting(assignMeeting: AssignMeeting) {
        val call = apiInterface.assignMeetingUser(
            userId, assignMeeting
        )
        progressBar.show()

        call.enqueue(object : Callback<AssignMeeting?> {
            override fun onResponse(
                call: Call<AssignMeeting?>,
                response: Response<AssignMeeting?>,
            ) {
                if (response.isSuccessful) {
                    // Redirect to MeetingFragment
                    delete()
                    if (context != null)
                        Toast.makeText(context, "Meeting assigned successfully.", Toast.LENGTH_LONG)
                            .show()
                    activity?.onBackPressed()
                } else {
                    if (response.code() == 500) {
                        Toast.makeText(
                            context,
                            "A meeting already exists for this date",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to assign meeting. Error code: " + response.code(),
                            Toast.LENGTH_LONG
                        ).show()
                        handleErrorResponse(response)
                    }
                }
                progressBar.dismiss()
            }

            override fun onFailure(call: Call<AssignMeeting?>, t: Throwable) {
                Toast.makeText(context, "Something Went Wrong.", Toast.LENGTH_LONG).show()
                Log.e("meetingAssigned", "onFailure: " + t.message)
                progressBar.dismiss()
            }
        })
    }

    private fun EditMeeting(assignMeeting: AssignMeeting) {
        val call = apiInterface.editMeetingUser(
            userId, assignMeeting
        )
        progressBar.show()

        call.enqueue(object : Callback<AssignMeeting?> {
            override fun onResponse(
                call: Call<AssignMeeting?>,
                response: Response<AssignMeeting?>,
            ) {
                Log.e("isSuccessful", "onResponse: "+assignMeeting.userIds+ " "+ userId )
                if (response.isSuccessful) {
                    // Redirect to MeetingFragment
                    delete()
                    if (context != null)
                        Toast.makeText(context, "Meeting assigned successfully.", Toast.LENGTH_LONG)
                            .show()
                    activity?.onBackPressed()
                } else {
                    if (response.code() == 500) {
                        Toast.makeText(
                            context,
                            "A meeting already exists for this date",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to assign meeting. Error code: " + response.code(),
                            Toast.LENGTH_LONG
                        ).show()
                        handleErrorResponse(response)
                    }
                }
                progressBar.dismiss()
            }

            override fun onFailure(call: Call<AssignMeeting?>, t: Throwable) {
                Toast.makeText(context, "Something Went Wrong.", Toast.LENGTH_LONG).show()
                Log.e("meetingAssigned", "onFailure: " + t.message)
                progressBar.dismiss()
            }
        })
    }

    private fun assignMeetingDepartment(assignMeeting: AssignMeeting, Department: String) {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        val call = apiInterface.assignMeetingDepartment(
            userId, assignMeeting, Department
        )
        progressBar.show()
        call.enqueue(object : Callback<AssignMeeting?> {
            override fun onResponse(
                call: Call<AssignMeeting?>,
                response: Response<AssignMeeting?>,
            ) {
                if (response.isSuccessful) {
                    // Redirect to MeetingFragment
                    delete()
                    if (context != null)
                        Toast.makeText(context, "Meeting assigned successfully.", Toast.LENGTH_LONG)
                            .show()
                    activity?.onBackPressed()
                } else {
                    if (response.code() == 500) {
                        Toast.makeText(
                            context,
                            "A meeting already exists for this date",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to assign meeting. Error code: " + response.code(),
                            Toast.LENGTH_LONG
                        ).show()
                        handleErrorResponse(response)
                    }
                }
                progressBar.dismiss()
            }

            override fun onFailure(call: Call<AssignMeeting?>, t: Throwable) {
                Toast.makeText(context, "Something Went Wrong.", Toast.LENGTH_LONG).show()
                Log.e("meetingAssigned", "onFailure: " + t.message)
                progressBar.dismiss()
            }
        })
    }

    private fun handleErrorResponse(response: Response<AssignMeeting?>) {
        var errorMessage = "Failed to connect internet"
        if (response.errorBody() != null) {
            try {
                val jsonObject = org.json.JSONObject(response.errorBody()!!.string())
                val message = jsonObject.optString("message")
                if (!message.isEmpty()) {
                    errorMessage = message
                    Log.e("errorMessage", "handleErrorResponse: " + errorMessage)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun setDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()

        builder.setTheme(R.style.CustomThemeOverlay_MaterialCalendar_Fullscreen);

        val picker = builder.build()
        picker.show(activity?.supportFragmentManager!!, picker.toString())
        //To apply the fullscreen

        // builder.setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar_Fullscreen);
        picker.addOnNegativeButtonClickListener { picker.dismiss() }
        picker.addOnPositiveButtonClickListener {
            val sdf = SimpleDateFormat("yyyy-dd-mm")


            val calendar = Calendar.getInstance()
            calendar.setTimeInMillis(it)
            val year: Int = calendar.get(Calendar.YEAR)
            val month: Int = calendar.get(Calendar.MONTH)
            val dayOfMonth: Int = calendar.get(Calendar.DAY_OF_MONTH)
            val selectedDate = dayOfMonth.toString() + "/" + (month + 1) + "/" + year
            binding.dateSelected.setText(selectedDate)

        }
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
                } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem?>>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    private fun getDepartments() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        val call = apiInterface.getDepartments()
        call.enqueue(object : Callback<Departments> {
            override fun onResponse(
                call: Call<Departments>,
                response: Response<Departments>,
            ) {
                if (response.isSuccessful) {
                    list = response.body()?.departmentName!!
                    Log.e("getDepartments", "onResponse: " + list)
                } else {
                    Log.e("getDepartments", "onResponse: " + response.body())

                    Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Departments>, t: Throwable) {
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
                    allActiveUsers = response.body()
                } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem?>>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    fun setClock() {
        val c = Calendar.getInstance()

        // on below line we are getting our hour, minute.
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // on below line we are initializing
        // our Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            context,
            { view, hourOfDay, minute ->
                // on below line we are setting selected
                // time in our text view.
                val formattedTime = java.lang.String.format("%02d:%02d", hourOfDay, minute)

                binding.selectedTime.setText(formattedTime)
            },
            hour,
            minute,
            false
        )
        // at last we are calling show to
        // display our time picker dialog.
        timePickerDialog.show()

    }

    fun setlocationSpinner() {
        val list = resources.getStringArray(R.array.listLeaveType)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.color_spinner_layout, list)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
//        binding.location.setAdapter(arrayAdapter)
    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): MemberLayoutBinding {
        return MemberLayoutBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {

        if (!finalList.isEmpty())
            return finalList.size
        else
            return assignedUser.size
    }

    override fun bindView(viewBind: MemberLayoutBinding, position: Int) {
        if (finalList.isNotEmpty()) {
            viewBind.nameOfMember.setText(finalList[position].userName)
            viewBind.delete.setOnClickListener {
                finalList.removeAt(position)
                binding.memberRecycler.adapter?.notifyItemRemoved(position)
            }
        } else if (!assignedUser.isEmpty()) {
            viewBind.nameOfMember.setText(assignedUser[position].assignToName)
            Log.d("assignToName", "bindView: " + assignedUser[position].assignToName)
            viewBind.delete.setOnClickListener {
                assignedUser.removeAt(position)
                binding.memberRecycler.adapter?.notifyItemRemoved(position)
            }

        }

    }

    @SuppressLint("MissingInflatedId")
    private fun showMemberListAdmin() {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.admin_meeting_wide_selection, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.membersList)
        val departmentsView = view.findViewById<RelativeLayout>(R.id.departments)
        val MemberView = view.findViewById<RelativeLayout>(R.id.wideSelection)
        val departmentList = view.findViewById<RecyclerView>(R.id.departmentList)
        val cross = view.findViewById<ImageView>(R.id.close)
        val done = view.findViewById<Button>(R.id.done)
        val done2 = view.findViewById<Button>(R.id.done2)
        val toggleSwitch = view.findViewById<SwitchCompat>(R.id.simpleSwitch)

        departmentList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        if (list != null && !list.isEmpty())
            departmentList.adapter = DepartmentAdap(this, list)

        toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle the toggle state change
            if (isChecked) {
                MemberView.visibility = View.VISIBLE
                departmentsView.visibility = View.GONE
            } else {
                departmentsView.visibility = View.VISIBLE
                MemberView.visibility = View.GONE
            }

        }
        builder.setView(view)

        recyclerView.layoutManager = GridLayoutManager(context, 1)
        if (allActiveUsers != null)
            recyclerView.adapter = Member_list_Adapter(this, allActiveUsers!!)
        val dialog = builder.create()

        cross.setOnClickListener { dialog.dismiss() }
        done.setOnClickListener {
            Log.e("selectedList", "showMemberList: " + selectedList.size)
            addUniqueItems()
            setAdapt()
            binding.memberRecycler.adapter?.notifyDataSetChanged()
            dialog.dismiss()
        }
        done2.setOnClickListener {
            setDepartmentName()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showMemberList() {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.member_list_alert, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.membersList)

        val cross = view.findViewById<ImageView>(R.id.close)
        val done = view.findViewById<Button>(R.id.done)
        builder.setView(view)

        recyclerView.layoutManager = GridLayoutManager(context, 1)
        recyclerView.adapter = Member_list_Adapter(this, allActiveUsers!!)
        val dialog = builder.create()

        cross.setOnClickListener { dialog.dismiss() }
        done.setOnClickListener {
            Log.e("selectedList", "showMemberList: " + selectedList.size)
            addUniqueItems()
            setAdapt()
            binding.memberRecycler.adapter?.notifyDataSetChanged()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun delete() {
        finalList.clear()
        binding.memberRecycler.adapter?.notifyDataSetChanged()
        binding.description.setText("")
        binding.dateSelected.setText("")
        binding.selectedTime.setText("")
        binding.title.setText("")
        binding.location.setSelection(0)

    }

    @SuppressLint("SuspiciousIndentation")
    fun setAdapt() {
        if (finalList != null && !finalList.isEmpty()) {
            binding.memberRecycler.layoutManager = GridLayoutManager(context, 2)
            binding.memberRecycler.adapter = CommonAdapter(this)
            departments = ""
            binding.departName.visibility = View.GONE
            binding.memberRecycler.visibility = View.VISIBLE
        } else if (assignedUser != null && !assignedUser!!.isEmpty()) {
            binding.memberRecycler.layoutManager = GridLayoutManager(context, 2)
            binding.memberRecycler.adapter = CommonAdapter(this)
            departments = ""
            binding.departName.visibility = View.GONE
            binding.memberRecycler.visibility = View.VISIBLE
        }
    }

    private fun addUniqueItems() {
        for (item in selectedList) {
            if (uniqueItems.add(item)) { // Set.add() returns false if the item already exists
                finalList.add(item)
            }
        }
    }

    fun setDepartmentName() {
        binding.departName.visibility = View.VISIBLE
        binding.memberRecycler.visibility = View.GONE
        binding.departName.setText(departments)
        finalList.clear()
    }

}
