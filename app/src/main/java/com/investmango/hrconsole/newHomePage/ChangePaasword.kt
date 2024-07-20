package com.investmango.hrconsole.newHomePage

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentChangePaaswordBinding
import com.investmango.hrconsole.service.LoginActivity
import com.investmango.hrconsole.service.Popup
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class ChangePaasword : Fragment() {
    lateinit var binding: FragmentChangePaaswordBinding
    lateinit var apiInterface: ApiInterface
    var email: String = "email"
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.takeIf { it.containsKey("email") }?.apply {
            Log.e("arguments", "onViewCreated: " + getString("email"))

            email = getString("email").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_change_paasword,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chngePassword.setOnClickListener {
            Log.e("chngePasswrd", "onViewCreated:2 " + email)

            if (binding.psswrd.text.toString() == binding.confirmPsswrd.text.toString() && !binding.psswrd.text.equals("")) {
                if (email.equals("email")) {
                    changePasswordApiCall()
                } else {
                    chngePassword()
                    Log.e("chngePasswrd", "onViewCreated: " + email)
                }
            } else Toast.makeText(context, "Password is not same or Empty.", Toast.LENGTH_LONG).show()
        }

        binding.confirmPsswrdEye.setOnClickListener {
            togglePasswordVisibility(
                binding.confirmPsswrd,
                binding.confirmPsswrdEye
            )
        }
        binding.passwordeye.setOnClickListener {
            togglePasswordVisibility(
                binding.psswrd,
                binding.passwordeye
            )
        }
//        if (binding.psswrd.text.length >= 8) {
//            binding.char8.visibility = View.VISIBLE
//
    }


    fun togglePasswordVisibility(edittext: EditText, passwordToggle: ImageButton) {
        if (passwordVisible) {
            edittext.setTransformationMethod(PasswordTransformationMethod.getInstance())
            passwordToggle.setImageResource(R.drawable.ic_open_eye)
        } else {
            edittext.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
            passwordToggle.setImageResource(R.drawable.ic_close_eye)
        }
        passwordVisible = !passwordVisible
        // Move the cursor to the end of the password EditText
        edittext.setSelection(edittext.getText().length)
    }

    fun chngePassword() {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val verifyCall = apiInterface.resetPassword(
            "khushisaxena@investmango.com",
            binding.psswrd.text.toString()
        )
        verifyCall.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        try {
                            // Parse the response body if it's not null
                            val responseBodyString = response.body()!!.string()
                            val responseObject = JSONObject(responseBodyString)
                            val apiMessage = responseObject.getString("message")
                            Toast.makeText(context, apiMessage, Toast.LENGTH_SHORT)
                                .show()

                            context?.startActivity(Intent(context, LoginActivity::class.java))

                        } catch (e: IOException) {
                            e.printStackTrace()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        // Handle the case where the response body is null
                        Toast.makeText(
                            context,
                            "Response body is empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                if (t is IOException) {
                    // Network error
                    Toast.makeText(
                        context,
                        "Network error. Please check your internet connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Non-network error
                    val errorMessage = "Failed to verify OTP"

                    Log.e("API_ERROR", "Error: " + t.message)

                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun changePasswordApiCall() {
        val preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val userId = preferences.getLong("userId", 0)
        val apiClient = ApiClient(requireContext())

        apiInterface = apiClient.apiInterface

        val call = apiInterface.updatePassword(userId, binding.psswrd.text.toString())
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    // Handle successful update
                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_LONG)
                        .show()
                    fragmentManager?.beginTransaction()?.remove(this@ChangePaasword)?.commit()
                } else {
                    // Handle error response
                    Toast.makeText(
                        context,
                        "Password update failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                // Handle API call failure
                Toast.makeText(context, "Network error. Please try again later.", Toast.LENGTH_LONG)
                    .show()
                // Dismiss the dialog in case of failure
            }
        })
    }


}