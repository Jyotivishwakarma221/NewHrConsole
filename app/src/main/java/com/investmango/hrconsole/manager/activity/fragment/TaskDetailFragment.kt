package com.investmango.hrconsole.manager.activity.fragment

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentTaskDetailBinding
import com.investmango.hrconsole.databinding.TaskDetailRecycBinding
import com.investmango.hrconsole.model.ContentItem
import com.investmango.hrconsole.model.Task
import com.investmango.hrconsole.model.TaskItems
import com.investmango.hrconsole.model.TaskResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale

class TaskDetailFragment : Fragment(), RecyclerViewInterface<TaskDetailRecycBinding> {
    lateinit var apiInterface: ApiInterface
    private lateinit var binding: FragmentTaskDetailBinding
    private var tasks: List<TaskItems?>? = null
    private var filterredList: List<TaskItems?>? = null
    private var userId: Long = 0
    var token: String = ""
    var count = 0
    var statusType = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0").toString()
        userId = preferences.getLong("userId", 0)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_task_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf { it.containsKey("Emp") }?.apply {
            Log.e("arguments", "onViewCreated: " + getString("Emp"))

            statusType = getString("Emp").toString()
        }
        listTask()

    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): TaskDetailRecycBinding {
        return TaskDetailRecycBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return tasks?.size!!
    }

    override fun bindView(viewBind: TaskDetailRecycBinding, position: Int) {
        if (tasks?.get(position)?.comments != null)
            viewBind.taskk.text =
                tasks?.get(position)?.subject + "\n \n" + tasks?.get(position)?.comments
        else viewBind.taskk.text = tasks?.get(position)?.subject
        viewBind.date.text = tasks?.get(position)?.createdTime?.let {
            convertTimestampToReadableFormat(
                it
            )
        }

        viewBind.id.text = tasks?.get(position)?.userId.toString()
        viewBind.name.text = tasks?.get(position)?.userName.toString()
//        viewBind.status.text=tasks?.get(position)?.status

    }

    private fun convertTimestampToReadableFormat(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd-MMM-yyyy HH:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun filter(status: String) {
        if (status == "All") {
            setAdapt()
        } else {
            filterredList = tasks?.filter { it?.status == status }!!
            Log.e("filteringHalfDay", "filter: " + filterredList?.size)
            setAdapt()
        }
    }

    private fun setAdapt() {
        try {
            if (tasks?.get(0)?.comments != null)
                binding.taskk.text =
                    tasks?.get(0)?.subject + "\n \n" + tasks?.get(0)?.comments
            else binding.taskk.text = tasks?.get(0)?.subject
            binding.date.text = tasks?.get(0)?.createdTime?.let {
                convertTimestampToReadableFormat(
                    it
                )
            }

            binding.id.text = tasks?.get(0)?.userId.toString()
            binding.name.text = tasks?.get(0)?.userName.toString()
//            binding.recyclerView.adapter = CommonAdapter(this)
//            binding.recyclerView.layoutManager =
//                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        } catch (e: Exception) {
            Log.e("TAG", "setAdapt: " + e)
        }

    }

    override fun onResume() {
        super.onResume()
//        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    fun listTask() {
        // Get task count of user
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface

        val taskCount = apiInterface.getAllTaskwithpage( userId, 0, "Done",1)
        taskCount.enqueue(object : Callback<TaskResponse> {
            override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                if (response.isSuccessful) {
                    tasks = response.body()?.content
                    val taskCount = tasks!!.size
                    Log.d("TaskCount", "Task Count: $taskCount")
                    if (statusType.equals("All")) {
                        filter("All")
                    } else if (statusType.equals("Pending")) {
                        filter("PENDING")
                    } else if (statusType.equals("Review")) {
                        filter("REVIEW")
                    } else {
                        Toast.makeText(context, "Something went wrong.", Toast.LENGTH_SHORT)
                            .show()

                    }

                } else {
                    makeText(
                        context,
                        "Failed to get task details.",
                        LENGTH_SHORT
                    ).show()
                    Log.e("listTask", "onResponse: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                Log.e("TaskFragment", "Network error: " + t.message)
                makeText(
                    context,
                    "Network error. Please try again.",
                    LENGTH_SHORT
                ).show()
            }
        })

    }
}