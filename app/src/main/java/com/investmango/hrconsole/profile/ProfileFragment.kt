package com.investmango.hrconsole.profile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentProfile2Binding
import com.investmango.hrconsole.manager.activity.ManagerActivity
import com.investmango.hrconsole.model.User
import com.investmango.hrconsole.newHomePage.ChangePaasword
import com.investmango.hrconsole.service.DateAndTimeUtility
import com.investmango.hrconsole.service.LoginActivity
import com.investmango.hrconsole.service.SharedUtils
import com.investmango.hrconsole.user.fragment.UserAttendanceFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.atomic.AtomicReference


class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfile2Binding
    lateinit var apiInterface: ApiInterface
    lateinit var user: User
    var token: String = ""
    var ViewOf: String? = ""
    private var userId: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var preferences = context?.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences?.getString("token", "0").toString()

        if (arguments != null) {
            if (arguments!!.containsKey("childUserid")) {
                ViewOf = arguments!!.getString("ViewOf")
                userId = arguments!!.getLong("childUserid")
                Log.e("Achievements", "onCreate: $userId")
            }
        }
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
    }

    fun getCurrentUser(token: String) {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        val call: Call<User> = apiInterface.getCurrentUser(token)
        call.enqueue(object : Callback<User?> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful) {
                    user = response.body()!!
                    setupData()
                } else {
                    Toast.makeText(
                        context,
                        "Something went wrong.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                val errorMessage = "Error: " + t.message
                Log.e("LoginError", errorMessage)
            }
        })
    }

    fun getChildUser(userID: Long) {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        val call: Call<User> = apiInterface.getChildUser(userId)
        call.enqueue(object : Callback<User?> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful) {
                    Log.e("userResponse", "onResponse: "+ response.body() + userId)
                    if (response.body()!=null) {
                        user = response.body()!!
                        setupData()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Something went wrong.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                val errorMessage = "Error: " + t.message
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

        if (requireContext()!=null) {
            try {
                Glide.with(requireContext()).load(user.profileImage).into(binding.profilePhoto)
            } catch (e: Exception) {
                Log.e("TAG", "setupData: " + e)
            }
        }
        Log.e("lastLogin", "setupData: "+user.lastLogin )

//        binding.lastlogin.setText(
//            DateAndTimeUtility.getDateFromLong(user.lastLogin).toString() + " " + "hours ago"
//        )
        binding.lastlogin.setText(
            DateAndTimeUtility.getRelativeTime(user.lastLogin).toString()
        )
        binding.Dob.setText(user.dob)
        binding.joiningDate.setText(user.createdDate.toString())
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
        UserAttendanceFragment.stopLocationUpdate()
        (activity as (ManagerActivity)).finish()
        val i = Intent(context, LoginActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        // on below line calling a method to start the activity
        startActivity(i)
    }
}