package com.investmango.hrconsole.admin.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.LeaveRequestUpdateStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApproveDialogFragment extends DialogFragment {
    private Context mContext;
    private String token;
    private SharedPreferences preferences;
    private ApiInterface apiInterface;
    private long leaveId;
    private long userId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public void setId(long leaveId) {
        this.leaveId = leaveId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.DialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_approve_dialog, null);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int) getResources().getDimension(R.dimen.dialog_width);
            layoutParams.height = (int) getResources().getDimension(R.dimen.dialog_height);
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.dimAmount = 0.8f;
            window.setAttributes(layoutParams);
        }

        Spinner spinner = view.findViewById(R.id.Spinner);

        List<String> list = new ArrayList<>();
        list.add("Select");
        list.add("Approved");
        list.add("Rejected");
        list.add("Pending");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext, R.layout.color_spinner_layout, list) {
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(ContextCompat.getColor(mContext, R.color.Card));
                return view;
            }
        };

        arrayAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        EditText commentEditText = view.findViewById(R.id.addCommentEditText);
        Button submitButton = view.findViewById(R.id.Approved_Button);

        submitButton.setOnClickListener(v -> {
            String selectedStatusString = spinner.getSelectedItem().toString();
            String commentText = commentEditText.getText().toString();

            // Validate input
            if (selectedStatusString.equals("Select") || commentText.isEmpty()) {
                Toast.makeText(mContext, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            LeaveRequestUpdateStatus.Status selectedStatus;
            switch (selectedStatusString) {
                case "Approved":
                    selectedStatus = LeaveRequestUpdateStatus.Status.APPROVED;
                    break;
                case "Rejected":
                    selectedStatus = LeaveRequestUpdateStatus.Status.REJECTED;
                    break;
                case "Pending":
                    selectedStatus = LeaveRequestUpdateStatus.Status.PENDING;
                    break;
                default:
                    selectedStatus = null;
            }

            if (selectedStatus != null) {
                approveLeaves(selectedStatus, commentText);
            }
        });

        return dialog;
    }

    private void approveLeaves(LeaveRequestUpdateStatus.Status selectedStatus, String commentText) {
        LeaveRequestUpdateStatus requestBody = new LeaveRequestUpdateStatus();
        requestBody.setId(leaveId);
        requestBody.setStatus(selectedStatus);
        requestBody.setComment(commentText);
        Call<Void> call = apiInterface.ApproveLeaves( requestBody, userId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d("API Response", "Response code: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(mContext, "Leave approval successful", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    try {
                        String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("API Error", "Error in API response: " + errorMessage);
                        Toast.makeText(mContext, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "Error processing request", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("API Failure", "API Request Failed: " + t.getMessage());
                Toast.makeText(mContext, "Network Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
