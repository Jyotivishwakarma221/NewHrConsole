package com.investmango.hrconsole.EmployeeAction

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.HrConsole.tv.official.console.premium.CommonAdapter
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.ActiveMemberRecyclerBinding
import com.investmango.hrconsole.databinding.FragmentActiveMemberBinding
import com.investmango.hrconsole.databinding.FragmentMemberStatusBinding
import com.investmango.hrconsole.manager.activity.ManagerActivity
import com.investmango.hrconsole.model.Content
import com.investmango.hrconsole.model.TotalEmpResponseItem
import com.investmango.hrconsole.service.Constant
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.prefs.Preferences


class ActiveMember : Fragment(), RecyclerViewInterface<ActiveMemberRecyclerBinding> {
    private lateinit var binding: FragmentActiveMemberBinding
    lateinit var apiInterface: ApiInterface
    lateinit var preferences:SharedPreferences
    lateinit var authority:String
    var listoftotalEmp: ArrayList<TotalEmpResponseItem> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences  = context!!.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        authority= preferences.getString("Authority","user").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_active_member, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TotalEmpResponse()
    }

    private fun TotalEmpResponse() {
        val apiClient = ApiClient(requireContext())

        apiInterface = apiClient.apiInterface
        var userId = preferences.getLong("userId", 0)

        val token: String = preferences.getString("token", "0").toString()

        val call: Call<List<TotalEmpResponseItem>>? = apiInterface.getTotalEmp( userId, true)
        call?.enqueue(object : Callback<List<TotalEmpResponseItem>?> {
            override fun onResponse(
                call: Call<List<TotalEmpResponseItem>?>,
                response: Response<List<TotalEmpResponseItem>?>
            ) {
                if (response.body() != null && response.isSuccessful()) {
                    Log.e("getmeetings", "onResponse: ")
//                    listoftotalEmp = (response.body()!!.totalEmpResponse as ArrayList<TotalEmpResponseItem>?)!!
                    binding.recyclerView.adapter = CommonAdapter(this@ActiveMember)
                    listoftotalEmp = (response.body() as ArrayList<TotalEmpResponseItem>?)!!
                    binding.recyclerView.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                } else {
                    Log.e("getmeetings", "onResponse: " + response.body().toString())

                    Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<TotalEmpResponseItem>?>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                Log.e("khushi123", "onFailure: " + t.message)
            }
        })
    }

    override fun getViewBinding(viewGroup: ViewGroup, viewType: Int): ActiveMemberRecyclerBinding {
        return ActiveMemberRecyclerBinding.inflate(layoutInflater, viewGroup, false)
    }

    override fun getListCount(): Int {
        return listoftotalEmp.size
    }

    override fun bindView(viewBind: ActiveMemberRecyclerBinding, position: Int) {
        viewBind.name.setText(listoftotalEmp.get(position)?.userName)
        viewBind.position.setText(listoftotalEmp.get(position)?.designation)
        viewBind.id.setText(listoftotalEmp.get(position)?.id.toString())

        viewBind.layoutfull.setOnClickListener {
            ((activity as ManagerActivity).replaceFragment(ActiveMem_Details()))
        }
    }
}