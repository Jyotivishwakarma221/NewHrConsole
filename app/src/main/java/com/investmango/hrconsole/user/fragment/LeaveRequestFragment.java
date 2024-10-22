package com.investmango.hrconsole.user.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.SaveUserLeave;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveRequestFragment extends Fragment {
    private long userId;
    private String token;
    private SharedPreferences preferences;
    private Spinner spinnerLeaveType;
    private ApiInterface apiInterface;
    private TextView selectedDateTextView1;
    private Button saveLeaveButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final List<String> selectedDates = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_leave_request, container, false);

        swipeRefreshLayout = rootView.findViewById(R.id.swipeLayout);
        EditText reasonEditText = rootView.findViewById(R.id.reasonEditText);
        selectedDateTextView1 = rootView.findViewById(R.id.selectedDateTextView1);
        saveLeaveButton = rootView.findViewById(R.id.saveLeave);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        spinnerLeaveType = rootView.findViewById(R.id.spinner_leave_type);

        setupLeaveTypeSpinner();
        setupSwipeRefreshLayout();
        setupCalendarImageView(rootView);
        setupSaveLeaveButton(reasonEditText);

        return rootView;
    }

    private void setupLeaveTypeSpinner() {
        List<String> list = new ArrayList<>();
        list.add("Select");
        list.add("Absent");
        list.add("Half Day");
        list.add("Short Leave");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), R.layout.color_spinner_layout, list);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        spinnerLeaveType.setAdapter(arrayAdapter);
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            selectedDates.clear();
            selectedDateTextView1.setVisibility(View.GONE);
            spinnerLeaveType.setSelection(0);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupCalendarImageView(View rootView) {
        ImageView calendarImg = rootView.findViewById(R.id.calenderImg);
        calendarImg.setOnClickListener(view -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Date");

        String[] options = {"Select Single Date", "Select Date Range"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showSingleDatePicker();
            } else if (which == 1) {
                showDateRangePicker();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSingleDatePicker() {
        // Initialize MaterialDatePicker for single date selection
        MaterialDatePicker<Long> builder = MaterialDatePicker.Builder.datePicker().build();
        builder.addOnPositiveButtonClickListener(selection -> {
            // Convert selected date to list of date
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);
            String selectedDateString = String.format("%d-%02d-%02d", selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH) + 1, selectedDate.get(Calendar.DAY_OF_MONTH));
            selectedDates.add(selectedDateString);
            updateSingleDateTextView();
        });

        // Show MaterialDatePicker for single date selection
        builder.show(getParentFragmentManager(), builder.toString());
    }

    private void showDateRangePicker() {
        // Initialize MaterialDatePicker for date range selection
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        MaterialDatePicker<Pair<Long, Long>> materialDatePicker = builder.build();

        // Add listener to get selected date range
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDates.clear();
            // Convert selected date range to list of dates
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(selection.first);
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(selection.second);
            while (!start.after(end)) {
                String selectedDate = String.format("%d-%02d-%02d", start.get(Calendar.YEAR), start.get(Calendar.MONTH) + 1, start.get(Calendar.DAY_OF_MONTH));
                selectedDates.add(selectedDate);
                start.add(Calendar.DATE, 1);
            }
            updateSelectedDateTextView();
        });

        // Show MaterialDatePicker for date range selection
        materialDatePicker.show(getParentFragmentManager(), materialDatePicker.toString());
    }

    private void updateSingleDateTextView() {
        if (!selectedDates.isEmpty()) {
            StringBuilder formattedDates = new StringBuilder();
            for (String date : selectedDates) {
                formattedDates.append(date).append(", ");
            }
            // Remove the trailing comma and space
            formattedDates.deleteCharAt(formattedDates.length() - 2);

            selectedDateTextView1.setText(formattedDates.toString());
            selectedDateTextView1.setVisibility(View.VISIBLE);
        }
    }

    private void updateSelectedDateTextView() {
        if (!selectedDates.isEmpty()) {
            // Display only the start date and end date
            String startDate = selectedDates.get(0);
            String endDate = selectedDates.get(selectedDates.size() - 1);
            String formattedDates = startDate + " To " + endDate;

            selectedDateTextView1.setText(formattedDates);
            selectedDateTextView1.setVisibility(View.VISIBLE);
        }
    }

    private void setupSaveLeaveButton(EditText reasonEditText) {
        saveLeaveButton.setOnClickListener(view -> {
            hideKeyboard();
            String reason = reasonEditText.getText().toString();
            String leaveType = spinnerLeaveType.getSelectedItem().toString();
            saveUserLeave(selectedDates, leaveType, reason);
        });
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void saveUserLeave(List<String> leaveDates, String leaveTypeStr, String reason) {
        // Check if leaveDates, leaveTypeStr, and reason are empty or null
        if (leaveDates.isEmpty() || leaveTypeStr.equals("Select") || reason.isEmpty()) {
            // Show toast message
            Toast.makeText(requireContext(), "Date, leave type, and reason cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert leaveType string to enum
        SaveUserLeave.LeaveType leaveType = SaveUserLeave.LeaveType.fromString(leaveTypeStr);

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("leaveDates", new JSONArray(leaveDates));
            jsonBody.put("leaveType", leaveType.name());
            jsonBody.put("reason", reason);

            RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
            ApiClient apiClient = new ApiClient(requireContext());
            apiInterface = apiClient.getApiInterface();

            Call<SaveUserLeave> call = apiInterface.saveUserLeave(requestBody, userId);
            call.enqueue(new Callback<SaveUserLeave>() {
                @Override
                public void onResponse(@NonNull Call<SaveUserLeave> call, @NonNull Response<SaveUserLeave> response) {
                    if (response.isSuccessful()) {
                        // Redirect to UserLeaveFragment
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.replaceLeave, new UserLeaveFragment())
                                .addToBackStack(null)
                                .commit();

                        Toast.makeText(requireContext(), "Leave request submitted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorMessage = getErrorMessage(response);
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("API Error", "Error: " + errorMessage);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SaveUserLeave> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Network Error", "Network error: " + t.getMessage());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private String getErrorMessage(Response<SaveUserLeave> response) {
        String errorMessage = "Unknown error";
        try {
            JSONObject errorJson = new JSONObject(response.errorBody().string());
            errorMessage = errorJson.optString("message", errorMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorMessage;
    }
}
