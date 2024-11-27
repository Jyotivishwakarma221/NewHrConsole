package com.investmango.hrconsole.newHomePage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentOtpBinding
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class OtpFragment : Fragment() {
    lateinit var binding: FragmentOtpBinding
    lateinit var apiInterface: ApiInterface
    lateinit var email:String
    lateinit var progressDialog: AwesomeProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)

        arguments?.takeIf { it.containsKey("email") }?.apply {
            Log.e("arguments", "onViewCreated: " + getString("email"))

            email = getString("email").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for context fragment
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_otp,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.Verify.setOnClickListener { forgetPasswrd() }
    }

    fun forgetPasswrd() {

        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        // Call the sendOtp API
progressDialog.showDialog()
        val call = apiInterface.verifyOtp(email,binding.otp.text.toString())
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>,
            ) {
                if (response.isSuccessful) {
                    progressDialog.dismissDialog()
                    if (response.body() != null) {
                        try {
                            // Parse the response body if it's not null
                            val responseBodyString = response.body()!!.string()
                            val responseObject = JSONObject(responseBodyString)
                            val apiMessage = responseObject.getString("message")
                            Toast.makeText(context, apiMessage, Toast.LENGTH_SHORT)
                                .show()

                            replaceWWithBundle()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        // Handle the case where the response body is null
                        Toast.makeText(
                            context,
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }else{
                    progressDialog.dismissDialog()
                    Toast.makeText(context,"Something went wrong.",Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                progressDialog.dismissDialog()
                if (t is IOException) {
                    Toast.makeText(
                        context,
                        "Network error. Please check your internet connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Non-network error
                    val errorMessage = "Failed to send OTP. Please try again later."

                    Log.e("API_ERROR", "Error: " + t.message)

                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    fun replaceWWithBundle(){
        val fragment = ChangePaasword()
        val bundles = Bundle()
        bundles.putString("email", email)
        fragment.setArguments(bundles)
        (activity as (ForgetPasswordActivity)).replaceFrag(fragment)
    }

}