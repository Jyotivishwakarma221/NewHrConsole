package com.investmango.hrconsole.manager.activity.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.investmango.hrconsole.Adapter.fileAdapter
import com.investmango.hrconsole.AwsUpload.UploadFileAws
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentNewTaskBinding
import com.investmango.hrconsole.model.AddTask
import com.investmango.hrconsole.model.TaskItems
import com.investmango.hrconsole.model.UpdateTaskStatus
import com.investmango.hrconsole.model.UpdateTaskStatus.Status
import com.investmango.hrconsole.service.DateAndTimeUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Objects
import java.util.regex.Pattern


class NewTaskFragment : Fragment() {
    private lateinit var binding: FragmentNewTaskBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var uri: Uri? = Uri.parse("")
    var uriStr: ArrayList<String> = arrayListOf()
    lateinit var nameIndex: String
    var sizeIndex: Long = 0

    //    var uriStr = ""
    lateinit var progressDialog: AwesomeProgressDialog

    //    lateinit var launcher: ActivityResultLauncher<Intent>
    private val apiKey = "974981595445112"
    private val apiSecret = "4URnjaut9IehzWDZZ8_AVH8pKoQ"
    var publicId = ""
    lateinit var file1: File
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private val TIME_PATTERN: Pattern =
        Pattern.compile("^([0-1][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$")
    lateinit var image: MultipartBody.Part
    var task: ArrayList<TaskItems> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        CloudinaryConfig.initCloudinary(requireContext())
        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0")!!
        userId = preferences.getLong("userId", 0)


        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)


        pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
                // Callback is invoked after the user selects media items or closes the
                // photo picker.
                if (uris.isNotEmpty()) {
                    Log.e("PhotoPicker", "Number of items selected: ${uris.size}")
                    for (i in 0..uris.size - 1) {
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
                                        binding.fileRecycler.adapter =
                                            fileAdapter(requireContext(), uriStr)
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
//                    file1 = File(
//                        Objects.requireNonNull<String>(
//                            UploadFileAws().getRealPathFromUri(
//                                uri!!,
//                                context!!
//                            )
//                        )
//                    )
//
//                    if (task != null && task.isEmpty()) {
//                        progressDialog.showDialog()
//                        if (isAdded) {
//                            CoroutineScope(Dispatchers.Main).launch {
//                                uriStr = UploadFileAws().uploadFile(file1, "taskDocs", context!!)
//                                    .toString()
//                                progressDialog.dismissDialog()
//                                if (uriStr != "") {
//                                    // Handle the success case here
//                                    Log.e("uploadimg", "onCreate: " + uriStr)
//                                    binding.uploadDocname.visibility = View.VISIBLE
//                                    binding.uploadDoc.visibility = View.GONE
//                                } else {
//                                    // Handle the failure case here
//                                    Toast.makeText(
//                                        context,
//                                        "Some error in uploading .",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                        } else {
//                            Log.e("TAG", "onCreate: ")
//                        }
//
//                    } else {
//                        if (!task[0].fileurl.equals("")) {
////                            val arrOfStr = task.get(0).fileUrl?.split("/")
//////                        Log.e("arrOfStr", "onCreate: " + (arrOfStr?.get(6)))
////                            // Your UI update code here
////
////                            CoroutineScope(Dispatchers.Main).launch {
////                                task[0].fileUrl?.let {
////                                    UploadFileAws().deleteFile(
////                                        it,
////                                        context!!
////                                    )
//                        }
//                        CoroutineScope(Dispatchers.Main).launch {
//                            uriStr =
//                                UploadFileAws().uploadFile(file1, "taskDocs", context!!)
//                                    .toString()
//                            if (uriStr != "") {
//                                // Handle the success case here
//                                Log.e("uploadimg", "onCreate: " + uriStr)
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
////                                deleteImageFromCloudinary(arrOfStr?.get(6).toString())
////                                uploadImageToCloud(uri)
//                    }
//                }
//            }
//        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData() {

        binding.taskDescription.isVerticalScrollBarEnabled = true
        binding.taskDescription.movementMethod = ScrollingMovementMethod()
        if (!task.isEmpty() && task != null) {
            if (task.get(0).comments != null && task.get(0).subject != null) {
                binding.taskDescription.setText(
                    task.get(0).subject + "\n \n" + task.get(
                        0
                    ).comments
                )
            } else if (task[0].subject != null) binding.taskDescription.setText(task[0].subject)
            else if (task[0].comments != null) binding.taskDescription.setText(task[0].comments)



            if (task.get(0).deadLine != null && task.get(0).deadLine != 0L) {
                binding.deadline.setText(DateAndTimeUtility.getDATEFromLong(task.get(0).deadLine))
                binding.selectedTime.setText(DateAndTimeUtility.getTimeIn24HourFromLong(task.get(0).deadLine))
            } else binding.deadline.setText("  Select Date ")

            if (task[0].fileurl!!.isNotEmpty()) {
                uriStr = task[0].fileurl as ArrayList<String>
                if (isAdded)
                    binding.fileRecycler.adapter = fileAdapter(context!!, uriStr)
                binding.fileRecycler.layoutManager = GridLayoutManager(context, 2)

//                binding.uploadDocname.visibility = View.VISIBLE
//                binding.uploadDoc.visibility = View.GONE
            } else {
//                binding.uploadDocname.visibility = View.GONE
//                binding.uploadDoc.visibility = View.VISIBLE
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_new_task, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = this.arguments
        if (bundle?.getSerializable("editTask") != null) {
            Log.e("editTask", "onViewCreated: " + bundle.getSerializable("editTask"))

            task = bundle.getSerializable("editTask") as ArrayList<TaskItems>
            setData()
//            Log.e("getExtras", "onCreate: " + task.get(0).id)
        }

        binding.calender.setOnClickListener {
            setDatePicker()
        }
        binding.time.setOnClickListener {
            setClock()
        }
        binding.uploadDoc.setOnClickListener {
            openGallery()
        }

//        binding.deleteUploadedImg.setOnClickListener {
//            Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show()
//            progressDialog.showDialog()
//            if (task.isNotEmpty()) {
//                if (task[0].fileurl!!.isNotEmpty()) {
//                    CoroutineScope(Dispatchers.Main).launch {
//                        for (i in 0..uriStr.size)
//
//                        task[0].fileurl?.get(i)?.let { UploadFileAws().deleteFile(it, context!!) }
//                        progressDialog.dismissDialog()
//
//                        binding.uploadDocname.visibility = View.GONE
//                        binding.uploadDoc.visibility = View.VISIBLE
//
//                    }
//                }
//            }
//        }

        binding.delete.setOnClickListener {
            binding.taskDescription.setText("")
            binding.deadline.setText("Select Date ")
            binding.selectedTime.setText("Select Time ")

//            if (uriStr.isNotEmpty()) {
//                CoroutineScope(Dispatchers.Main).launch {
//                    for (i in 0 until task[0].fileurl!!.size)
//                    task[0].fileurl!!.get(i).let { UploadFileAws().deleteFile(it, context!!) }
//                    binding.uploadDocname.visibility = View.GONE
//                    binding.taskDescription.setText("")
//                }
//            }


        }

        binding.assignTask.setOnClickListener {
            if (binding.taskDescription.text.isEmpty()) {
                Toast.makeText(context, "Please fill some information .", Toast.LENGTH_SHORT).show()
            } else {
                if (!task.isEmpty()) {
                    try {
                        Log.e(
                            "isValidTine",
                            "onViewCreated: " + (binding.deadline.text.toString() + binding.selectedTime.text.toString())
                        )

                        if (isValid(binding.deadline.text.toString()) && isValidTine(binding.selectedTime.text.toString()))
                            UpdateTask(task[0].id?.toLong()!!, task[0].status!!, uriStr)
                        else {
                            Log.e(
                                "isValidTine",
                                "onViewCreated: " + isValid(binding.selectedTime.text.toString())
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("Exception", "onViewCreated: " + e.message)
                    }
                } else {
                    try {
                        Log.e(
                            "selectedTime",
                            "onViewCreated: " + binding.selectedTime.text.toString()
                        )

                        sendTask(uriStr)


                    } catch (e: Exception) {
                        Log.e("Exception", "onViewCreated: " + e.message)
                    }
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun isValidTine(timeStr: String?): Boolean {
        return try {
            // Trim the input and validate using the "HH:mm" format
            LocalTime.parse(timeStr?.trim(), DateTimeFormatter.ofPattern("HH:mm"))
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun UpdateTask(taskId: Long, status: String, imageUrl: List<String>) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface

        progressDialog.showDialog()
        val taskObj = UpdateTaskStatus()
        taskObj.id = taskId

        Log.e("vhbkbl", "UpdateTask: $status")

        if (status == Status.PENDING.toString()) {
            taskObj.status = Status.PENDING
        } else if (status == Status.DONE.toString()) {
            taskObj.status = Status.DONE
        } else if (status == Status.ASSIGNED.toString()) {
            taskObj.status = Status.ASSIGNED
        }

        if (isValid(binding.deadline.text.toString()) && isValidTine(binding.selectedTime.text.toString())) {
            taskObj.deadLine =
                DateAndTimeUtility.convertToEpochMillis(
                    binding.deadline.text.toString(),
                    binding.selectedTime.text.toString()
                )
        }
//        taskObj.deadLine = DateAndTimeUtility.convertToEpochMillis(deadlineText)


        val taskDescription = binding.taskDescription.text.toString()
        if (taskDescription.isNotEmpty()) {
            taskObj.subject = taskDescription
        }

        if (imageUrl.isNotEmpty())
            taskObj.fileUrl = imageUrl


        val call = apiInterface.updateUserTaskStatus(taskObj, userId)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, "Tak updated successfully", Toast.LENGTH_SHORT)
                            .show()
                    getActivity()?.onBackPressed()

                } else {
                    progressDialog.dismissDialog()

                    // Read the error body as a string
                    val errorBodyString = response.errorBody()!!.string()


                    // Convert the string to a JSONObject
                    val errorJson = JSONObject(errorBodyString)
                    val errorMessage = errorJson.getString("message")
                    Log.e("vhbkbl", "onResponse: " + errorMessage)
                    if (isAdded)
                        Toast.makeText(context, "Failed to Update task .", Toast.LENGTH_SHORT)
                            .show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                progressDialog.dismissDialog()
                if (isAdded)
                    Toast.makeText(context, "Failed to Update task .", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendTask(imageUrl: List<String>) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface

        progressDialog.showDialog()

        val taskObj = AddTask()
        taskObj.subject = binding.taskDescription.text.toString()

        if (imageUrl.isNotEmpty())
            taskObj.fileurl = imageUrl


//        else if(imageUrl.equals(null)) taskObj.fileUrl = ""
        Log.e(
            "imageUrl",
            "sendTask: " + DateAndTimeUtility.convertToEpochMillis(binding.deadline.text.toString())
        )
        if (isValid(binding.deadline.text.toString()) && isValidTine(binding.selectedTime.text.toString())) {
            taskObj.deadline =
                DateAndTimeUtility.convertToEpochMillis(
                    binding.deadline.text.toString(),
                    binding.selectedTime.text.toString()
                )
        }

        val call = apiInterface.addTask(taskObj, userId)
        call.enqueue(object : Callback<AddTask?> {
            override fun onResponse(call: Call<AddTask?>, response: Response<AddTask?>) {
                progressDialog.dismissDialog()

                if (response.isSuccessful) {
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(
                            requireContext(),
                            "Task added successfully",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    binding.taskDescription.setText("")
                    uri = null
                    uriStr.clear()
                    if (binding.fileRecycler.adapter!=null)
                    binding.fileRecycler.adapter!!.notifyDataSetChanged()
                    binding.deadline.setText("Select Date ")
                    binding.selectedTime.setText("Select Time ")

//                    binding.uploadDocname.visibility = View.GONE





                } else {
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(
                            requireContext(),
                            ("Something went wrong."),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                }
            }

            override fun onFailure(call: Call<AddTask?>, t: Throwable) {
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
}
