package com.investmango.hrconsole.manager.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentStoryViewBinding
import com.investmango.hrconsole.databinding.ViewStoryBinding
import com.investmango.hrconsole.model.StoryResponse
import com.investmango.hrconsole.service.DateAndTimeUtility
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.util.Objects
import javax.security.auth.Subject

class StoryViewFragment : Fragment(), RecyclerViewInterface<ViewStoryBinding> {

    private lateinit var binding: FragmentStoryViewBinding
    lateinit var apiInterface: ApiInterface
    lateinit var progressDialog: AwesomeProgressDialog
    private var subTaskId: Int = 0
    private var assignmnetId: Int = 0
    private lateinit var storyResponse: StoryResponse
    var stories: ArrayList<String> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_story_view, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            progressDialog.showDialog()

            subTaskId = arguments!!.getInt("subtaskId")
            assignmnetId = arguments!!.getInt("assignmnetId")
            getStoryBySubTaskId()
            Log.e("getStoryBySubTaskId", "onViewCreated: " + subTaskId)
        }
        binding.addStory.setOnClickListener {
            showReasonAlert()
        }
        binding.details.setOnClickListener {
            showEditAlert()
        }
        binding.fileView.setOnClickListener {
            try {
                val urlIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(storyResponse.fileUrl)
                )
                activity!!.startActivity(urlIntent)
            } catch (e: Exception) {
                Log.e("TAG", "onViewCreated: " + e)
            }
        }
    }

    private fun getStoryBySubTaskId() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()
        val call = apiInterface.getSubTaskById(subTaskId)
        call.enqueue(object : Callback<StoryResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>,
            ) {
                progressDialog.dismissDialog()

                if (response.isSuccessful) {
                    storyResponse = response.body()!!
                    Log.e("StoryResponse", "onResponse: " + response.body())
                    setData()
                } else {
                    Log.e("allproject", "onResponse: " + response.message())
                    if (isAdded)
                        Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                progressDialog.dismissDialog()
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData() {

        binding.id.text = storyResponse.id.toString()
        binding.status.text = storyResponse.status
        binding.details.text = storyResponse.description
        binding.heading.text = storyResponse.name


        if (storyResponse.fileUrl != "") {
            binding.file.visibility = View.VISIBLE
            binding.fileView.visibility = View.VISIBLE
        } else {
            binding.file.visibility = View.GONE
            binding.fileView.visibility = View.GONE
        }

        if (storyResponse.priorityLevel != null) {
            binding.priorityy.visibility = View.VISIBLE
            binding.priorityy.text = " " + storyResponse.priorityLevel + " priority"
        } else binding.priorityy.visibility = View.INVISIBLE


        if (storyResponse.deadline != 0L)
            binding.DeadlineDate.text =
                " " + DateAndTimeUtility.getDATEFromLong(storyResponse.deadline) +" "+DateAndTimeUtility.getTimeInHourFromLong(storyResponse.deadline)

        if (storyResponse.createdTime != 0L)
            binding.createdDate.text =
                " " + DateAndTimeUtility.getDATEFromLong(storyResponse.createdTime)

        if (storyResponse.updatedTime != 0L)
            binding.updateDate.text =
                " " + DateAndTimeUtility.getDATEFromLong(storyResponse.updatedTime)

        binding.assignedBY.text = storyResponse.assignedByName

        binding.statusDetail.setOnClickListener {
            showStatusAlert()
        }
        if (storyResponse.story != null) {
            binding.storyRecycler.adapter = CommonAdapter(this)
            if (isAdded)
                binding.storyRecycler.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        }

    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): ViewStoryBinding {
        return ViewStoryBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return storyResponse.story?.size!!
    }

    override fun bindView(viewBind: ViewStoryBinding, position: Int) {
        val imgs = intArrayOf(R.drawable.hour_glass, R.drawable.tick)

        val drawableId = if (position == 0) imgs[0] else imgs[1]
        val drawable = ContextCompat.getDrawable(viewBind.root.context, drawableId)
        viewBind.image.setImageDrawable(drawable)

        viewBind.storyDescription.text = storyResponse.story?.get(position)

    }

    @SuppressLint("MissingInflatedId")
    fun showReasonAlert() {
        // Create an alert builder
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)

        // set the custom layout
        val customLayout: View =
            layoutInflater.inflate(com.investmango.hrconsole.R.layout.add_story, null)
        builder.setView(customLayout)

        val reasonTxt =
            customLayout.findViewById<EditText>(com.investmango.hrconsole.R.id.storyDescription)
        val okBtn = customLayout.findViewById<TextView>(com.investmango.hrconsole.R.id.ok_btn)
        reasonTxt.movementMethod = ScrollingMovementMethod()

        val dialog = builder.create()
        okBtn.setOnClickListener {
            if (!reasonTxt.text.toString().equals("")) {
                progressDialog.showDialog()
                updateStory(reasonTxt.text.toString())
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    @SuppressLint("MissingInflatedId")
    fun showStatusAlert() {
        // Create an alert builder
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)

        // set the custom layout
        val customLayout: View =
            layoutInflater.inflate(com.investmango.hrconsole.R.layout.change_status, null)
        builder.setView(customLayout)

        val statusSpin = customLayout.findViewById<Spinner>(R.id.status)
        val list2 = resources.getStringArray(R.array.StatusOfSubTask)

        val arrayAdapter =
            ArrayAdapter<Any?>(requireContext(), R.layout.color_spinner_layout, list2)
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout)
        assert(statusSpin != null)
        statusSpin!!.adapter = arrayAdapter

        val okBtn = customLayout.findViewById<TextView>(R.id.ok_btn)

        val dialog = builder.create()
        okBtn.setOnClickListener {
            progressDialog.showDialog()
            updateSubtask("", "", statusSpin.getSelectedItem().toString())
            dialog.dismiss()

        }
        dialog.show()
    }

    @SuppressLint("MissingInflatedId")
    fun showEditAlert() {
        // Create an alert builder
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)

        // set the custom layout
        val customLayout: View =
            layoutInflater.inflate(com.investmango.hrconsole.R.layout.edit_subtask, null)
        builder.setView(customLayout)

        val subject =
            customLayout.findViewById<EditText>(R.id.subject)
        val Description =
            customLayout.findViewById<EditText>(R.id.description)
        val okBtn = customLayout.findViewById<TextView>(com.investmango.hrconsole.R.id.ok_btn)
        subject.movementMethod = ScrollingMovementMethod()

        val dialog = builder.create()
        okBtn.setOnClickListener {
            if (Description.text.toString() != "" || subject.text.toString() != "") {
                progressDialog.showDialog()
                updateSubtask(Description.text.toString(), subject.text.toString(), "")
                dialog.dismiss()
            }

        }
        dialog.show()
    }

    private fun updateStory(storyText: String) {
        stories.clear()
        stories.add(storyText)

        var request = StoryRequest(stories)
        request.story = stories

        val call = apiInterface.updateStory(request, subTaskId, assignmnetId)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(
                call: Call<String?>,
                response: Response<String?>,
            ) {
                progressDialog.dismissDialog()

                if (response.isSuccessful) {
                    stories.clear()
                    Toast.makeText(requireContext(), "Story Updated.", Toast.LENGTH_LONG)
                        .show()
                    getStoryBySubTaskId()

                } else {
                    stories.clear()
                    Log.e("failure", "onResponse: " + response.message() + " " + request)
                    Toast.makeText(
                        activity,
                        "Server error " + response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                progressDialog.dismissDialog()
                stories.clear()
                Toast.makeText(activity, "Server error " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("Failure", Objects.requireNonNull(t.message!!))
            }
        })
    }

    private fun updateSubtask(description: String, subject: String, status: String) {
        val jsonObject = JSONObject()
        subject.let {
            if (it.isNotEmpty()) {
                jsonObject.put("subtaskName", it)
            }
        }
        status.let {
            if (it.isNotEmpty()) {
                jsonObject.put("subtaskStatus", it)
            }
        }

        description.let {
            if (it.isNotEmpty()) {
                jsonObject.put("subtaskDescription", it)
            }
        }
        val jsonString = jsonObject.toString()
        val requestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

        Log.e("subTask", "updateSubtask: " + requestBody)
        val call = apiInterface.update_Sub_Descrip(requestBody, subTaskId, assignmnetId)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(
                call: Call<String?>,
                response: Response<String?>,
            ) {
                progressDialog.dismissDialog()

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), " Updated.", Toast.LENGTH_LONG).show()
                    getStoryBySubTaskId()
                } else {
                    Log.e("failure", "onResponse: " + response.message())
                    Toast.makeText(
                        activity,
                        "Server error " + response.message(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                progressDialog.dismissDialog()
                Toast.makeText(activity, "Server error " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("Failure", Objects.requireNonNull(t.message!!))
            }
        })
    }

}

data class SubtaskRequestBody(
    val subtaskName: String? = null,
    val subtaskDescription: String? = null,
) : Serializable

fun createSubtaskRequestBody(
    subtaskName: String?,
    subtaskDescription: String?,
): Map<String, Any> {
    val requestBody = mutableMapOf<String, Any>()

    subtaskName?.let {
        if (it.isNotEmpty()) {
            requestBody["subtaskName"] = it
        }
    }

    subtaskDescription?.let {
        if (it.isNotEmpty()) {
            requestBody["subtaskDescription"] = it
        }
    }

    return requestBody
}

data class StoryRequest(var story: List<String>)
