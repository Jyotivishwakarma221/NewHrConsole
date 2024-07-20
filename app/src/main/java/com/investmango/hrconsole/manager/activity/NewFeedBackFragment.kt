package com.investmango.hrconsole.manager.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FeedBackRecyclerBinding
import com.investmango.hrconsole.databinding.FragmentNewFeedBackBinding
import com.investmango.hrconsole.model.FeedbackRequest
import com.investmango.hrconsole.model.FeedbackResponseItem
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewFeedBackFragment : Fragment(), RecyclerViewInterface<FeedBackRecyclerBinding> {

    private lateinit var binding: FragmentNewFeedBackBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    private var authority: String = ""
    var feedItem: List<FeedbackResponseItem>? = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        userId = preferences.getLong("userId", 0)
        authority = preferences.getString("Authority", "").toString()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_new_feed_back, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinner()

        if (authority == Constant.USER) {
            getFeedBacks()
        }

        binding.submit.setOnClickListener { saveFeedback("") }


    }

    fun setAdapter() {
        binding.recyclerFeedback.adapter = CommonAdapter(this@NewFeedBackFragment)

        binding.recyclerFeedback.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun setupSpinner() {
        val sections: MutableList<String> = ArrayList()
        sections.add("Select")
        sections.add("Task")
        sections.add("Attendance")
        sections.add("Salary")
        sections.add("Upload Document")
        sections.add("Other")

        val adapter = ArrayAdapter(requireContext(), R.layout.color_spinner_layout, sections)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)

        binding.feedbackType.setAdapter(adapter)
    }

    private fun saveFeedback(imageUrl: String) {
        val apiClient=ApiClient(requireContext())
        apiInterface=apiClient.apiInterface

        val feedbackText = binding.feedback.getText().toString()
        val selectedSection: String = binding.feedbackType.getSelectedItem().toString()

        if (!feedbackText.trim().isEmpty() && selectedSection != "Select") {
            val feedbackRequest = FeedbackRequest()
            feedbackRequest.feedback = feedbackText
            feedbackRequest.section = selectedSection
            feedbackRequest.url = imageUrl
            val call = apiInterface.saveNewFeedbacks(userId, feedbackRequest)
            call.enqueue(object : Callback<FeedbackRequest?> {
                override fun onResponse(
                    call: Call<FeedbackRequest?>,
                    response: Response<FeedbackRequest?>,
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Feedback saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.feedbackType.setSelection(0)
                        binding.feedback.setText("")
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to save feedback",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<FeedbackRequest?>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed to save feedback", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        } else {
            Toast.makeText(
                requireContext(),
                "Please provide feedback and select a section",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getFeedBacks() {
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<List<FeedbackResponseItem>>? = apiInterface.getfeedBack(userId)
        call?.enqueue(object : Callback<List<FeedbackResponseItem>?> {
            override fun onResponse(
                call: Call<List<FeedbackResponseItem>?>,
                response: Response<List<FeedbackResponseItem>?>,
            ) = if (response.body() != null && response.isSuccessful()) {

                response.body().also { feedItem = it }
                Log.e("getfeedback", "onResponse: " + response.body())
                setAdapter()
            } else {
                Log.e("getfeedback", "onResponse: " + response.errorBody())

                Toast.makeText(context, getErrorMessage(response), Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<List<FeedbackResponseItem>?>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    private fun getErrorMessage(response: Response<List<FeedbackResponseItem>?>): String {
        var errorMessage = "Unknown error"
        try {
            val errorJson = JSONObject(response.errorBody()!!.string())
            errorMessage = errorJson.optString("message", errorMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return errorMessage
    }


    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): FeedBackRecyclerBinding {
        return FeedBackRecyclerBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return feedItem?.size!!
    }

    override fun bindView(viewBind: FeedBackRecyclerBinding, position: Int) {
        viewBind.feedbackFrom.text = feedItem?.get(position)?.givenByName
        viewBind.description.text = feedItem?.get(position)?.feedback
        viewBind.dateTime.text =
            DateAndTimeUtility.getDateAndTimeFromLong(feedItem?.get(position)?.createdTime)
    }

}