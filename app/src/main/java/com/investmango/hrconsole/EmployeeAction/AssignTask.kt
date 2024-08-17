package com.investmango.hrconsole.EmployeeAction

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.datepicker.MaterialDatePicker
import com.investmango.hrconsole.R
import com.investmango.hrconsole.admin.fragment.AdminTaskFragment
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.cloudinary.CloudinaryConfig
import com.investmango.hrconsole.databinding.FragmentAssignTask2Binding
import com.investmango.hrconsole.model.AllActiveUsers
import com.investmango.hrconsole.model.AssignTask
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.service.Constant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar

class AssignTask : Fragment() {
    private lateinit var binding: FragmentAssignTask2Binding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var authority: String = ""
    private val PICK_IMAGE_REQUEST = 1
    var sizeIndex: Long = 0
    private var allActiveUsers: List<TotalEmpResponseItem>? = null
    var selectedId: Long = 0
    lateinit var progressDialog: AwesomeProgressDialog
    var uri: Uri? = Uri.parse("")
    var uriStr = ""
    lateinit var nameIndex: String
    lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CloudinaryConfig.initCloudinary(requireContext())

        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0")!!
        authority = preferences.getString("Authority", "user").toString()
        userId = preferences.getLong("userId", 0)


        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)


        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
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
                }
                uploadImageToCloud(uri)
            }

        };
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_assign_task2, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (authority.equals(Constant.MANAGER)) {
            getAllActiveUser()
        } else if (authority.equals(Constant.ADMIN))
            getAdminAllActiveUser()

        binding.selectEmployee.setOnItemClickListener { parent, view, position, id ->
            selectedId = allActiveUsers?.get(position)?.id?.toLong()!!
        }

        binding.deadline.setOnClickListener {
            setDatePicker()
        }

        binding.uploadDoc.setOnClickListener {
            openGallery()
        }
        binding.uploadDocname.setOnClickListener {

            uri = Uri.parse("")
            binding.uploadDocname.text = ""
            binding.uploadDoc.visibility = View.VISIBLE
            binding.uploadDocname.visibility = View.INVISIBLE

        }
        binding.assignTask.setOnClickListener {
            if (binding.taskDescription.text.isEmpty()) {

                Toast.makeText(context, "Please fill some information.", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    if (isValid(binding.deadline.text.toString()))
                        sendTaskWithImage(uriStr)
                    else Toast.makeText(context, "Fill deadline date.", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Log.e("Exception", "onViewCreated: " + e.message)
                }

            }
        }

    }

    fun isValid(dateStr: String?): Boolean {
        val sdf: DateFormat = SimpleDateFormat("dd/mm/yyyy")
        sdf.setLenient(false)
        try {
            sdf.parse(dateStr)
        } catch (e: ParseException) {
            return false
        }
        return true
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        launcher.launch(galleryIntent)
    }

    private fun uploadImageToCloud(imageUri: Uri?) {
//        val fileSize: Long = getFileSize(imageUri)
        if (sizeIndex > 300 * 1024) {
            Toast.makeText(
                requireContext(),
                "File size exceeds 300 KB limit. Please upload a file smaller than 300 KB",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val progressDialog = ProgressDialog.show(requireContext(), "", "Uploading image...", true)
        val folderName = "taskfiles"
        val publicId = folderName + "/" + System.currentTimeMillis()
        // Configure the Cloudinary upload options
        MediaManager.get().upload(imageUri)
            .option("public_id", publicId) // Specify the folder name
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    progressDialog.dismiss()
                    uriStr = uri?.toString()!!
                    uriStr = (resultData["url"] as String?).toString()
                    Toast.makeText(
                        requireContext(),
                        "Image uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Dismiss progress dialog when upload is successful
                    progressDialog.dismiss()
                    binding.uploadDocname.visibility = View.VISIBLE
                    binding.uploadDocname.text = nameIndex
                    binding.uploadDoc.visibility = View.INVISIBLE
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Upload failed: " + error.description,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun getAllActiveUser() {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<List<TotalEmpResponseItem>>? = apiInterface.getTotalEmp(userId, true)
        call?.enqueue(object : Callback<List<TotalEmpResponseItem>?> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem>?>,
                response: Response<List<TotalEmpResponseItem>?>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    Log.e("getmeetings", "onResponse: ")
                    allActiveUsers = response.body()
                    val userNames: MutableList<String> = ArrayList()
                    for (user in allActiveUsers!!) {
                        userNames.add(user.userName.uppercase())
                    }
                    binding.selectEmployee.setAdapter(
                        ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            userNames
                        )
                    )
                } else {
                    Log.e("getmeetings", "onResponse: " + response.body().toString())

                    Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem>?>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    private fun getAdminAllActiveUser() {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<List<TotalEmpResponseItem>>? = apiInterface.getAllEmployee(true)
        call?.enqueue(object : Callback<List<TotalEmpResponseItem>?> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem>?>,
                response: Response<List<TotalEmpResponseItem>?>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    Log.e("getmeetings", "onResponse: ")
                    allActiveUsers = response.body()
                    val userNames: MutableList<String> = ArrayList()
                    for (user in allActiveUsers!!) {
                        userNames.add(user.userName.uppercase())
                    }
                    binding.selectEmployee.setAdapter(
                        ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            userNames
                        )
                    )
                } else {
                    Log.e("getmeetings", "onResponse: " + response.body().toString())

                    Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem>?>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    private fun sendTaskWithImage(imageUrl: String) {

        if (selectedId.toInt() == 0) {
            Toast.makeText(context, "Please select a user.", Toast.LENGTH_SHORT).show()
            return
        }
        val taskObj = AssignTask()
        taskObj.subject = binding.taskDescription.text.toString()
        taskObj.userId = selectedId

        progressDialog.showDialog()

        taskObj.fileUrl = imageUrl

        val call = apiInterface.assignTaskUser(token, selectedId, taskObj)
        call.enqueue(object : Callback<AssignTask?> {
            override fun onResponse(call: Call<AssignTask?>, response: Response<AssignTask?>) {
                if (response.isSuccessful) {
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, "Task assigned successfully.", Toast.LENGTH_SHORT)
                            .show()
                    selectedId = 0
                    binding.selectEmployee.setText("")
                    binding.taskDescription.setText("")
                    binding.deadline.setText("  Select Date ")
                    uri = null
                } else {
                    progressDialog.dismissDialog()
                    if (isAdded) Toast.makeText(context, "Some Error Occurred", Toast.LENGTH_SHORT)
                        .show()

                }
            }

            override fun onFailure(call: Call<AssignTask?>, t: Throwable) {
                Log.e("TAG", "onFailure: " + "Network Error: " + t.message)
                progressDialog.dismissDialog()
                if (isAdded)
                    Toast.makeText(context, "Network Error: ", Toast.LENGTH_SHORT).show()

            }
        })
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
            binding.deadline.setText(selectedDate)

        }
    }


}
