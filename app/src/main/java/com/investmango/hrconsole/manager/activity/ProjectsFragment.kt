package com.investmango.hrconsole.manager.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog
import com.bumptech.glide.Glide
import com.investmango.hrconsole.Adapter.StagesAdapter
import com.investmango.hrconsole.Adapter.fileAdapter
import com.investmango.hrconsole.Adapter.noteApater
import com.investmango.hrconsole.Adapter.showTaskAdap
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentProjectsBinding
import com.investmango.hrconsole.databinding.MemberLayoutBinding
import com.investmango.hrconsole.model.AssignmentItem
import com.investmango.hrconsole.model.Stage
import com.investmango.hrconsole.model.StagesResponse
import com.investmango.hrconsole.model.SubTaskItem
import com.investmango.hrconsole.model.SubTaskResponse
import com.investmango.hrconsole.service.DateAndTimeUtility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.util.Objects

class ProjectsFragment : Fragment(), RecyclerViewInterface<MemberLayoutBinding> {
    private lateinit var binding: FragmentProjectsBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    private lateinit var assignment: AssignmentItem
    lateinit var progressDialog: AwesomeProgressDialog
    private var from: String? = null
    lateinit var stages: List<Stage>
    var notes: ArrayList<String> = arrayListOf()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        userId = preferences.getLong("userId", 0)

        progressDialog = AwesomeProgressDialog(context)
        progressDialog.addTitle("Loading...") // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS)
        progressDialog.isCancelable(false)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_projects, container, false)
        // Inflate the layout for this fragment
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        getAssignment()
        if (arguments != null) {
            from = arguments!!.getString("ViewOf")
            progressDialog.showDialog()

            assignment = arguments!!.getSerializable("projectItem") as AssignmentItem
            getStages()
            setData()

        }
        binding.notes.setOnClickListener {
            showAlert()
        }
        binding.mySubTask.setOnClickListener {
            goToSubTask(userId)
        }
