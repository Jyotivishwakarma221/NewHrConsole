package com.investmango.hrconsole.manager.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.Observable
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.datepicker.MaterialDatePicker
import com.investmango.hrconsole.Adapter.fileAdapter
import com.investmango.hrconsole.AwsUpload.UploadFileAws
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.cloudinary.CloudinaryConfig
import com.investmango.hrconsole.databinding.FragmentSubTaskBinding
import com.investmango.hrconsole.model.AddSubTask
import com.investmango.hrconsole.model.AssignmentItem
import com.investmango.hrconsole.model.UsersItem
import com.investmango.hrconsole.service.DateAndTimeUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Objects

class AddSubTaskFragment : Fragment() {
    private lateinit var binding: FragmentSubTaskBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var authority: String = ""
    var uri: Uri? = Uri.parse("")
    lateinit var nameIndex: String
    var sizeIndex: Long = 0
    var uriStr : ArrayList<String> = arrayListOf()
    lateinit var file1:File
    private var allActiveUsers: List<UsersItem?>? = null
    var selectedId: Long = 0
    var projectId: Int = 0
    lateinit var progressDialog: AwesomeProgressDialog
    lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var  pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private val apiKey = "974981595445112"
    private val apiSecret = "4URnjaut9IehzWDZZ8_AVH8pKoQ"
    var publicId = ""
    private var from: String? = null
    lateinit var image: MultipartBody.Part


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        CloudinaryConfig.initCloudinary(requireContext())
        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0")!!
        authority = preferences.getString("Authority", "user").toString()
        userId = preferences.getLong("userId", 0)

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)

        if (arguments != null) {
            from = arguments!!.getString("ViewOf")
            Log.e("ViewOf", "onCreate: " + from)
            if (arguments!!.getInt("id") != null)
                projectId = arguments!!.getInt("id")
        }

