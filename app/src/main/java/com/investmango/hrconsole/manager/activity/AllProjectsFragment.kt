package com.investmango.hrconsole.manager.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentAllProjectsBinding
import com.investmango.hrconsole.databinding.ProjectItemBinding
import com.investmango.hrconsole.model.AssignmentItem
import com.investmango.hrconsole.model.AssignmentsResponse
import com.investmango.hrconsole.service.CommonUtils
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AllProjectsFragment : Fragment(), RecyclerViewInterface<ProjectItemBinding> {


    private lateinit var binding: FragmentAllProjectsBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    private  var assignment: List<AssignmentItem?> = ArrayList()
    private var from: String? = null
    lateinit var progressDialog: AwesomeProgressDialog
    var authority: String = ""
    val progress = mutableListOf<Pair<Int, Int>>()
    var backpressed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)

        authority = preferences.getString("Authority", "0").toString()

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)
        progressDialog.showDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_all_projects, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getbundle()

    }

    fun getbundle() {
        assert(arguments != null)
        from = arguments!!.getString("ViewOf")
        getAssignment()
        Log.e("AssignmentsResponse", "getbundle: " + from)
        if (from != "Own") {
            if (authority.equals(Constant.ADMIN))
                getAllAssignment()
        }
    }

    private fun getAssignment() {
        val apiClient = ApiClient(context)
        val apiInterface = apiClient.apiInterface
        val preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val userId = preferences.getLong("userId", 0)

        val call = apiInterface.getuserAssigments(userId)
        call.enqueue(object : Callback<AssignmentsResponse> {
            override fun onResponse(
                call: Call<AssignmentsResponse>,
                response: Response<AssignmentsResponse>,
            ) {
                if (response.isSuccessful) {
                    progressDialog.dismissDialog()
                    assignment = response.body()!!.content!!

                    if (assignment.size != 0) {

                        binding.noDataFound.visibility = View.GONE
                        binding.recyclerProject.visibility = View.VISIBLE

                        for (i in assignment.indices) {
                            val assignmentId = assignment[i]!!.id!!
                            if (isAdded)
                                CommonUtils.getProgress(
                                    assignmentId,
                                    context,
                                    object : CommonUtils.ProgressCallback {
                                        override fun onProgressCalculated(progressValue: Int) {
                                            progress.add(Pair(progressValue, assignmentId))
                                            Log.e("getProgress", "Updated Progress List: $progress")

                                            // Only call setData when all progress values are collected
                                            if (progress.size == assignment.size) {
                                                progressDialog.showDialog()
                                                Handler(Looper.getMainLooper()).postDelayed({
                                                    setData()
                                                }, 200)
                                            }
                                        }
                                    })
                        }
                    } else {
                        binding.noDataFound.visibility = View.VISIBLE
                        binding.recyclerProject.visibility = View.GONE
                    }
                    Log.e("AssignmentsResponse", "onResponse: " + assignment.size)
                } else {
                    progressDialog.dismissDialog()
                    Log.e("AssignmentsResponse", "Unsuccessful: " + response.errorBody())
                    if (isAdded) {
                        Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<AssignmentsResponse>, t: Throwable) {
                progressDialog.dismissDialog()
                Log.e("AssignmentsResponse", "onFailure: " + t.message)
            }
        })
    }

    private fun getAllAssignment() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface

        val call = apiInterface.getAllAssigments()
        call.enqueue(object : Callback<AssignmentsResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<AssignmentsResponse>,
                response: Response<AssignmentsResponse>,
            ) {
                progressDialog.dismissDialog()
                if (response.isSuccessful) {

                    assignment = response.body()?.content!!
                    if (assignment.size != 0) {

                        binding.noDataFound.visibility = View.GONE
                        binding.recyclerProject.visibility = View.VISIBLE

                        for (i in assignment.indices) {
                            val assignmentId = assignment[i]!!.id!!
                            CommonUtils.getProgress(
                                assignmentId,
                                context,
                                object : CommonUtils.ProgressCallback {
                                    override fun onProgressCalculated(progressValue: Int) {
                                        progress.add(Pair(progressValue, assignmentId))
                                        Log.e("getProgress", "Updated Progress List: $progress")

                                        // Only call setData when all progress values are collected
                                        if (progress.size == assignment.size) {
                                            progressDialog.showDialog()
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                setData()
                                            }, 200)
                                        }
                                    }
                                })
                        }
                    } else {
                        binding.noDataFound.visibility = View.VISIBLE
                        binding.recyclerProject.visibility = View.GONE
                    }
                    Log.e("AssignmentsResponse", "onResponse: " + assignment.size)

                } else {
                    Log.e("allproject", "onResponse: " + response.message())
                    if (isAdded)
                        Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AssignmentsResponse>, t: Throwable) {
                progressDialog.dismissDialog()
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    private fun setData() {
        backpressed = true
        Log.e("allproject", "setData: ")
        progressDialog.showDialog()
        binding.recyclerProject.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerProject.adapter = CommonAdapter(this)
        progressDialog.dismissDialog()

    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): ProjectItemBinding {
        return ProjectItemBinding.inflate(layoutInflater, viewGroup, false)

    }

    override fun getListCount(): Int {
        return assignment.size
    }


    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun bindView(viewBind: ProjectItemBinding, position: Int) {
        viewBind.projectName.text = assignment[position]?.headings?.toUpperCase()
        viewBind.deadlineDate.text =
            DateAndTimeUtility.getDATEFromLong(assignment[position]?.deadLine)
        viewBind.createdOn.text =   DateAndTimeUtility.getDATEFromLong(assignment[position]?.createdDate)
        if (assignment[position]?.priorityLevel != null) {
            viewBind.priority.visibility = View.VISIBLE
            viewBind.priority.text = "  " + assignment.get(position)!!.priorityLevel + " PRIORITY"
        } else {
            viewBind.priority.visibility = View.GONE
        }


        if (assignment[position]?.banner!="" || assignment[position]?.banner!=null){
            Glide.with(this)
                .load(assignment[position]?.banner)
                .into(object : CustomTarget<Drawable?>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in Drawable?>?,
                    ) {
                        viewBind.cardView.setBackground(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Optionally handle cleanup
                    }
                })        }

        for (i in 0 until progress.size) {
            Log.e(
                "getProgress",
                "bindView: " + progress.get(i).second + " " + assignment.get(position)?.id
            )
            if (progress.get(i).second == assignment.get(position)?.id) {
                viewBind.progressbar.progress = progress.get(i).first
            }

        }

        if (assignment[position]?.users?.isNotEmpty() == true) {
            if (isAdded) {

                // Load and display the image using Glide

                if (assignment.get(position)?.users?.get(0) != null)
                    Glide.with(requireContext())
                        .load(assignment.get(position)?.users?.get(0)?.assignToProfile)
                        .into(viewBind.photo1)

                if (assignment.get(position)?.users?.size!! >= 2) {
                    // Load and display the image using Glide
                    Glide.with(requireContext())
                        .load(assignment.get(position)?.users?.get(1)!!.assignToProfile)
                        .into(viewBind.photo2)
                    Log.e("assignmentget", "bindView: "+assignment.get(position)?.users?.get(1)!!.assignToProfile )
                }
                if (assignment.get(position)?.users?.size!! >= 3) {
                    Glide.with(requireContext())
                        .load(assignment.get(position)?.users?.get(2)!!.assignToProfile)
                        .into(viewBind.photo3)
                }
            }
        }
        viewBind.cardView.setOnClickListener {
            val fragment = ProjectsFragment()
            val bundle = Bundle()
            bundle.putSerializable("projectItem", assignment.get(position))
            bundle.putString("ViewOf", from)
            fragment.arguments = bundle
            (activity as ManagerActivity).replaceFragment(fragment)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e("allproject", "onPause: ")
        setData()
    }

    override fun onResume() {
        super.onResume()
        if (backpressed)
            setData()
        Log.e("allproject", "onResume: ")
    }
}