package com.investmango.hrconsole.manager.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.investmango.hrconsole.EmployeeAction.ViewPagerAdap;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.databinding.FragmentLeavesBinding;
import com.investmango.hrconsole.manager.activity.ApplyNewLeaveFragment;
import com.investmango.hrconsole.manager.activity.ManagerActivity;

public class Leaves extends Fragment {
    private FragmentLeavesBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_leaves, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAdapter();

        binding.NewLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ManagerActivity) getContext()).replaceFragment(new ApplyNewLeaveFragment());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setAdapter();
    }

    private void setAdapter() {
        ViewPagerAdap adapter = new ViewPagerAdap(getChildFragmentManager(), 0);

        // add fragment to the list
        adapter.addFragment("All", new LeaveList());
        adapter.addFragment("Half", new LeaveList());
        adapter.addFragment("Absent", new LeaveList());
        binding.viewPager.setAdapter(adapter);
        binding.tabs.setupWithViewPager(binding.viewPager);
    }
}