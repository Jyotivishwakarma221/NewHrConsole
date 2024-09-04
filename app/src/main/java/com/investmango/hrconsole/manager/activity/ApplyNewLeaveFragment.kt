package com.investmango.hrconsole.manager.activity

import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentApplyNewLeaveBinding
import com.investmango.hrconsole.model.SaveUserLeave
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar


class ApplyNewLeaveFragment : Fragment() {
    private lateinit var binding: FragmentApplyNewLeaveBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var selectedDates: ArrayList<String> = arrayListOf()
    lateinit var progressDialog: AwesomeProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0").toString()
        userId = preferences.getLong("userId", 0)

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_apply_new_leave, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reason.setLines(5)
        setupLeaveTypeSpinner()

        binding.askNow.setOnClickListener {
            saveUserLeave()
        }

        binding.clear.setOnClickListener {
            binding.reason.setText("")
            binding.datelayout.visibility = View.INVISIBLE
        }
        binding.delete.setOnClickListener {
            selectedDates.clear()
            binding.datelayout.visibility = View.INVISIBLE
        }

        binding.calender.setOnClickListener {

            val builder = MaterialDatePicker.Builder.dateRangePicker()

            builder.setTheme(R.style.CustomThemeOverlay_MaterialCalendar_Fullscreen);

            val picker = builder.build()
            picker.show(activity?.supportFragmentManager!!, picker.toString())
//To apply the fullscreen:
//            builder.setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar_Fullscreen);
            picker.addOnNegativeButtonClickListener { picker.dismiss() }
            picker.addOnPositiveButtonClickListener {
                selectedDates.clear()
                val sdf = SimpleDateFormat("dd/MM/yyyy")
                // Convert selected date range to list of dates
                // Convert selected date range to list of dates

                binding.datelayout.visibility = View.VISIBLE

                val start = Calendar.getInstance()
                start.timeInMillis = it.first
                val end = Calendar.getInstance()
                end.timeInMillis = it.second
                while (!start.after(end)) {
                    val selectedDate = String.format(
                        "%d-%02d-%02d",
                        start[Calendar.YEAR],
                        start[Calendar.MONTH] + 1,
                        start[Calendar.DAY_OF_MONTH]
                    )
                    selectedDates.add(selectedDate)
                    start.add(Calendar.DATE, 1)
                }
                Log.e("selectedDates", "onViewCreated: " + selectedDates)
                if (it.first == it.second)
                    binding.dateForLeave.setText(sdf.format(java.sql.Date(it.first)))
                else binding.dateForLeave.setText(
                    (sdf.format(java.sql.Date(it.first)) + " - " + sdf.format(java.sql.Date(it.second)))
                )
            }
        }

    }

    private fun setupLeaveTypeSpinner() {
        val list = resources.getStringArray(R.array.listLeaveType)

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.color_spinner_layout, list)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        binding.leaveType.setAdapter(arrayAdapter)
    }

    private fun saveUserLeave() {
        // Check if leaveDates, leaveTypeStr, and reason are empty or null
        if (binding.dateForLeave.text.isEmpty() || binding.leaveType.id.toString() == "0" || binding.reason.text.isEmpty()) {
            // Show toast message
            makeText(
                requireContext(),
                "Date, leave type, and reason cannot be empty!",
                LENGTH_SHORT
            ).show()
            return
        }
        progressDialog?.showDialog()
        // Convert leaveType string to enum
        val leaveType =
            SaveUserLeave.LeaveType.fromString(binding.leaveType.selectedItem.toString())

        try {
            val jsonBody = JSONObject()
            jsonBody.put("leaveDates", JSONArray(selectedDates))
            jsonBody.put("leaveType", leaveType.name)
            jsonBody.put("reason", binding.reason.text)

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                jsonBody.toString()
            )

            Log.e("jsonput", "saveUserLeave: " + requestBody)

            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface

            val call = apiInterface.saveUserLeave(token, requestBody, userId)
            call.enqueue(object : retrofit2.Callback<SaveUserLeave?> {
                override fun onResponse(
                    call: Call<SaveUserLeave?>,
                    response: Response<SaveUserLeave?>,
                ) {
                    if (response.isSuccessful) {
                        progressDialog?.dismissDialog()
                        makeText(
                            requireContext(),
                            "Leave request send successfully",
                            LENGTH_SHORT
                        ).show()
                        fragmentManager?.fragments?.remove(this@ApplyNewLeaveFragment)
                    } else {
                        progressDialog?.dismissDialog()
                        if (isAdded)
                            makeText(context, getErrorMessage(response), LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SaveUserLeave?>, t: Throwable) {
                    progressDialog?.dismissDialog()
                    if (isAdded)
                        makeText(requireContext(), "Something went wrong.", LENGTH_SHORT).show()
                    Log.e("failure", "onFailure: " + t.message)
                }
            })
        } catch (e: JSONException) {
            progressDialog?.dismissDialog()
            e.printStackTrace()
        }
    }

    private fun getErrorMessage(response: Response<SaveUserLeave?>): String {
        var errorMessage = "Unknown error"
        try {
            val errorJson = JSONObject(response.errorBody()!!.string())
            errorMessage = errorJson.optString("message", errorMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return errorMessage
    }

}
