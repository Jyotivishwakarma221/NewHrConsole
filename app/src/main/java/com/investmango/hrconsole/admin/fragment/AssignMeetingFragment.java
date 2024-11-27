package com.investmango.hrconsole.admin.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.AllActiveUsers;
import com.investmango.hrconsole.model.AssignMeeting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignMeetingFragment extends Fragment {
    private ApiInterface apiInterface;
    private String token;
    private long id;
    private SharedPreferences preferences;
    private TextView date;
    private List<AllActiveUsers> allActiveUsers;
    private EditText meetingEditText;
    private EditText purposeEditText;
    private EditText locationEditText;
    private TextView clickTimeTextView;
    private Spinner spinnerEmployee;
    private long selectedUserId;
    private Calendar selectedCalendar;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;
    private SwipeRefreshLayout swipeRefreshLayout;
    List<Long> selectedUserIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assign_meeting, container, false);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

        // Get from local storage
        token = preferences.getString("token", "0");
        id = preferences.getLong("userId", 0);

        selectedCalendar = Calendar.getInstance();
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        date = view.findViewById(R.id.date);
        meetingEditText = view.findViewById(R.id.meetingEditText);
        purposeEditText = view.findViewById(R.id.purpose);
        locationEditText = view.findViewById(R.id.location);
        clickTimeTextView = view.findViewById(R.id.click_Time);
        spinnerEmployee = view.findViewById(R.id.spinner_Employee);
        Button submitButton = view.findViewById(R.id.submitButton);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            clearData();
            swipeRefreshLayout.setRefreshing(false);
        });

        timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        getAllActiveUser();

        ImageView calendarImg = view.findViewById(R.id.calenderImgs);

        calendarImg.setOnClickListener(view1 -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setSelection(selectedCalendar.getTimeInMillis())
                    .build();

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                selectedCalendar.setTimeInMillis(selection);
                String formattedDate = dateFormat.format(selectedCalendar.getTime());
                date.setText(formattedDate);
            });

            materialDatePicker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        });



        clickTimeTextView.setOnClickListener(view12 -> showTimePickerDialog());

        submitButton.setOnClickListener(v -> {
            String description = meetingEditText.getText().toString().trim();
            String meetingPurpose = purposeEditText.getText().toString().trim();
            String meetingLocation = locationEditText.getText().toString().trim();
            String meetingTime = clickTimeTextView.getText().toString().trim();
            String selectedDate = date.getText().toString().trim();

            if (!description.isEmpty() && !meetingPurpose.isEmpty() && !meetingLocation.isEmpty() && !meetingTime.isEmpty()) {
                if (!selectedUserIds.isEmpty() && selectedCalendar != null) {

                    AssignMeeting assignMeeting = new AssignMeeting();
                    assignMeeting.setDate(selectedDate);

                    long timestamp = selectedCalendar.getTimeInMillis();
                    assignMeeting.setMeetingTime(timestamp);
                    assignMeeting.setDescription(description);
                    assignMeeting.setPurpose(meetingPurpose);
                    assignMeeting.setLocation(meetingLocation);

                    List<Long> userIds = new ArrayList<>(selectedUserIds);
                    assignMeeting.setUserIds(userIds);

                    assignMeeting(assignMeeting);

                    showToast("Meeting assigned successfully.");

                } else {
                    Toast.makeText(requireContext(), "Please select a date and user.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void clearData() {
        meetingEditText.setText("");
        purposeEditText.setText("");
        locationEditText.setText("");
        clickTimeTextView.setText("");
        date.setText("");
        selectedUserId = 0;
    }

    // all active user
    private void getAllActiveUser() {
        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();
        Call<List<AllActiveUsers>> call = apiInterface.getAllActiveUser();
        call.enqueue(new Callback<List<AllActiveUsers>>() {
            @Override
            public void onResponse(@NonNull Call<List<AllActiveUsers>> call, @NonNull Response<List<AllActiveUsers>> response) {
                if (response.isSuccessful()) {
                    allActiveUsers = response.body();
                    if (allActiveUsers != null && !allActiveUsers.isEmpty()) {
                        // Create an ArrayAdapter for the spinner using the custom layout
                        ArrayAdapter<AllActiveUsers> adapter = new ArrayAdapter<AllActiveUsers>(requireContext(), R.layout.item_spinner_checkbox, allActiveUsers) {
                            @NonNull
                            @Override
                            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                if (convertView == null) {
                                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spinner_checkbox, parent, false);
                                }

                                CheckBox checkBox = convertView.findViewById(R.id.checkbox_employee);
                                TextView textView = convertView.findViewById(R.id.text_employee_name);

                                // Set employee name
                                String fullName = allActiveUsers.get(position).getFirstName() + " " + allActiveUsers.get(position).getLastName();
                                textView.setText(capitalizeEachWord(fullName));

                                // Handle checkbox selection
                                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                    // Update selectedUserIds based on checkbox state
                                    if (isChecked) {
                                        selectedUserIds.add(allActiveUsers.get(position).getId());
                                    } else {
                                        selectedUserIds.remove(allActiveUsers.get(position).getId());
                                    }
                                });
                                return convertView;
                            }

                            @Override
                            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                return getView(position, convertView, parent);
                            }
                        };

                        spinnerEmployee.setAdapter(adapter);
                    } else {
                        Toast.makeText(getActivity(), "No users found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to get user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AllActiveUsers>> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String capitalizeEachWord(String str) {
        StringBuilder result = new StringBuilder();
        String[] words = str.split(" ");
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }

    private void assignMeeting(AssignMeeting assignMeeting) {
        Call<AssignMeeting> call = apiInterface.assignMeetingUser( id, assignMeeting);
        call.enqueue(new Callback<AssignMeeting>() {
            @Override
            public void onResponse(@NonNull Call<AssignMeeting> call, @NonNull Response<AssignMeeting> response) {
                if (response.isSuccessful()) {
                    // Redirect to MeetingFragment
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.move, new MeetingFragment())
                            .addToBackStack(null)
                            .commit();
                    showToast("Meeting assigned successfully.");
                } else {
                    if (response.code() == 500) {
                        showToast("A meeting already exists for this date");
                    } else {
                        showToast("Failed to assign meeting. Error code: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AssignMeeting> call, @NonNull Throwable t) {

                showToast("Network Error: " + t.getMessage());
            }
        });
    }

    private void showTimePickerDialog() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTitleText("Select Time")
                .setHour(hour)
                .setMinute(minute)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);

        final MaterialTimePicker materialTimePicker = builder.build();

        materialTimePicker.addOnPositiveButtonClickListener(view -> {
            if (selectedCalendar != null) {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, materialTimePicker.getHour());
                selectedCalendar.set(Calendar.MINUTE, materialTimePicker.getMinute());
                String formattedTime = timeFormat.format(selectedCalendar.getTime());
                clickTimeTextView.setText(formattedTime);
                Log.d("SelectedTime", formattedTime);
            }
        });

        materialTimePicker.show(requireActivity().getSupportFragmentManager(), "MATERIAL_TIME_PICKER");
    }


    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