//        launcher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
//                uri = result.data!!.data!!
//                Log.e("launcherrrr", "onCreate: " + uri)
//
//                result.data?.let { returnUri ->
//                    context?.contentResolver?.query(uri!!, null, null, null, null)
//                }?.use { cursor ->
//                    /*
//                     * Get the column indexes of the data in the Cursor,
//                     * move to the first row in the Cursor, get the data,
//                     * and display it.
//                     */
//                    nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).toString()
//                    val size = cursor.getColumnIndex(OpenableColumns.SIZE)
//                    cursor.moveToFirst()
//                    sizeIndex = cursor.getLong(size)
//                    Log.e("launcherrrr", "onCreate: " + sizeIndex)
//
//                    cursor.moveToFirst()
//
//                     file1= File(Objects.requireNonNull<String>(UploadFileAws().getRealPathFromUri(uri!!,context!!)))
//                     val requestBody1 = RequestBody.create("image/*".toMediaTypeOrNull(), file1)
//                     image = MultipartBody.Part.createFormData("image", file1.name, requestBody1)
//                     Log.e("khushi1111", "onClick: " + image)
//
//                    if (isAdded)
//                        CoroutineScope(Dispatchers.Main).launch {
//                            uriStr = UploadFileAws().uploadFile(file1, "subtasksDocs", context!!).toString()
//                            if (uriStr != "") {
//                                // Handle the success case here
//                                Log.e("uploadimg", "onCreate: "+uriStr )
//                                binding.uploadDocname.visibility = View.VISIBLE
//                                binding.uploadDoc.visibility = View.GONE
//                            } else {
//                                // Handle the failure case here
//                                Toast.makeText(
//                                    context,
//                                    "Some error in uploading .",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//
//                }
//            }
//
//        };
//

         pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
                // Callback is invoked after the user selects media items or closes the
                // photo picker.
                if (uris.isNotEmpty()) {
                    Log.e("PhotoPicker", "Number of items selected: ${uris.size}")
                    for( i in 0..uris.size-1 ) {
                        file1 = File(
                            Objects.requireNonNull<String>(
                                UploadFileAws().getRealPathFromUri(
                                    uris[i],
                                    context!!
                                )
                            )
                        )
                        val requestBody1 = RequestBody.create("image/*".toMediaTypeOrNull(), file1)
                        image = MultipartBody.Part.createFormData("image", file1.name, requestBody1)
                        Log.e("khushi1111", "onClick: " + image)
                        if (isAdded)
                            CoroutineScope(Dispatchers.Main).launch {
                                progressDialog.showDialog()
                               var url =
                                    UploadFileAws().uploadFile(file1, "subtasksDocs", context!!)
                                        .toString()
                                uriStr.add(url)
                                if (url != "") {
                                    // Handle the success case here
                                    Log.e("uploadimg", "onCreate: " + uriStr)

                                    progressDialog.dismissDialog()

                                    if (isAdded)
                                        binding.fileRecycler.adapter = fileAdapter(requireContext(), uriStr)
                                    binding.fileRecycler.layoutManager =
                                        GridLayoutManager(context, 2)
                                } else {
                                    progressDialog.dismissDialog()
                                    // Handle the failure case here
                                    Toast.makeText(
                                        context,
                                        "Some error in uploading .",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                            }
                    }

                } else {
                    Log.e("PhotoPicker", "No media selected")
                }
            }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            context!!.contentResolver.query(uri, projection, null, null, null) ?: return null


        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_sub_task, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!from.equals("Own")) {
            getActiveUser()
            binding.selectEmployee.visibility = View.VISIBLE
            binding.selectText.visibility = View.VISIBLE
        } else {
            binding.selectEmployee.visibility = View.GONE
            binding.selectText.visibility = View.GONE
        }

        binding.selectEmployee.setOnItemClickListener { parent, view, position, id ->
            selectedId = allActiveUsers?.get(position)?.assignToId?.toLong()!!
        }
        binding.deadline.setOnClickListener {
            setDatePicker()
        }
        binding.time.setOnClickListener {
            setClock()
        }
        binding.uploadDoc.setOnClickListener {
            openGallery()
        }

        binding.delete.setOnClickListener {
            binding.taskDescription.setText("")
            binding.deadline.setText("Select Date ")
            if (!uriStr.isEmpty()) {
                progressDialog.showDialog()
                // Delete the old image from Cloudinary in a background thread
                CoroutineScope(Dispatchers.Main).launch {
                    for (i in 0 until uriStr.size) {
                        uriStr[i].let { UploadFileAws().deleteFile(it, context!!) }
                    }
                        uriStr.clear()
                    binding.fileRecycler.adapter!!.notifyDataSetChanged()
                    progressDialog.dismissDialog()
                }

                binding.taskDescription.setText("")
//            activity?.runOnUiThread(Runnable {
//                deleteImageFromCloudinary(publicId)
//
//            })
            }
        }

        binding.assignTask.setOnClickListener {
            if (binding.taskDescription.text.isEmpty() || binding.taskname.text.isEmpty()) {
                Toast.makeText(context, "Please fill some information.", Toast.LENGTH_SHORT).show()
            } else {
                try {

                    if (isValid(binding.deadline.text.toString()))
                        sendTask(uriStr)
                    else Toast.makeText(
                        context,
                        "Fill All fields properly.",
                        Toast.LENGTH_SHORT
                    ).show()

                } catch (e: Exception) {
                    Log.e("Exception", "onViewCreated: " + e.message)
                }
            }
        }

    }

    private fun deleteImageFromAWS(publicId: List<String>) {

    }

    private fun getActiveUser() {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<AssignmentItem>? = apiInterface.getAssigmentById(projectId)
        call?.enqueue(object : Callback<AssignmentItem?> {
            override fun onResponse(
                call: Call<AssignmentItem?>,
                response: Response<AssignmentItem?>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    Log.e("getmeetings", "onResponse: ")
                    allActiveUsers = response.body()!!.users
                    val userNames: MutableList<String> = ArrayList()
                    for (user in allActiveUsers!!) {
                        userNames.add(user?.assignToName!!.uppercase())
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

            override fun onFailure(call: Call<AssignmentItem?>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendTask(imageUrl: List<String>) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface

        Log.e("imageUrl", "sendTask: "+ imageUrl )
        progressDialog.showDialog()

        val taskObj = AddSubTask()
        if (imageUrl.isNotEmpty())
                taskObj.fileurl=imageUrl


        taskObj.subtaskDescription = binding.taskDescription.text.toString()
        taskObj.assignById = userId


        if (selectedId != 0L)
            taskObj.assignToId = selectedId
        else
            taskObj.assignToId = userId

        taskObj.subtaskName = binding.taskname.text.toString()

        taskObj.taskDeadline =
            DateAndTimeUtility.convertToEpochMillis(
                binding.deadline.text.toString(),
                binding.selectedTime.text.toString()
            )
        Log.e(
            "imageUrl",
            "sendTask: " + taskObj)

        val call = apiInterface.addSubTask(projectId, taskObj)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                progressDialog.dismissDialog()

                if (response.isSuccessful) {
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(
                            requireContext(),
                            "SubTask added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    binding.taskDescription.setText("")
                    uri = null
                    uriStr.clear()
                    binding.fileRecycler.adapter!!.notifyDataSetChanged()
                    binding.deadline.text = "Select Date "
                    binding.selectedTime.text = "Select Time "
                    binding.taskname.setText("")
                    selectedId = 0
                    binding.selectEmployee.setText("")

                } else {
                    progressDialog.dismissDialog()
                    Log.e("subtask", "onResponse: " + response.message())
                    if (isAdded)
                        Toast.makeText(
                            requireContext(),
                            ("Something went wrong."),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                progressDialog.dismissDialog()
                if (isAdded)
                    Toast.makeText(requireContext(), "Some error occurred.", Toast.LENGTH_SHORT)
                        .show()
                Log.e("failure", "onFailure: " + t.message)
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
        val timePickerDialog = android.app.TimePickerDialog(
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
//        launcher.launch(galleryIntent)

        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))

    }

}