//        binding.addNew.setOnClickListener {
//            var bundle = Bundle()
//            bundle.putInt("id", assignment.id!!)
//            var fragment = AddSubTaskFragment()
//            fragment.arguments = bundle
//            (context as ManagerActivity?)!!.replaceFragment(fragment)
//
//        }

    }

    private fun getStages() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        progressDialog.showDialog()

        val call = apiInterface.getStageById(assignment.id!!)
        call.enqueue(object : Callback<StagesResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<StagesResponse>,
                response: Response<StagesResponse>,
            ) {
                if (response.isSuccessful) {
                    progressDialog.dismissDialog()

                    stages = response.body()?.content!!
                    if (stages.isNotEmpty() ) {
                        binding.stageRecycler.adapter = StagesAdapter(stages, context!!)
                    if (isAdded)
                        binding.stageRecycler.layoutManager =
                            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
                }
                } else {
                    Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<StagesResponse>, t: Throwable) {
                progressDialog.dismissDialog()
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    @SuppressLint("MissingInflatedId")
    fun showAlert() {
        // Create an alert builder
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)

        // set the custom layout
        val customLayout: View =
            layoutInflater.inflate(R.layout.add_story, null)
        builder.setView(customLayout)

        val reasonTxt =
            customLayout.findViewById<EditText>(R.id.storyDescription)
        val okBtn = customLayout.findViewById<TextView>(com.investmango.hrconsole.R.id.ok_btn)
        reasonTxt.movementMethod = ScrollingMovementMethod()

        val dialog = builder.create()
        okBtn.setOnClickListener {
            if (!reasonTxt.text.toString().isEmpty()) {
                progressDialog.showDialog()

                updateStory(reasonTxt.text.toString())
                dialog.dismiss()

            } else Toast.makeText(context, "Add something.", Toast.LENGTH_SHORT).show()

        }
        dialog.show()
    }

    private fun updateStory(storyText: String) {
        notes.clear()
        notes.add(storyText)
        progressDialog.showDialog()
        var request = NotesRequest(notes)
        request.notes = notes

        val call = apiInterface.updateNotes(request, assignment.id!!)
        call.enqueue(object : Callback<String?> {
            override fun onResponse(
                call: Call<String?>,
                response: Response<String?>,
            ) {
                progressDialog.dismissDialog()

                if (response.isSuccessful) {
                    progressDialog.dismissDialog()
                    notes.clear()
                    Toast.makeText(requireContext(), "Notes Updated.", Toast.LENGTH_LONG)
                        .show()
                    getAssignment()
                } else {
                    notes.clear()
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
                notes.clear()
                Toast.makeText(activity, "Server error " + t.message, Toast.LENGTH_SHORT).show()
                Log.e("Failure", Objects.requireNonNull(t.message!!))
            }
        })
    }

    data class NotesRequest(var notes: List<String>)


    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    fun setData() {
        progressDialog.dismissDialog()

        binding.projectName.text = assignment.subject
        binding.projectName.text = assignment.subject

        binding.deadlineDate.text = "   " + DateAndTimeUtility.getDATEFromLong(assignment.deadLine)
        binding.duedate.text = "  " + DateAndTimeUtility.getDATEFromLong(assignment.deadLine)

        if (assignment.stages != null)
            binding.stageName.text = assignment.stages.toString()

        if (assignment.description != null) {
            binding.description.visibility = View.VISIBLE
            binding.descriptionn.text = assignment.description
        } else {
            binding.description.visibility = View.GONE
            binding.descriptionn.visibility = View.GONE
        }

        if (assignment.priorityLevel != null) {

            binding.priority.visibility = View.VISIBLE
            binding.priorityy.visibility = View.VISIBLE

            binding.priority.text = " " + assignment.priorityLevel + " Priority"
            binding.priorityy.text = "  " + assignment.priorityLevel + " Priority"
        } else {
            binding.priority.visibility = View.GONE
            binding.priorityy.visibility = View.GONE
        }

        if (assignment.files != null) {
            binding.file.visibility = View.VISIBLE
        } else {
            binding.file.visibility = View.GONE
        }

        binding.file.setOnClickListener {
//            val urlIntent = Intent(
//                Intent.ACTION_VIEW,
//                Uri.parse(assignment.files.)
//            )
//            startActivity(urlIntent)
        }
        if (assignment.users?.isNotEmpty() == true) {
            if (isAdded) {

                // Load and display the image using Glide
                Glide.with(requireContext())
                    .load(assignment.users?.get(0)?.assignToProfile)
                    .into(binding.photo1)

                if (assignment.users?.size!! >= 2) {
                    // Load and display the image using Glide
                    Glide.with(requireContext())
                        .load(assignment.users?.get(1)?.assignToProfile)
                        .into(binding.photo2)
                }
                if (assignment.users?.size!! >= 3) {
                    // Load and display the image using Glide
                    Glide.with(requireContext())
                        .load(assignment.users?.get(2)?.assignToProfile)
                        .into(binding.photo3)
                }
            }
        }
        if (assignment.files != null || assignment.files?.size != 0) {
            binding.fileRecycler.adapter = fileAdapter(this, assignment.files!!)
            binding.fileRecycler.layoutManager =
                GridLayoutManager(context, 2)
        }
        if (assignment.notes != null && assignment.notes!!.size != 0) {
            binding.notesRecycler.adapter = noteApater(assignment.notes!!)
            binding.notesRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        }

        binding.subTaskMember.adapter = showTaskAdap(this, assignment.users!!)
        binding.subTaskMember.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.assignedRecyccler.adapter = CommonAdapter(this)
        binding.assignedRecyccler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)


        if (from.equals("Own")) {
            binding.teamSubTask.visibility = View.GONE
            binding.mySubTask.visibility = View.VISIBLE
            binding.assignedRecyccler.visibility = View.VISIBLE
            binding.Assign.visibility = View.VISIBLE
        } else {
            binding.teamSubTask.visibility = View.VISIBLE
            binding.mySubTask.visibility = View.GONE
            binding.assignedRecyccler.visibility = View.GONE
            binding.Assign.visibility = View.GONE
        }
    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): MemberLayoutBinding {
        return MemberLayoutBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return assignment.users?.size!!
    }

    override fun bindView(viewBind: MemberLayoutBinding, position: Int) {
        viewBind.nameOfMember.setText(assignment.users?.get(position)?.assignToName)
        Glide.with(requireContext()).load(assignment.users?.get(position)?.assignToProfile)
            .into(viewBind.profilephoto)
    }

    fun openfile(url: String) {
        val urlIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        startActivity(urlIntent)
    }

    fun goToSubTask(id: Long) {
        Log.e("goTosubtaskkkk", "goToSubTask: 1")
        val bundle = Bundle()
        bundle.putLong("id", id)
        bundle.putString("ViewOf", from)
        bundle.putInt("assignmentId", assignment.id!!)
        val fragment = SubTaskFragment()
        fragment.arguments = bundle
        (activity as ManagerActivity).replaceFragment(fragment)
    }

    private fun getAssignment() {
        progressDialog.showDialog()
        val apiClient = ApiClient(requireContext())
        apiInterface = apiClient.apiInterface
        val call: Call<AssignmentItem>? = apiInterface.getAssigmentById(assignment.id!!)
        call?.enqueue(object : Callback<AssignmentItem?> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<AssignmentItem?>,
                response: Response<AssignmentItem?>,
            ) {
                progressDialog.dismissDialog()

                if (response.body() != null && response.isSuccessful()) {
                    Log.e("getmeetings", "onResponse: " + response.body())
                    assignment = response.body()!!

                    setData()
                } else {
                    Log.e("getmeetings", "onResponse: " + response.body().toString())

                    Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AssignmentItem?>, t: Throwable) {
                progressDialog.dismissDialog()
                if (isAdded)
                    Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }


}