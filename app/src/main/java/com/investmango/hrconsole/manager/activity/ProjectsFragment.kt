package com.investmango.hrconsole.manager.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.investmango.hrconsole.R
import com.investmango.hrconsole.api.ApiInterface
import com.investmango.hrconsole.databinding.FragmentProjectsBinding
import com.investmango.hrconsole.manager.activity.fragment.NewTaskFragment

class ProjectsFragment : Fragment() {
    private lateinit var binding: FragmentProjectsBinding
    lateinit var apiInterface: ApiInterface
    private var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences =
            requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        userId = preferences.getLong("userId", 0)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addNew.setOnClickListener {
            (context as ManagerActivity?)!!.replaceFragment(NewTaskFragment())

        }

    }

}