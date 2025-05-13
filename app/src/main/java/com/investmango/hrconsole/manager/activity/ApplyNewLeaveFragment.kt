package com.investmango.hrconsole.manager.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.investmango.hrconsole.AwsUpload.UploadFileAws
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentApplyNewLeaveBinding
import com.investmango.hrconsole.model.LeaveItem
import com.investmango.hrconsole.model.SaveUserLeave
import com.investmango.hrconsole.model.TaskItems
import com.investmango.hrconsole.service.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Objects


class ApplyNewLeaveFragment : Fragment() {
    private lateinit var binding: FragmentApplyNewLeaveBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var selectedDates: ArrayList<String> = arrayListOf()
    lateinit var progressDialog: AwesomeProgressDialog
    lateinit var launcher: ActivityResultLauncher<Intent>
    var uri: Uri? = Uri.parse("")
    var uriStr = ""
    lateinit var nameIndex: String
    var sizeIndex: Long = 0
    lateinit var file1: File
 var leavId=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0").toString()
        userId = preferences.getLong("userId", 0)

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)

        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                uri = result.data!!.data!!
                Log.e("launcherrrr", "onCreate: " + uri)

                result.data?.let { returnUri ->
                    context?.contentResolver?.query(uri!!, null, null, null, null)
                }?.use { cursor ->
                    /*
                     * Get the column indexes of the data in the Cursor,
                     * move to the first row in the Cursor, get the data,
                     * and display it.
                     */
                    nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).toString()
                    val size = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    sizeIndex = cursor.getLong(size)
                    Log.e("launcherrrr", "onCreate: " + sizeIndex)

                    cursor.moveToFirst()

                    file1 = File(
                        Objects.requireNonNull<String>(
                            UploadFileAws().getRealPathFromUri(
                                uri!!,
                                context!!
                            )
                        )
                    )
                    Log.e("khushi1111", "onClick: " + file1)

