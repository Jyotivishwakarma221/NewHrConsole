package com.investmango.hrconsole.newHomePage

import android.R.attr.key
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.ActivityForgetPasswordBinding
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException


class ForgetPasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityForgetPasswordBinding
    lateinit var apiInterface: ApiInterface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password);

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ForgetPasswrdFragment())
            .commit()


    }


    fun replaceFrag(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(fragment.toString())
            .commit()
    }
}