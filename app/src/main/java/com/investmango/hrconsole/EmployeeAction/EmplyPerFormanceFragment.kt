package com.investmango.hrconsole.EmployeeAction

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract.Profile
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiClient
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentEmplyPerFormanceBinding
import com.investmango.hrconsole.databinding.FragmentTotalMemberBinding
import com.investmango.hrconsole.manager.activity.AchievementFragment
import com.investmango.hrconsole.manager.activity.ManagerFragment
import com.investmango.hrconsole.manager.activity.PerformanceFragment
import com.investmango.hrconsole.manager.activity.ProjectsFragment
import com.investmango.hrconsole.model.User
import com.investmango.hrconsole.profile.ProfileFragment
import com.investmango.hrconsole.service.DateAndTimeUtility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EmplyPerFormanceFragment : Fragment() {

    lateinit var apiInterface: ApiInterface
    private lateinit var binding: FragmentEmplyPerFormanceBinding
    var childUserid: Long = 0
    lateinit var user: User


    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey("childUserid") == true) {
            Log.e("Achievements", "onCreate: " + arguments?.getLong("childUserid")!!)
            childUserid = arguments?.getLong("childUserid")!!
            getChildUser()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_emply_per_formance,
                container,
                false
            )
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
    }

    fun getChildUser() {
        val apiClient = ApiClient(context)
        apiInterface = apiClient.apiInterface
        val call: Call<User> = apiInterface.getChildUser(childUserid)
        call.enqueue(object : Callback<User?> {
            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                if (response.isSuccessful) {
                    if (response.body()!=null) {
                        user = response.body()!!

                        setupData()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Something went wrong.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<User?>, t: Throwable) {
                Toast.makeText(context,"Something went wrong.",Toast.LENGTH_LONG).show()
            }
        });
    }

    private fun setupData() {
        binding.name.setText(user.userName)
        binding.designation.setText(user.designation)
        binding.phoneNum.setText(user.phone)
        binding.id.setText(user.id.toString())
        Glide.with(this).load(user.profileImage).into(binding.profilePhoto)

        binding.dateOfJoin.setText(DateAndTimeUtility.getDateeFromLong(user.createdDate))
    }

    private fun setAdapter() {
        val adapter = activity?.let { it1 -> ViewPager2Adap(it1) }

        val fragment = AchievementFragment()
        val fragment2 = ProfileFragment()
        val fragment3 = PerformanceFragment()

        val bundle = Bundle()
        bundle.putString("ViewOf", "child")
        if (!childUserid.equals(0))
        bundle.putLong("childUserid", childUserid)
        fragment.arguments = bundle
        fragment2.arguments = bundle
        fragment3.arguments = bundle

        // add fragment to the list
        adapter?.addWithIcon("Profile", R.drawable.emply_icon1, fragment2)
        adapter?.addWithIcon("Performance", R.drawable.emply_icon2, fragment3)
        adapter?.addWithIcon("Feedback", R.drawable.emply_icon3, fragment)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            val tabView = LayoutInflater.from(context).inflate(R.layout.custom_tab, null)
            tab.customView = tabView

            val icon = tabView.findViewById<ImageView>(R.id.tab_icon)
            val text = tabView.findViewById<TextView>(R.id.tab_text)

            adapter?.icons?.get(position)?.let { icon.setImageResource(it) }
            text.text = adapter?.name?.get(position)
        }.attach()

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val text = tab?.customView?.findViewById<TextView>(R.id.tab_text)
                text?.visibility = View.VISIBLE
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val text = tab?.customView?.findViewById<TextView>(R.id.tab_text)
                text?.visibility = View.GONE
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // No-op
            }
        })
    }
}

