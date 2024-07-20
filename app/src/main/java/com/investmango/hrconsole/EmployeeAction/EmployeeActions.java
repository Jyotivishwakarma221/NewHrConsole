package com.investmango.hrconsole.EmployeeAction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.investmango.hrconsole.databinding.FragmentEmployeeActionsBinding;
import com.investmango.hrconsole.manager.activity.ManagerActivity;
import com.investmango.hrconsole.manager.activity.fragment.TasksFragment;

public class EmployeeActions extends Fragment {

    private FragmentEmployeeActionsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEmployeeActionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.MemberStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ((ManagerActivity) getActivity()).replaceFragment(new EmplyPerFormanceFragment());
                ((ManagerActivity) getActivity()).replaceFragment(new MemberStatus());
            }
        });
//        binding.ActiveMember.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((ManagerActivity) getActivity()).replaceFragment(new ActiveMember());
//            }
//        });
        binding.TeamTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TasksFragment fragment = new TasksFragment();
                Bundle bb = new Bundle();
                bb.putString("ViewOf", "Childs");
                fragment.setArguments(bb);
                ((ManagerActivity) getActivity()).replaceFragment(fragment);
//                ((ManagerActivity) getActivity()).replaceFragment(new AssignTask());

            }
        });
        binding.AddMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ManagerActivity) getActivity()).replaceFragment(new AddMeeting());

            }
        });
        binding.assignments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Comming soon..", Toast.LENGTH_SHORT).show();
//                ((ManagerActivity) getActivity()).replaceFragment(new ManageAssignFragment());

            }
        });
        binding.StaffLeaves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ManagerActivity) getActivity()).replaceFragment(new StaffLeaveFragment());

            }
        });
    }

}