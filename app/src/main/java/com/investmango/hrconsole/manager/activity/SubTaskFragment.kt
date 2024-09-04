package com.investmango.hrconsole.manager.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentProjectsBinding
import com.investmango.hrconsole.databinding.FragmentSubTask2Binding
import com.investmango.hrconsole.databinding.FragmentSubTaskBinding
import com.investmango.hrconsole.databinding.OneSubtaskBinding
import com.investmango.hrconsole.model.SubTaskItem
import com.investmango.hrconsole.model.SubTaskResponse
import com.investmango.hrconsole.service.DateAndTimeUtility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SubTaskFragment : Fragment(), RecyclerViewInterface<OneSubtaskBinding> {

    private lateinit var binding: FragmentSubTask2Binding
    var userid: Long = 0
    var assignmnetId: Int = 0
    lateinit var apiInterface: ApiInterface
    private lateinit var subTask: List<SubTaskItem?>
    lateinit var progressDialog: AwesomeProgressDialog
    private var from: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)

        if (arguments != null) {
            from = arguments!!.getString("ViewOf")
            userid = arguments!!.getLong("id")
            assignmnetId = arguments!!.getInt("assignmentId")
        }
        Log.e("goTosubtaskkkk", "onCreate: 2 " + userid + " " + assignmnetId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_sub_task2, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("goTosubtaskkkk", "onViewCreated: 3")
        if (assignmnetId != 0)
            getSubtask()

        binding.addNew.setOnClickListener {
            var bundle = Bundle()
            bundle.putInt("id", assignmnetId)
            bundle.putString("ViewOf", from)
            var fragment = AddSubTaskFragment()
            fragment.arguments = bundle
            (context as ManagerActivity?)!!.replaceFragment(fragment)

        }

    }

    private fun getSubtask() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()

        val call = apiInterface.getSubtask(assignmnetId, userid)
        call.enqueue(object : Callback<SubTaskResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<SubTaskResponse>,
                response: Response<SubTaskResponse>
            ) {
                if (response.isSuccessful) {
                    progressDialog.dismissDialog()

                    subTask = response.body()?.content!!
                    if (subTask.isNotEmpty()) {
                        binding.noDataFound.visibility = View.GONE
                        binding.subtaskRecycler.visibility = View.VISIBLE
                        Log.e(
                            "AssignmentsResponse",
                            "onResponse: " + response.body()?.content?.size
                        )
                        binding.subtaskRecycler.adapter = CommonAdapter(this@SubTaskFragment)
                        if (isAdded)
                            binding.subtaskRecycler.layoutManager =
                                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    } else {
                        binding.noDataFound.visibility = View.VISIBLE
                        binding.subtaskRecycler.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SubTaskResponse>, t: Throwable) {
                progressDialog.dismissDialog()
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("goTosubtaskkkk", "onDestroyView: 4")

    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): OneSubtaskBinding {
        return OneSubtaskBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return subTask.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun bindView(viewBind: OneSubtaskBinding, position: Int) {
        viewBind.heading.text = subTask[position]?.name
        viewBind.details.text = subTask[position]?.description
        viewBind.nameOfMember.text = subTask[position]?.assignedByName
        viewBind.id.text = subTask[position]?.id.toString()

        if (subTask[position]?.updatedTime != 0L)
            viewBind.updateDate.text =
                DateAndTimeUtility.getDATEFromLong(subTask.get(position)?.updatedTime)
        else viewBind.updateDate.text =
            DateAndTimeUtility.getDATEFromLong(subTask.get(position)!!.createdTime)

        viewBind.layout.setOnClickListener {
            val bundle=Bundle()
            bundle.putInt("subtaskId",subTask.get(position)?.id!!)
            bundle.putInt("assignmnetId",assignmnetId)
            val fragment=StoryViewFragment()
            fragment.arguments=bundle
            (activity as ManagerActivity).replaceFragment(fragment)

        }
    }
}
