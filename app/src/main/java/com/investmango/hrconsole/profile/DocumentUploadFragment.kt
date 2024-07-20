package com.investmango.hrconsole.profile

import android.app.Activity
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.cloudinary.CloudinaryConfig
import com.investmango.hrconsole.databinding.FragmentDocumentUploadBinding
import com.investmango.hrconsole.manager.activity.ManagerActivity
import com.investmango.hrconsole.model.DocumentModel
import com.investmango.hrconsole.model.DocumentResponse
import com.investmango.hrconsole.model.UrlsItem
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class DocumentUploadFragment : Fragment() {
    private lateinit var binding: FragmentDocumentUploadBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var categoryyy: String = ""
    private var successfulUploadCount = 0
    lateinit var documents: List<UrlsItem?>
    var documentsToUpload: ArrayList<UrlsItem?> = arrayListOf()
    var cloudinaryUrls: ArrayList<String> = ArrayList()
    lateinit var launcher: ActivityResultLauncher<Intent>
    var uri: Uri? = null
    lateinit var progressDialog: ProgressDialog
    private var documentTypeUriMap: HashMap<String, Uri> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CloudinaryConfig.initCloudinary(requireContext())

        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
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
//                    /*
//                     * Get the column indexes of the data in the Cursor,
//                     * move to the first row in the Cursor, get the data,
//                     * and display it.
//                     */
                    var nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).toString()
                    val size = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    var sizeIndex = cursor.getLong(size)
                    Log.e("launcherrrr", "onCreate: " + sizeIndex)

                    cursor.moveToFirst()
                }
                uploadImageToCloud(uri)
            }

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_document_upload, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            getAlldocument()

        } catch (e: Exception) {
            Log.e("expection", "onCreate: " + e.message)
        }
        onclickListners()

    }

    fun viewDocument(url: String) {
        val fragment = ViewDocumentFragment()
        fragment.arguments = Bundle().apply {
            putString("url", url)
        }
        (activity as ManagerActivity).replaceFragment(fragment)
    }

    private fun getAlldocument() {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<DocumentResponse>? = apiInterface.getdocument(userId)
        call?.enqueue(object : Callback<DocumentResponse?> {
            override fun onResponse(
                call: Call<DocumentResponse?>,
                response: Response<DocumentResponse?>,
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    if (response.body()!!.urls?.get(0)?.imageUrl != null) {
                        documents = response.body()?.urls!!
                        Log.e("getmeetings", "onResponse: " + response.body())
                        chekDocuments()
                    }
                } else {
                    Log.e("getmeetings", "onResponse: " + response.errorBody())

                    Toast.makeText(context, getErrorMessage(response), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DocumentResponse?>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    private fun getErrorMessage(response: Response<DocumentResponse?>): String {
        var errorMessage = "Unknown error"
        try {
            val errorJson = JSONObject(response.errorBody()!!.string())
            errorMessage = errorJson.optString("message", errorMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return errorMessage
    }

    fun chekDocuments() {
        for (i in 0..documents.size - 1) {
            documentTypeUriMap.put(
                documents.get(i)?.category!!,
                Uri.parse(documents.get(i)?.imageUrl)
            )
            when (documents.get(i)?.category) {
                "graduation" -> {
                    binding.editPostGraduation.visibility = View.VISIBLE
                    binding.uploadPostGraduation.visibility = View.GONE

                }

                "intermediate" -> {
                    binding.editmarksheet12.visibility = View.VISIBLE
                    binding.uploadmarksheet12.visibility = View.GONE

                }

                "matriculation" -> {
                    binding.editmarksheet10.visibility = View.VISIBLE
                    binding.uploadmarksheet10.visibility = View.GONE

                }

                "pan" -> {
                    binding.editPan.visibility = View.VISIBLE
                    binding.uploadpan.visibility = View.GONE

                }

                "aadhar" -> {
                    binding.editAdhar.visibility = View.VISIBLE
                    binding.uploadAdhar.visibility = View.GONE
                }

                "ExperienceLetter" -> {
                    binding.editExperienceLetter.visibility = View.VISIBLE
                    binding.uploadExperienceLetter.visibility = View.GONE

                }

                "offer" -> {
                    binding.editLastCompanyOffer.visibility = View.VISIBLE
                    binding.uploadLastCompanyOffer.visibility = View.GONE
                }
            }
        }
    }

    fun onclickListners() {

        binding.uploadAdhar.setOnClickListener { openGallery("aadhar") }
        binding.uploadpan.setOnClickListener { openGallery("pan") }
        binding.uploadGraduation.setOnClickListener { openGallery("graduation") }
        binding.uploadAppointmentletter.setOnClickListener { openGallery("appointmentLetter") }
        binding.uploadSalarySlipn.setOnClickListener { openGallery("salarySlip") }
        binding.uploadExperienceLetter.setOnClickListener { openGallery("ExperienceLetter") }
        binding.uploadmarksheet12.setOnClickListener { openGallery("matriculation") }
        binding.uploadmarksheet10.setOnClickListener { openGallery("intermediate") }
        binding.uploadPostGraduation.setOnClickListener { openGallery("postgraduation") }
        binding.uploadLastCompanyOffer.setOnClickListener { openGallery("offer") }

        binding.chngAdhar.setOnClickListener { openGallery("aadhar") }
        binding.chngPan.setOnClickListener { openGallery("pan") }
        binding.chngGraduation.setOnClickListener { openGallery("graduation") }
        binding.chngAppointmnt.setOnClickListener { openGallery("appointmentLetter") }
        binding.chngSaralyslip.setOnClickListener { openGallery("salarySlip") }
        binding.chngExperience.setOnClickListener { openGallery("ExperienceLetter") }
        binding.chng12marksheet.setOnClickListener { openGallery("matriculation") }
        binding.chng10marksheet.setOnClickListener { openGallery("intermediate") }
        binding.chngPostgra.setOnClickListener { openGallery("postgraduation") }
        binding.chnglastCompOffer.setOnClickListener { openGallery("offer") }


        binding.editAdhar.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("aadhar").toString()
            )
        }
        binding.editPan.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("pan").toString()
            )
        }
        binding.editGraduation.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("graduation").toString()
            )
        }
        binding.editAppointmentletter.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("appointmentLetter").toString()
            )
        }
        binding.editSalarySlip.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("salarySlip").toString()
            )
        }
        binding.editExperienceLetter.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("ExperienceLetter").toString()
            )
        }
        binding.editmarksheet12.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("matriculation").toString()
            )
        }
        binding.editmarksheet10.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("intermediate").toString()
            )
        }
        binding.editPostGraduation.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("postgraduation").toString()
            )
        }
        binding.editLastCompanyOffer.setOnClickListener {
            viewDocument(
                documentTypeUriMap.get("offer").toString()
            )
        }

    }


    private fun openGallery(category: String) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        launcher.launch(galleryIntent)
        categoryyy = category
    }

    private fun uploadImageToCloud(imageUri: Uri?) {
//        val fileSize: Long = getFileSize(imageUri)
        progressDialog = ProgressDialog.show(requireContext(), "", "Uploading image...", true)
        val folderName = "document"
        val publicId = folderName + "/" + System.currentTimeMillis()
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
                    if (resultData.containsKey("secure_url")) {
                        val cloudinaryUrl = resultData["secure_url"] as String?
                        if (cloudinaryUrl != null) {
                            successfulUploadCount++
                            documentsToUpload.add(UrlsItem(categoryyy, cloudinaryUrl))
                            if (successfulUploadCount == documentsToUpload.size)
                                saveUserDoc(documentsToUpload)
                        }
                    }


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

    private fun saveUserDoc(cloudinaryUrls: ArrayList<UrlsItem?>) {
        val documentModel = DocumentResponse(cloudinaryUrls)
        val call = apiInterface.saveDocument(documentModel, userId)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(
                call: Call<String?>,
                response: Response<String?>,
            ) {
                if (response.isSuccessful) {
                    val res = response.body()?.contains("message")
                    Log.e("SaveDocumentError", "onResponse: " + res)
                    Toast.makeText(
                        requireContext(),
                        "Image uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Dismiss progress dialog when upload is successful
                    progressDialog.dismiss()
                } else {
                    try {
                        val errorMessage = response.errorBody()!!.string()
                        Log.e(
                            "SaveDocumentError",
                            "API call not successful. Error Message: $errorMessage"
                        )
                        // Extract the message from the JSON response
                        val errorJson = JSONObject(errorMessage)
                        val message = errorJson.optString("message")
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e("SaveDocumentError", "Error parsing error response: " + e.message)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.e("SaveDocumentError", "Error parsing error response: " + e.message)
                    }
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                // Handle API call failure
                val failureMessage = "API call failed: " + t.message
                Log.e("SaveDocumentError", failureMessage)
                Toast.makeText(
                    requireContext(),
                    "Failed to upload document. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    override fun onResume() {
        super.onResume()

    }

}