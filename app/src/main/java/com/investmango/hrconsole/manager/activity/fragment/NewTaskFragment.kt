package com.investmango.hrconsole.manager.activity.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.cloudinary.CloudinaryConfig
import com.investmango.hrconsole.databinding.FragmentNewTaskBinding
import com.investmango.hrconsole.model.AddTask
import com.investmango.hrconsole.model.TaskItems
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class NewTaskFragment : Fragment() {
    private lateinit var binding: FragmentNewTaskBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var uri: Uri? = null
    lateinit var nameIndex: String
    var sizeIndex: Long = 0
    lateinit var launcher: ActivityResultLauncher<Intent>
    private val apiKey = "974981595445112"
    private val apiSecret = "4URnjaut9IehzWDZZ8_AVH8pKoQ"
    var publicId = ""

    var task: ArrayList<TaskItems> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CloudinaryConfig.initCloudinary(requireContext())
        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0")!!
        userId = preferences.getLong("userId", 0)




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
                }

                if (task != null && task.isEmpty())
                    uploadImageToCloud(uri)
                else {
                    if (!task.get(0).fileUrl.equals("")) {
                        val arrOfStr = task.get(0).fileUrl?.split("/")
//                        Log.e("arrOfStr", "onCreate: " + (arrOfStr?.get(6)))
                        // Your UI update code here

                        activity?.runOnUiThread(Runnable {
                            deleteImageFromCloudinary(arrOfStr?.get(6).toString())
                            uploadImageToCloud(uri)


                        })
                    }
                }
            }

        };
    }

    private fun setData() {

        binding.taskDescription.isVerticalScrollBarEnabled = true
        binding.taskDescription.movementMethod = ScrollingMovementMethod()
        if (!task.isEmpty() && task!=null)
        binding.taskDescription.setText(task.get(0).comments);
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = this.arguments
        if (bundle?.getSerializable("editTask") != null) {
            task = bundle.getSerializable("editTask") as ArrayList<TaskItems>
//            Log.e("getExtras", "onCreate: " + task.get(0).id)
            setData()
        }

        binding.uploadDoc.setOnClickListener {
            openGallery()
        }

        binding.delete.setOnClickListener {
            binding.taskDescription.setText("")
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
            if (binding.taskDescription.text.isEmpty()) {
                Toast.makeText(context, "Please fill some information.", Toast.LENGTH_SHORT).show()
            } else {
                if (!task.isEmpty()) {
                    try {
                        addComment(task[0].id?.toLong()!!, uri.toString())
                    } catch (e: Exception) {
                        Log.e("Exception", "onViewCreated: " + e.message)
                    }
                } else {

                    try {
                        sendTask(uri.toString())

                    } catch (e: Exception) {
                        Log.e("Exception", "onViewCreated: " + e.message)
                    }
                }
            }
        }
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
                    var uriStr = uri?.toString()
                    uriStr = resultData["url"] as String?
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

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        launcher.launch(galleryIntent)
    }

    private fun addComment(taskId: Long, imageUrl: String) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val taskObj = AddTask()
        taskObj.id = taskId
        taskObj.comments = binding.taskDescription.text.toString()
        if (imageUrl != null) {
            taskObj.fileUrl = imageUrl
        }
        val call = apiInterface.addComment(token, taskObj, userId)
        call.enqueue(object : Callback<AddTask?> {
            override fun onResponse(call: Call<AddTask?>, response: Response<AddTask?>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Comment added successfully", Toast.LENGTH_SHORT).show()
                    getActivity()?.onBackPressed()

                } else {
                    Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AddTask?>, t: Throwable) {
                Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendTask(imageUrl: String) {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val taskObj = AddTask()
        taskObj.subject = binding.taskDescription.text.toString()
        taskObj.setFileUrl(imageUrl)

        val call = apiInterface.addTask(token, taskObj, userId)
        call.enqueue(object : Callback<AddTask?> {
            override fun onResponse(call: Call<AddTask?>, response: Response<AddTask?>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT)
                        .show()
                    binding.taskDescription.setText("")
                    uri = null


                } else {
                    Toast.makeText(requireContext(), ("Something went wrong."), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<AddTask?>, t: Throwable) {
                Toast.makeText(requireContext(), "Some error occurred.", Toast.LENGTH_SHORT).show()
                Log.e("failure", "onFailure: " + t.message)
            }
        })
    }

}
