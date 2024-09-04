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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.datepicker.MaterialDatePicker
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
    var uriStr = ""
    private var allActiveUsers: List<UsersItem?>? = null
    var selectedId: Long = 0
    var projectId: Int = 0
    lateinit var progressDialog: AwesomeProgressDialog
    lateinit var launcher: ActivityResultLauncher<Intent>
    private val apiKey = "974981595445112"
    private val apiSecret = "4URnjaut9IehzWDZZ8_AVH8pKoQ"
    var publicId = ""
    private var from: String? = null
    lateinit var image:MultipartBody.Part


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

        if (arguments != null) {
            from = arguments!!.getString("ViewOf")
            Log.e("ViewOf", "onCreate: " + from)
            if (arguments!!.getInt("id") != null)
                projectId = arguments!!.getInt("id")
        }

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

                    val file1 = File(Objects.requireNonNull<String>(getRealPathFromUri(uri!!)))
                    val requestBody1 = RequestBody.create("image/*".toMediaTypeOrNull(), file1)
                   image  = MultipartBody.Part.createFormData("image", file1.name, requestBody1)
                    Log.e("khushi1111", "onClick: " + image)

//                    uploadImage(uri.toString())

                    uploadImageToCloud(uri)

                }
            }

        };

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
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_sub_task, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

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

        binding.uploadDoc.setOnClickListener {
            openGallery()
        }

        binding.delete.setOnClickListener {
            binding.taskDescription.setText("")
            binding.deadline.setText("Select Date ")
            if (!publicId.isEmpty()) {
                // Delete the old image from Cloudinary in a background thread
                Thread {
                    deleteImageFromCloudinary(publicId)
                }.start()

                binding.uploadDocname.visibility = View.GONE
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

    private fun uploadImage(filename:String) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface

        val jsonObject = JSONObject()
        filename?.let {
            if (it.isNotEmpty()) {
                jsonObject.put("file", it)
            }
        }

        val jsonString = jsonObject.toString()
        val requestBody= jsonString.toRequestBody("application/json".toMediaTypeOrNull())

        Log.e("UPLOADimg", "uploadImage: "+ image + " "+ jsonString)
        val call: Call<String>? = apiInterface.saveImage(image,"subtasksDocs")
        call?.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String?>,
                response: Response<String?>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    Log.e("UPLOADimg", "onResponse: "+ response.message())

                } else {
                    Log.e("UPLOADimg", "onResponse: " + response.body().toString())
                    if (isAdded)
                        Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                if (isAdded)
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("UPLOADimg", "onFailure: " + t.message)
            }
        })
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
    private fun sendTask(imageUrl: String) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface

        progressDialog.showDialog()

        val taskObj = AddSubTask()
        taskObj.subtaskDescription = binding.taskDescription.text.toString()
        taskObj.assignById = userId

        if (selectedId != 0L)
            taskObj.assignToId = selectedId
        else
            taskObj.assignToId = userId


        taskObj.fileUrl = imageUrl
        taskObj.subtaskName = binding.taskname.text.toString()
        Log.e(
            "imageUrl",
            "sendTask: " + DateAndTimeUtility.convertToEpochMillis(binding.deadline.text.toString())
        )

        taskObj.taskDeadline =
            DateAndTimeUtility.convertToEpochMillis(binding.deadline.text.toString())
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
                    uriStr = ""
                    binding.deadline.text = "Select Date "
                    binding.uploadDocname.visibility = View.GONE
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

    fun deleteImageFromCloudinary(publicId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    // Initialize Cloudinary with your cloud name, API key, and API secret
                    val cloudinary = Cloudinary("cloudinary://$apiKey:$apiSecret@dzvsmmraz")

                    // Delete the image from Cloudinary
                    cloudinary.uploader().destroy(publicId, null)
                }
                Log.d("Cloudinary", "Image deleted successfully from Cloudinary-----$publicId")
            } catch (e: IOException) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                Log.e("Cloudinary", "Error deleting image from Cloudinary: " + e.message)
            }
        }
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
        publicId = folderName + "/" + System.currentTimeMillis()
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
                    Log.e("uriStr", "onSuccess: " + uriStr)
                    Toast.makeText(
                        requireContext(),
                        "Image uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Dismiss progress dialog when upload is successful
                    progressDialog.dismiss()
                    binding.uploadDocname.visibility = View.VISIBLE
                    binding.uploadDocname.text = "img. " + nameIndex
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
        launcher.launch(galleryIntent)
    }

}