//                    uploadImage(filename = file1.name)

                        if (isAdded)
                            CoroutineScope(Dispatchers.Main).launch {
                                uriStr = UploadFileAws().uploadFile(file1, "leavesDocs", context!!).toString()
                                if (uriStr != "") {
                                    // Handle the success case here
                                    Log.e("uploadimg", "onCreate: "+uriStr )
                                    binding.uploadDocname.visibility = View.VISIBLE
                                    binding.uploadDoc.visibility = View.GONE
                                } else {
                                    // Handle the failure case here
                                    makeText(context,"Some error in uploading .", LENGTH_SHORT).show()
                                }
                            }

                }
            }

        };
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
        setupLeaveTypeSpinner()

        getBundle()

        binding.reason.setLines(5)

        binding.askNow.setOnClickListener {
            if (leavId!=0){
                ChangeUserLeave()
            }else
            saveUserLeave()
        }
        binding.uploadDoc.setOnClickListener {
            UploadFileAws().openGallery(launcher)
        }
        binding.uploadDocname.setOnClickListener {
            uriStr=""
            binding.uploadDocname.text = ""
            binding.uploadDoc.visibility = View.VISIBLE
            binding.uploadDocname.visibility = View.INVISIBLE
        }
        binding.clear.setOnClickListener {
            binding.reason.setText("")
            binding.datelayout.visibility = View.GONE
        }
        binding.delete.setOnClickListener {
            selectedDates.clear()
            binding.datelayout.visibility = View.GONE
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

    fun getBundle() {
        //val leaveList: ArrayList<LeaveItem> = arguments?.getSerializable("editLeave") as ArrayList<LeaveItem>
        val leaveList = arguments?.getSerializable("editLeave") as? ArrayList<LeaveItem> ?: arrayListOf()

        if (leaveList.isNullOrEmpty()) {
            Log.e("AssignmentsResponse", "getBundle: arguments or editLeave is null")
            return
        }

        val leave = leaveList[0] // Assuming only one item is passed
        leavId = leave.id?.toInt()!!
        Log.e("AssignmentsResponse", "getBundle: $leavId")

        binding.reason.setText(leave.reason)
        binding.datelayout.visibility = View.VISIBLE
        binding.dateForLeave.text = "${leave.leaveDates?.get(0)} - ${leave.leaveDates?.last()}"

        // Convert leave.leaveType to LeaveType enum using fromString
        val leaveTypeEnum = SaveUserLeave.LeaveType.fromConst(leave.leaveType)
        val leaveTypeLabel = leaveTypeEnum?.label ?: "" // This gives values like "Absent", "Half Day", etc.
        Log.e("SpinnerSelection", "getBundle: "+leaveTypeLabel+ leaveTypeEnum +leave.leaveType)
        val leaveTypeList = resources.getStringArray(R.array.listLeaveType)
        val index = leaveTypeList.indexOf(leaveTypeLabel)

        if (index != -1) {
            binding.leaveType.setSelection(index)
        } else {
            Log.e("SpinnerSelection", "Leave type not found in list: $leaveTypeLabel")
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
        progressDialog.showDialog()
        // Convert leaveType string to enum
        val leaveType =
            SaveUserLeave.LeaveType.fromString(binding.leaveType.selectedItem.toString())

        try {
            if(leavId!=0){
                val jsonBody2 = JSONObject()
                jsonBody2.put("leaveType", leaveType.name)
                jsonBody2.put("reason", binding.reason.text)
            }

            val jsonBody = JSONObject()
            jsonBody.put("leaveDates", JSONArray(selectedDates))
            jsonBody.put("leaveType", leaveType.name)
            jsonBody.put("reason", binding.reason.text)
            jsonBody.put("fileUrl", uriStr)

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                jsonBody.toString()
            )

            Log.e("jsonput", "saveUserLeave: " + requestBody)

            val apiClient = ApiClient(requireContext())
            apiInterface = apiClient.apiInterface

            val call = apiInterface.saveUserLeave(requestBody, userId)
            call.enqueue(object : retrofit2.Callback<SaveUserLeave?> {
                override fun onResponse(
                    call: Call<SaveUserLeave?>,
                    response: Response<SaveUserLeave?>,
                ) {
                    if (response.isSuccessful) {
                        progressDialog.dismissDialog()
                        makeText(
                            requireContext(),
                            "Leave request send successfully",
                            LENGTH_SHORT
                        ).show()
                        activity?.onBackPressed()
//                        fragmentManager?.fragments?.remove(this@ApplyNewLeaveFragment)
//                        fragmentManager?.beginTransaction()?.remove(this@ApplyNewLeaveFragment)
//                            ?.commit();

                    } else {
                        progressDialog.dismissDialog()
                        if (isAdded)
                            makeText(context, getErrorMessage(response), LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SaveUserLeave?>, t: Throwable) {
                    progressDialog.dismissDialog()
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
    private fun ChangeUserLeave() {
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
        progressDialog.showDialog()
        // Convert leaveType string to enum
        val leaveType =
            SaveUserLeave.LeaveType.fromString(binding.leaveType.selectedItem.toString())

        try {
                val jsonBody2 = JSONObject()
                jsonBody2.put("id",leavId)
                jsonBody2.put("leaveType", leaveType.name)
                jsonBody2.put("reason", binding.reason.text)

                val requestBody = RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    jsonBody2.toString()
                )

                Log.e("jsonput", "saveUserLeave: " + requestBody)

                val apiClient = ApiClient(requireContext())
                apiInterface = apiClient.apiInterface

                val call = apiInterface.EditLeave(leavId,requestBody)
                call.enqueue(object : Callback<SaveUserLeave?> {
                    override fun onResponse(
                        call: Call<SaveUserLeave?>,
                        response: Response<SaveUserLeave?>,
                    ) {
                        if (response.isSuccessful) {
                            progressDialog.dismissDialog()
                            makeText(
                                requireContext(),
                                "Leave request send successfully",
                                LENGTH_SHORT
                            ).show()
                            activity?.onBackPressed()
//                        fragmentManager?.fragments?.remove(this@ApplyNewLeaveFragment)
//                        fragmentManager?.beginTransaction()?.remove(this@ApplyNewLeaveFragment)
//                            ?.commit();

                        } else {
                            progressDialog.dismissDialog()
                            if (isAdded)
                                makeText(context, getErrorMessage(response), LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<SaveUserLeave?>, t: Throwable) {
                        progressDialog.dismissDialog()
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
