package com.investmango.hrconsole.newHomePage

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.basusingh.beautifulprogressdialog.BeautifulProgressDialog
import com.bumptech.glide.Glide
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentForgetPasswrdBinding
import com.investmango.hrconsole.model.AssignMeeting
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class ForgetPasswrdFragment : Fragment() {
    lateinit var binding: FragmentForgetPasswrdBinding
    lateinit var apiInterface: ApiInterface
    lateinit var email: String
    lateinit var progressBar: ProgressDialog
    lateinit var Dialog: BeautifulProgressDialog
    lateinit var  progressDialog : AwesomeProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)




        progressBar = ProgressDialog(activity, R.drawable.progress_bar)
        progressBar.setProgressDrawable(
            ContextCompat.getDrawable(
                context!!,
                R.drawable.progress_bar
            )
        )
        progressBar!!.setCancelable(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_forget_passwrd,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.requestOtp.setOnClickListener {
            if (binding.emailId.text.toString().equals("")) {
                Toast.makeText(
                    context,
                    "Enter email address.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                forgetPasswrd()
            }
        }

        binding.enter.setOnClickListener {
//            showprogress()
//            progressDialog.showDialog()


//            replaceWithBundle()
        }
    }

    fun replaceWithBundle() {
        val fragment = OtpFragment()
        val bundles = Bundle()
        bundles.putString("email", binding.emailId.text.toString())
        fragment.setArguments(bundles)
        (activity as (ForgetPasswordActivity)).replaceFrag(fragment)
    }

    fun showprogress() {
//     val videouri=   Uri.parse("android.resource://"  + R.raw.hrlogo_animate)
//        binding.videoView.setVideoURI(videouri)

//        val gifUrl = "https://www.easygifanimator.net/images/samples/eglite.gif"  // Replace with your GIF URL
        val gifUrl = "android.resource://" + context?.packageName + "/" + R.raw.hrlogo_animate

        Glide.with(this)
            .asGif()
            .load(gifUrl)
            .into(binding.imageView)
//        progressBar.setCancelable(false) //you can cancel it by pressing back button
//        progressBar.show()
    }


    fun forgetPasswrd() {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val token = preferences.getString("token", "0").toString()

        val call = apiInterface.sendOtp( binding.emailId.text.toString())
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        try {
                            // Parse the response body if it's not null
                            val responseBodyString = response.body()!!.string()
                            val responseObject = JSONObject(responseBodyString)
                            val apiMessage = responseObject.getString("message")
                            Toast.makeText(
                                context,
                                apiMessage,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            progressBar.dismiss()
                            replaceWithBundle()

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
                } else {
//                    // Handle the case where the response body is null
                    Toast.makeText(
                        context,
                        response.errorBody().toString(),
                        Toast.LENGTH_SHORT
                    ).show()
//                    handleErrorResponse(response.errorBody());
                    Log.e("forgetPass", "onResponse: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
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

                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })

    }

//    private fun handleErrorResponse(response: ResponseBody?) {
//        var errorMessage = "Failed to connect internet"
//        if (response() != null) {
//            try {
//                val jsonObject = org.json.JSONObject(response.errorBody()!!.string())
//                val message = jsonObject.optString("message")
//                if (!message.isEmpty()) {
//                    errorMessage = message
//                    Log.e("errorMessage", "handleErrorResponse: " + errorMessage)
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            } catch (e: JSONException) {
//                e.printStackTrace()
//            }
//        }
//    }
}