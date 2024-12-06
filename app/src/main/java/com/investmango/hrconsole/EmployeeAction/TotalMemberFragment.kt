package com.investmango.hrconsole.EmployeeAction;

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.investmango.hrconsole.EmployeeAction.EmployeeActions
import com.investmango.hrconsole.EmployeeAction.EmplyDetail.FilteredMeetingFragment
import com.investmango.hrconsole.EmployeeAction.EmplyPerFormanceFragment
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.ActiveMemberRecyclerBinding
import com.investmango.hrconsole.databinding.FragmentTotalMemberBinding
import com.investmango.hrconsole.manager.activity.AchievementFragment
import com.investmango.hrconsole.manager.activity.ManagerActivity
import com.investmango.hrconsole.manager.activity.PerformanceFragment
import com.investmango.hrconsole.model.Content
import com.investmango.hrconsole.model.PresentEmpRes
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TotalMemberFragment : Fragment(), RecyclerViewInterface<ActiveMemberRecyclerBinding> {
    lateinit var apiInterface: ApiInterface
    var listofpresentEmp: ArrayList<Content?> = arrayListOf()
    var listoftotalEmp: ArrayList<TotalEmpResponseItem> = arrayListOf()
    var absent: ArrayList<TotalEmpResponseItem> = arrayListOf()
    var present: ArrayList<TotalEmpResponseItem> = arrayListOf()
    private lateinit var binding: FragmentTotalMemberBinding
    lateinit var token: String
    lateinit var authority: String
    var userId: Long = 0
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
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_total_member, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (requireContext() != null) {
            if (authority.equals(Constant.MANAGER)) {
//                    Log.e("arguments", "onViewCreated: " + getString("Emp"))
//                    if (getString("Emp").equals("Present Members")) {
//                        PresentEmpRes()
//                    } else if (getString("Emp").equals("Total Members")) {
                TotalEmpResponse()
//                    }
            } else if (authority.equals(Constant.ADMIN)) {
//                    Log.e("arguments", "onViewCreated: " + getString("Emp"))
//                    if (getString("Emp").equals("Present Employees")) {
//                        AdminPresentEmpRes()
//                    } else if (getString("Emp").equals("Total Employees")) {
                AdminTotalEmpResponse()
//                    }
            }

        }

        binding.Filter.setOnClickListener {
            if (isAdded)
                showFilterBox(context!!)
        }
    }

    private fun PresentEmpRes() {
        listoftotalEmp.clear()
        val apiClient = ApiClient(requireContext())

        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()

        val call: Call<PresentEmpRes>? = apiInterface.PresentEmployee(userId, true, 100)
        call?.enqueue(object : Callback<PresentEmpRes?> {
            override fun onResponse(
                call: Call<PresentEmpRes?>,
                response: Response<PresentEmpRes?>,
            ) {
                if (response.body() != null && response.isSuccessful() && response.body()!!.content.size > 0) {
                    progressDialog.dismissDialog()
                    Log.e("getmeetings", "onResponse: " + response.body()!!.content.size)
                    listofpresentEmp = response.body()!!.content as ArrayList<Content?>
                    binding.recyclerView.adapter = CommonAdapter(this@TotalMemberFragment)
                    binding.recyclerView.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                } else {
                    progressDialog.dismissDialog()
                    Log.e("getmeetings", "onResponse: " + response.body().toString())
                    if (isAdded)
                        Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PresentEmpRes?>, t: Throwable) {
                progressDialog.dismissDialog()
                if (isAdded)
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    private fun showFilterBox(context: Context) {
        val dialog1 = BottomSheetDialog(context, R.style.BottomSheetDialog)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setCancelable(true)
        dialog1.setCanceledOnTouchOutside(true)
        dialog1.setContentView(R.layout.employee_filter)
        val window = dialog1.window!!
        window!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )


        val statusSpin = dialog1.findViewById<Spinner>(R.id.status)
        val showResult = dialog1.findViewById<TextView>(R.id.showresult)
        val list2 = listOf("Present", "Absent")

        val arrayAdapter =
            ArrayAdapter<Any?>(requireContext(), R.layout.color_spinner_layout, list2)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        assert(statusSpin != null)
        statusSpin!!.adapter = arrayAdapter


        showResult!!.setOnClickListener {
            val selectedStatus = statusSpin.selectedItem.toString()


//            val filteredList = when (selectedStatus) {
//                "Present" -> listoftotalEmp2.filter { it.isTodayPresent } // Filter active (Present) employees
//                "Absent" -> listoftotalEmp2.filter { !it.isTodayPresent } // Filter inactive (Absent) employees
//                else -> listoftotalEmp2 // Return the full list if no valid filter selected
//            }

// Log the filtered list (optional, for debugging)
//            Log.e("Filtered List", "Filtered Employees: $filteredList")

// Update the list used by the RecyclerView adapter
            listoftotalEmp.clear() // Clear the previous list
            if (selectedStatus.equals("Present"))
                listoftotalEmp.addAll(present) // Add the filtered data
            else listoftotalEmp.addAll(absent)
// Notify the adapter that the data has changed
            binding.recyclerView.adapter?.notifyDataSetChanged()
            dialog1.dismiss()
        }
        dialog1.show()
    }

    private fun filterList(status: Boolean): List<TotalEmpResponseItem> {
        val (activeEmployees, inactiveEmployees) = listoftotalEmp.partition { it.isTodayPresent }
        if (status)
            return activeEmployees
        else return inactiveEmployees
    }

    private fun AdminPresentEmpRes() {
        listoftotalEmp.clear()

        if (!isAdded) return  // Ensure the fragment is attached

        val apiClient = ApiClient(requireContext())
        progressDialog.showDialog()
        apiInterface = apiClient.apiInterface

        try {

            val call: Call<PresentEmpRes>? = apiInterface.newgetAllTodayAttendance(100)
            call?.enqueue(object : Callback<PresentEmpRes?> {
                override fun onResponse(
                    call: Call<PresentEmpRes?>,
                    response: Response<PresentEmpRes?>,
                ) {
                    if (response.body() != null && response.isSuccessful() && response.body()!!.content.size > 0) {
                        progressDialog.dismissDialog()

                        Log.e("getmeetings", "onResponse: " + response.body()!!.content.size)
                        listofpresentEmp = response.body()!!.content as ArrayList<Content?>
                        binding.recyclerView.adapter = CommonAdapter(this@TotalMemberFragment)
                        if (requireContext() != null)
                            binding.recyclerView.layoutManager =
                                LinearLayoutManager(
                                    requireContext(),
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )

                    } else {
                        Log.e("getmeetings", "onResponse: " + response.body().toString())
                        progressDialog.dismissDialog()
                        if (isAdded)
                            Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<PresentEmpRes?>, t: Throwable) {
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                    Log.e("khushi123", "onFailure: " + t.message)
                }
            })
        } catch (E: Exception) {
            Log.e("TAG", "AdminPresentEmpRes: " + E.message)
        }
    }

    private fun TotalEmpResponse() {
        listofpresentEmp.clear()
        val apiClient = ApiClient(requireContext())

        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()

        val call: Call<List<TotalEmpResponseItem>>? = apiInterface.getTotalEmp(userId, true)
        call?.enqueue(object : Callback<List<TotalEmpResponseItem>?> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem>?>,
                response: Response<List<TotalEmpResponseItem>?>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    progressDialog.dismissDialog()

                    Log.e("getmeetings", "onResponse: ")
//                    listoftotalEmp = (response.body()!!.totalEmpResponse as ArrayList<TotalEmpResponseItem>?)!!
                    binding.recyclerView.adapter = CommonAdapter(this@TotalMemberFragment)
                    listoftotalEmp = (response.body() as ArrayList<TotalEmpResponseItem>?)!!
                     absent = listoftotalEmp.filter { !it.isTodayPresent } as ArrayList<TotalEmpResponseItem>
                     present = listoftotalEmp.filter { it.isTodayPresent } as ArrayList<TotalEmpResponseItem>
                    binding.recyclerView.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                } else {
                    Log.e("getmeetings", "onResponse: " + response.body().toString())
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem>?>, t: Throwable) {
                progressDialog.dismissDialog()
                if (isAdded)
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    private fun AdminTotalEmpResponse() {
        listofpresentEmp.clear()
        val apiClient = ApiClient(requireContext())
        if (!isAdded) return  // Ensure the fragment is attached

        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()

        val call: Call<List<TotalEmpResponseItem>>? = apiInterface.getAllEmployee(true)
        call?.enqueue(object : Callback<List<TotalEmpResponseItem>?> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem>?>,
                response: Response<List<TotalEmpResponseItem>?>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    progressDialog.dismissDialog()

                    Log.e("getmeetings", "onResponse: ")
//                    listoftotalEmp = (response.body()!!.totalEmpResponse as ArrayList<TotalEmpResponseItem>?)!!
                    binding.recyclerView.adapter = CommonAdapter(this@TotalMemberFragment)
                    listoftotalEmp = (response.body() as ArrayList<TotalEmpResponseItem>?)!!
                    absent = listoftotalEmp.filter { !it.isTodayPresent } as ArrayList<TotalEmpResponseItem>
                    present = listoftotalEmp.filter { it.isTodayPresent } as ArrayList<TotalEmpResponseItem>
                    if (isAdded)
                        binding.recyclerView.layoutManager =
                            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

                } else {
                    Log.e("getmeetings", "onResponse: " + response.body().toString())
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem>?>, t: Throwable) {
                progressDialog.dismissDialog()
                if (isAdded)
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): ActiveMemberRecyclerBinding {
        return ActiveMemberRecyclerBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        if (listoftotalEmp.size == 0)
            return listofpresentEmp.size
        else return listoftotalEmp.size
    }

    override fun bindView(viewBind: ActiveMemberRecyclerBinding, position: Int) {
        if (listofpresentEmp.size != 0) {
            viewBind.name.setText(listofpresentEmp.get(position)?.userName)
            viewBind.position.setText(listofpresentEmp.get(position)?.designation)
            viewBind.id.setText(listofpresentEmp.get(position)?.userId.toString())

            if (listofpresentEmp[position]?.profileImage != "" && listofpresentEmp[position]?.profileImage != null) {
                if (isAdded)
                    Glide.with(context!!).load(listofpresentEmp[position]?.profileImage)
                        .into(viewBind.profilepic)
                viewBind.profilepic.visibility = View.VISIBLE
                viewBind.initialAvatar.visibility = View.GONE
            } else {
                viewBind.initialAvatar.setName(listofpresentEmp[position]?.userName!!)
                viewBind.profilepic.visibility = View.GONE
                viewBind.initialAvatar.visibility = View.VISIBLE
            }

            viewBind.layoutfull.setOnClickListener {
                (activity as ManagerActivity?)!!.replaceFragment(
                    putChildId(
                        listofpresentEmp.get(
                            position
                        )?.userId!!
                    )
                )
            }
        } else {

            viewBind.name.setText(listoftotalEmp.get(position).userName)
            viewBind.position.setText(listoftotalEmp.get(position).designation)
            viewBind.id.setText(listoftotalEmp.get(position).id.toString())

            if (listoftotalEmp[position].isTodayPresent != true) {
                viewBind.statusIndicator.setImageDrawable(resources.getDrawable(R.drawable.red_dot))
            } else {
                viewBind.statusIndicator.setImageDrawable(resources.getDrawable(R.drawable.green_dot))

            }

            if (listoftotalEmp[position].profile != "" && listoftotalEmp[position].profile != null) {
                if (isAdded)
                    Glide.with(context!!).load(listoftotalEmp[position].profile)
                        .into(viewBind.profilepic)
                viewBind.profilepic.visibility = View.VISIBLE
                viewBind.initialAvatar.visibility = View.GONE
            } else {
                viewBind.initialAvatar.setName(listoftotalEmp[position].userName)
                viewBind.profilepic.visibility = View.GONE
                viewBind.initialAvatar.visibility = View.VISIBLE
            }

            viewBind.layoutfull.setOnClickListener {
                (activity as ManagerActivity?)!!.replaceFragment(
                    putChildId(
                        listoftotalEmp.get(
                            position
                        )?.id!!
                    )
                )
            }
        }
    }

    fun putChildId(Id: Long): Fragment {
        val fragment = EmplyPerFormanceFragment()
        val bundle = Bundle()
        bundle.putString("ViewOf", "child")
        bundle.putLong("childUserid", Id)
        Log.e("Achievements", "onCreate: " + Id)

        fragment.arguments = bundle
        return fragment
    }

}
