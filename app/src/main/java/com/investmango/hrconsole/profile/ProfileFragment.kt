package com.investmango.hrconsole.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.bumptech.glide.Glide
import com.investmango.hrconsole.AwsUpload.UploadFileAws
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentProfile2Binding
import com.investmango.hrconsole.manager.activity.AttendanceFragment
import com.investmango.hrconsole.manager.activity.ManagerActivity
import com.investmango.hrconsole.model.User
import com.investmango.hrconsole.newHomePage.ChangePaasword
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.LoginActivity
import com.investmango.hrconsole.service.SharedUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Objects
import java.util.concurrent.atomic.AtomicReference


class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfile2Binding
    lateinit var apiInterface: ApiInterface
    lateinit var user: User
    var token: String = ""
    var ViewOf: String? = ""
    private var userId: Long = 0
    lateinit var progressDialog: AwesomeProgressDialog
    lateinit var launcher: ActivityResultLauncher<Intent>
    var uri: Uri? = Uri.parse("")
    lateinit var nameIndex: String
    var sizeIndex: Long = 0
    lateinit var file1: File
    var uriStr = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var preferences = context?.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences?.getString("token", "0").toString()

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)


        if (arguments != null) {
            if (arguments!!.containsKey("childUserid")) {
                ViewOf = arguments!!.getString("ViewOf")
                userId = arguments!!.getLong("childUserid")
                Log.e("Achievements", "onCreate: $userId")
            }
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
                    file1 = File(
                        Objects.requireNonNull<String>(
                            UploadFileAws().getRealPathFromUri(
                                uri!!,
                                context!!
                            )
                        )
                    )


                    CoroutineScope(Dispatchers.Main).launch {
                        uriStr =
                            UploadFileAws().uploadFile(file1, "profilePhotos", context!!)
                                .toString()
                        if (uriStr != "") {
                            uploadFile(uriStr)
                        } else {
                            // Handle the failure case here
                            Toast.makeText(
                                context,
                                "Some error in uploading .",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
//                                deleteImageFromCloudinary(arrOfStr?.get(6).toString())
//                                uploadImageToCloud(uri)
                }
            }
        }
    }

    private fun uploadFile(uri: String) {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()
        val call: Call<ResponseBody> = apiInterface.uploadFile(user.id, uri)
        call.enqueue(object : Callback<ResponseBody> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
//                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(context, "Profile Updated Successfully.", Toast.LENGTH_SHORT)
                            .show()
                        getCurrentUser(token)

                }else{
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(
                            context,
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressDialog.dismissDialog()
                val errorMessage = "Error: " + t.message
                Log.e("LoginError", errorMessage)
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile2, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ViewOf.equals("child")) {
            binding.signOut.visibility = View.GONE
            binding.saveChanges.visibility = View.GONE
            binding.changePassword.visibility = View.GONE
            binding.userDocument.visibility = View.GONE
            binding.firstLay.visibility = View.GONE

            if (userId != null)
                getChildUser(userId)

        } else {
            getCurrentUser(token)

            binding.userDocument.setOnClickListener {
                (activity as (ManagerActivity)).replaceFragment(
                    DocumentUploadFragment()
                )
            }
            binding.signOut.setOnClickListener { logout() }
            binding.changePassword.setOnClickListener {
                (activity as (ManagerActivity)).replaceFragment(
                    ChangePaasword()
                )
            }
        }


        binding.profilePhoto.setOnClickListener {
            UploadFileAws().openGallery(launcher)
        }
    }

    fun getCurrentUser(token: String) {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()
        val call: Call<User> = apiInterface.getCurrentUser()
        call.enqueue(object : Callback<User?> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful) {

                    user = response.body()!!
                    setupData()
                    progressDialog.dismissDialog()
                } else {
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(
                            context,
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                progressDialog.dismissDialog()
                val errorMessage = "Error: " + t.message
                Log.e("LoginError", errorMessage)
            }
        })
    }

    fun getChildUser(userID: Long) {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()
        val call: Call<User> = apiInterface.getChildUser(userId)
        call.enqueue(object : Callback<User?> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful) {
                    progressDialog.dismissDialog()

                    Log.e("userResponse", "onResponse: " + response.body() + userId)
                    if (response.body() != null) {

                        user = response.body()!!
                        setupData()
                    }
                } else {
                    progressDialog.dismissDialog()
                    if (isAdded)
                        Toast.makeText(
                            context,
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                val errorMessage = "Error: " + t.message
                progressDialog.dismissDialog()

                Log.e("LoginError", errorMessage)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupData() {
        binding.name.setText(user.firstName + " " + user.lastName)
        binding.designation.setText(user.designation)
        binding.Designation2.setText(user.designation)
        binding.userId.setText(user.id.toString())
        binding.phoneNum.setText(user.phone)
        binding.email.setText(user.email)
        binding.gender.setText(user.gender)

        if (isAdded) {
            try {
                Glide.with(requireContext()).load(user.profileImage).into(binding.profilePhoto)
            } catch (e: Exception) {
                Log.e("TAG", "setupData: " + e)
            }
        }
        Log.e("lastLogin", "setupData: " + user.lastLogin)

//        binding.lastlogin.setText(
//            DateAndTimeUtility.getDateFromLong(user.lastLogin).toString() + " " + "hours ago"
//        )
        binding.lastlogin.setText(
            DateAndTimeUtility.getRelativeTime(user.lastLogin).toString()
        )
        binding.Dob.setText(user.dob)
        binding.joiningDate.setText(DateAndTimeUtility.getDATEFromLong(user.createdDate))
        binding.Department.setText(user.department)

        if (user.managerName != null)
            binding.ReportingManager.setText(user.managerName)
    }

    fun logout() {
        // Clear the stored token in SharedPreferences
        val apiClient = ApiClient(context)
        apiClient.logout()
        // Get the saved token from SharedPreferences
        val preferences =
            AtomicReference(
                context?.getSharedPreferences(
                    "my_preferences",
                    AppCompatActivity.MODE_PRIVATE
                )
            )
        val sharedUtils = SharedUtils(context)

        preferences.set(sharedUtils.getSharedPreferencesContext())
        preferences.get()?.edit()?.clear()?.apply()

        val attendanceFragment =
            parentFragmentManager.findFragmentByTag("ATTENDANCE") as? AttendanceFragment
        (attendanceFragment)?.stopLocationUpdate()
        (activity as (ManagerActivity)).stoploactionService()
        (activity as (ManagerActivity)).finish()

        val i = Intent(context, LoginActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        // on below line calling a method to start the activity
        startActivity(i)
    }
}