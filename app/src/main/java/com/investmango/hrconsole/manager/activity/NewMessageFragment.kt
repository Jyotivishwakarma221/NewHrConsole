package com.investmango.hrconsole.manager.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.investmango.hrconsole.EmployeeAction.Member_list_Adapter
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentAssignTask2Binding
import com.investmango.hrconsole.databinding.FragmentNewMessageBinding
import com.investmango.hrconsole.databinding.MemberLayoutBinding
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.model.messageItem
import com.investmango.hrconsole.service.Constant
import com.investmango.hrconsole.service.DateAndTimeUtility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewMessageFragment : Fragment(), RecyclerViewInterface<MemberLayoutBinding> {
    private lateinit var binding: FragmentNewMessageBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0
    var token: String = ""
    var selectedList: ArrayList<TotalEmpResponseItem> = arrayListOf()
    var authority: String = ""
    private val uniqueItems = mutableSetOf<TotalEmpResponseItem>()
    var selectedId: Long = 0
    var finalList: ArrayList<TotalEmpResponseItem> = arrayListOf()
    private var allActiveUsers: List<TotalEmpResponseItem?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        token = preferences.getString("token", "0")!!
        userId = preferences.getLong("userId", 0)
        authority = preferences.getString("Authority", "0").toString()

        if (authority.equals(Constant.MANAGER))
            getChildActiveUser()
        else if (authority.equals(Constant.ADMIN))
            getAllActiveUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_message, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addMore.setOnClickListener {
            showMemberList()
        }

        binding.Send.setOnClickListener {
            val msg = messageItem()
            msg.id = userId
            val userIds: ArrayList<Long> = arrayListOf()
            for (i in 0..finalList.size - 1) {
                userIds.add(finalList.get(i).id)
            }
            msg.userIds = userIds
            msg.purpose= binding.Title.text.toString()
            msg.message= binding.message.text.toString()
            if ( binding.message.text.toString().trim().equals("") || binding.Title.text.toString().trim().equals("") || userIds.isEmpty()){
                Toast.makeText(context,"Fill details",Toast.LENGTH_SHORT).show()
            }else
            sendMessage(msg)
        }
//        binding.selectEmployee.setOnItemClickListener { parent, view, position, id ->
//            selectedId = allActiveUsers?.get(position)?.id?.toLong()!!
//        }
//        binding.backbutton.setOnClickListener { activity?.onBackPressed(); }

    }

    private fun sendMessage(message: messageItem) {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface

        val call = apiInterface.sendMessage( message)
        call.enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>,
            ) {
                if (response.isSuccessful) {
                    if (response.code() == 201) {
                        delete()
                        Toast.makeText(context, "Sent successfully", Toast.LENGTH_LONG).show()
                    }
                } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    private fun getAllActiveUser() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface

        val call = apiInterface.getAllEmployee(true)
        call.enqueue(object : Callback<List<TotalEmpResponseItem?>> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem?>>,
                response: Response<List<TotalEmpResponseItem?>>,
            ) {
                if (response.isSuccessful) {
                    allActiveUsers = response.body()
                } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem?>>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    private fun getChildActiveUser() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        var preferences = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val userId = preferences.getLong("userId", 0)

        val call = apiInterface.getTotalEmp(userId, true)
        call.enqueue(object : Callback<List<TotalEmpResponseItem?>> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem?>>,
                response: Response<List<TotalEmpResponseItem?>>,
            ) {
                if (response.isSuccessful) {
                    allActiveUsers = response.body()
                } else Toast.makeText(context, "Something went wrong.", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem?>>, t: Throwable) {
                Log.e("onFailure", "onFailure: " + t.message)
            }
        })
    }

    private fun showMemberList() {
        val builder = AlertDialog.Builder(context)
        val view = layoutInflater.inflate(R.layout.member_list_alert, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.membersList)
        val cross = view.findViewById<ImageView>(R.id.close)
        val done = view.findViewById<Button>(R.id.done)

        builder.setView(view)

        recyclerView.layoutManager = GridLayoutManager(context, 1)
        recyclerView.adapter = Member_list_Adapter(this, allActiveUsers!!)
        val dialog = builder.create()

        cross.setOnClickListener { dialog.dismiss() }

        done.setOnClickListener {
            Log.e("selectedList", "showMemberList: " + selectedList.size)
            addUniqueItems()
            setAdapt()
            binding.memberRecycler.adapter?.notifyDataSetChanged()
            dialog.dismiss()
        }
        dialog.show()
    }

    @SuppressLint("SuspiciousIndentation")
    fun setAdapt() {
        if (finalList != null && !finalList.isEmpty())
            binding.memberRecycler.layoutManager = GridLayoutManager(context, 2)
        binding.memberRecycler.adapter = CommonAdapter(this)
    }

    fun delete() {
        finalList.clear()
        binding.memberRecycler.adapter?.notifyDataSetChanged()
        binding.message.setText("")
        binding.Title.setText("")
    }

    private fun addUniqueItems() {
        for (item in selectedList) {
            if (uniqueItems.add(item)) { // Set.add() returns false if the item already exists
                finalList.add(item)
            }
        }
    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): MemberLayoutBinding {
        return MemberLayoutBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return finalList.size
    }

    override fun bindView(viewBind: MemberLayoutBinding, position: Int) {
        viewBind.nameOfMember.setText(finalList[position].userName)
    }

}