package com.investmango.hrconsole.admin.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.adapter.AssignmentAdapter;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.Assignment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Assignment> assignment;
    private ApiInterface apiInterface;
    private String token;
    private SharedPreferences preferences;
    private Button previous_Assigment;
    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_assignment, container, false);
//        previous_Assigment = view.findViewById(R.id.previous_Assigment);
        // Set OnClickListener for addTaskButton
//        previous_Assigment.setOnClickListener(v -> {
            // Show the PreviousTaskFragment when the button is clicked
//            PreviousTaskFragment previousTaskFragment = new PreviousTaskFragment();
//
//            // Replace the current fragment with the PreviousTaskFragment
//            getParentFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, previousTaskFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
        recyclerView = view.findViewById(R.id.admin_assignmentRecyclerView);
        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog); // Initialize the progress dialog
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false); // Prevent user from dismissing the dialog

        // Initialize SharedPreferences and retrieve the token and userId
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");

       // Call the API to get task details
        getAssignment();
        return view;
    }
    private void getAssignment() {
        progressDialog.show(); // Show the progress dialog before making the API call

        // Create an instance of ApiClient and initialize apiInterface
        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();
        Call<List<Assignment>> call = apiInterface.getAssignment();
        call.enqueue(new Callback<List<Assignment>>() {
            @Override
            public void onResponse(@NonNull Call<List<Assignment>> call, @NonNull Response<List<Assignment>> response) {
                progressDialog.dismiss(); // Dismiss the progress dialog
                if (response.isSuccessful()) {
                    assignment = response.body();
                    if ( assignment != null && ! assignment.isEmpty()) {
                        setAssignmentAdapter(assignment);
                    } else {
                        // Handle case when no task details are available
                        Toast.makeText(getActivity(), "No tasks found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle API error here
                    Toast.makeText(getActivity(), "Failed to get task details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Assignment>> call, @NonNull Throwable t) {
                progressDialog.dismiss(); // Dismiss the progress dialog on network failure
                // Log the error for debugging purposes
                Log.e("TaskFragment", "Network error: " + t.getMessage());
                // Handle network failure here
                Toast.makeText(getActivity(), "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void setAssignmentAdapter(List<Assignment> assignment) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        AssignmentAdapter adapter = new AssignmentAdapter(assignment, getActivity());
        recyclerView.setAdapter(adapter);
    }
}