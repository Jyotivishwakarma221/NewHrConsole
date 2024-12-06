package com.investmango.hrconsole.user.activity;

import static com.investmango.hrconsole.service.ApplicationUtil.runConnectionCheckThread;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.DocsModel;
import com.investmango.hrconsole.model.EmpPerformance;
import com.investmango.hrconsole.model.MeetingDetails;
import com.investmango.hrconsole.model.Message;
import com.investmango.hrconsole.model.Salary;
import com.investmango.hrconsole.model.SaveUserLeave;
import com.investmango.hrconsole.model.Task;
import com.investmango.hrconsole.model.User;
import com.investmango.hrconsole.service.ApplicationUtil;
import com.investmango.hrconsole.service.CommonUtils;
import com.investmango.hrconsole.service.LoginActivity;
import com.investmango.hrconsole.service.MyBackgroundLocationService;
import com.investmango.hrconsole.service.Popup;
import com.investmango.hrconsole.service.SharedUtils;
import com.investmango.hrconsole.user.fragment.AddTasksFragment;
import com.investmango.hrconsole.user.fragment.DocFragment;
import com.investmango.hrconsole.user.fragment.EmployeePerformanceFragment;
import com.investmango.hrconsole.user.fragment.FeedbackFragment;
import com.investmango.hrconsole.user.fragment.LeaveRequestFragment;
import com.investmango.hrconsole.user.fragment.MeetingFragment;
import com.investmango.hrconsole.user.fragment.MessageFragment;
import com.investmango.hrconsole.user.fragment.MonthlyTaskFragment;
import com.investmango.hrconsole.user.fragment.ProfileFragment;
import com.investmango.hrconsole.user.fragment.SalaryDetailsFragment;
import com.investmango.hrconsole.user.fragment.TaskFragment;
import com.investmango.hrconsole.user.fragment.UpdateDocumentFragment;
import com.investmango.hrconsole.user.fragment.UpdateProfilePicFragment;
import com.investmango.hrconsole.user.fragment.UploadDocFragment;
import com.investmango.hrconsole.user.fragment.UserAttendanceFragment;
import com.investmango.hrconsole.user.fragment.UserAttendanceGraphFragment;
import com.investmango.hrconsole.user.fragment.UserLeaveFragment;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserHomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private CardView profile, attendance, meeting, task, addTask, salaryDetails, leaveRequest, userLeave;
    private ImageView profileImage, attendanceImage, leaveRequestImg, MeetingImg, addTasks, logoutImg, salaryDetailsImg, userLeaveImg, drawerIcon, taskImgView;
    private View userDashboard;
    private SharedUtils sharedUtils;

    public static ProgressDialog progressDialog;
    private TextView nameText, designationText, present_Counts, userTaskCounts, userLeaveCounts, meetingCounts, tvTittle, logoutTextView;
    private List<MeetingDetails> meetingDetails;
    private List<Task> tasks;
    private List<SaveUserLeave> saveUserLeaves;
    private RoundedImageView toolbarImage;
    private View green_Dots;
    private ApiClient apiClient;
    private ImageView posterImage, crossButtonImageView;
    private TextView eventDateTextView, subjectTextView, descriptionTextView, eventTimeTextView;
    private LinearLayout eventLayout;
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Check if the intent contains data to load a specific fragment
        Intent intent1 = getIntent();
        if (intent1 != null && intent1.hasExtra("fragmentToLoad")) {
            String fragmentToLoad = intent1.getStringExtra("fragmentToLoad");
            assert fragmentToLoad != null;
            if (fragmentToLoad.equals("TaskFragment")) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.Container, new TaskFragment())
                        .commit();
            }else if (fragmentToLoad.equals("UserLeaveFragment")) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.Container, new UserLeaveFragment())
                        .commit();
            }else if (fragmentToLoad.equals("UserAttendanceFragment")) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.Container, new UserAttendanceFragment())
                        .commit();
            }
            else if (fragmentToLoad.equals("MessageFragment")) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.Container, new MessageFragment())
                        .commit();
            }
            else if (fragmentToLoad.equals("MeetingFragment")) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.Container, new MeetingFragment())
                        .commit();
            }

        }
        apiClient = new ApiClient(this);

        // Screenshot restricted.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("TokenDetails", "Token Failed to received!");
                return;
            }
            String token = task.getResult();
            Log.d("FCM Token", token);
        });

        preferences = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        sharedUtils = new SharedUtils(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        green_Dots = findViewById(R.id.green_Dots);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setItemTextColor(AppCompatResources.getColorStateList(this, R.color.menu_item_text_color_selector));
        present_Counts = findViewById(R.id.present_Counts);
        userTaskCounts = findViewById(R.id.userTaskCounts);
        userLeaveCounts = findViewById(R.id.userLeaveCounts);
        meetingCounts = findViewById(R.id.meetingCounts);
        drawerIcon = findViewById(R.id.drawerIcon);
        profileImage = findViewById(R.id.profileImage);
        profile = findViewById(R.id.profile);
        attendanceImage = findViewById(R.id.attendanceImage);
        attendance = findViewById(R.id.attendance);
        MeetingImg = findViewById(R.id.MeetingImg);
        toolbarImage = findViewById(R.id.toolbarImage);
        meeting = findViewById(R.id.meeting);
        leaveRequestImg = findViewById(R.id.leaveRequestImg);
        leaveRequest = findViewById(R.id.leaveRequest);
        salaryDetailsImg = findViewById(R.id.salaryDetailsImg);
        salaryDetails = findViewById(R.id.salaryDetails);
        taskImgView = findViewById(R.id.taskImgView);
        task = findViewById(R.id.task);
        addTasks = findViewById(R.id.addTasks);
        addTask = findViewById(R.id.addTask);
        userLeaveImg = findViewById(R.id.userLeaveImg);
        userLeave = findViewById(R.id.userLeave);
        nameText = findViewById(R.id.nameText);
        designationText = findViewById(R.id.designationText);
        sharedUtils = new SharedUtils(this);
        tvTittle = findViewById(R.id.tvTittle);
        logoutTextView = findViewById(R.id.logoutTextView);

        // Event
        posterImage = findViewById(R.id.posterImage);
        crossButtonImageView = findViewById(R.id.cutImageView);
        eventDateTextView = findViewById(R.id.eventDate);
        eventTimeTextView = findViewById(R.id.eventTime);
        subjectTextView = findViewById(R.id.subject);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        eventLayout = findViewById(R.id.eventLay);

        Intent event = getIntent();
        String posterImageUrl = event.getStringExtra("POSTER");
        String formattedDate = event.getStringExtra("DATE");
        String formattedTime = event.getStringExtra("TIME");
        String description = event.getStringExtra("DESCRIPTION");
        String subject = event.getStringExtra("SUBJECT");

        if (formattedDate != null && !formattedDate.isEmpty() &&
                formattedTime != null && !formattedTime.isEmpty() &&
                description != null && !description.isEmpty() &&
                subject != null && !subject.isEmpty() &&
                posterImageUrl != null && !posterImageUrl.isEmpty()) {

            eventDateTextView.setText(formattedDate);
            eventTimeTextView.setText(formattedTime);
            subjectTextView.setText(subject);
            descriptionTextView.setText(description);
            Glide.with(getApplicationContext()).load(posterImageUrl).into(posterImage);
            eventLayout.setVisibility(View.VISIBLE);
        } else {
            eventLayout.setVisibility(View.GONE);
        }
        crossButtonImageView.setOnClickListener(v -> eventLayout.setVisibility(View.GONE));

        // Redirect to Home Page
        tvTittle.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomeActivity.this, UserHomeActivity.class);
            startActivity(intent);
        });

        toolbarImage.setOnClickListener(view -> {
            ProfileFragment profileFragment = new ProfileFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.Container, profileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        Intent intent = new Intent(this, MyBackgroundLocationService.class);
        runConnectionCheckThread(this);
        ContextCompat.startForegroundService(this, intent);

        // Get the saved token from SharedPreferences
        AtomicReference<SharedPreferences> preferences = new AtomicReference<>(getSharedPreferences("my_preferences", Context.MODE_PRIVATE));
        String token = preferences.get().getString("token", "0");
        long userId = preferences.get().getLong("userId", 0);

        // Call the API to get the current user data
        ApiClient apiClient = new ApiClient(getApplicationContext());
        ApiInterface apiInterface = apiClient.getApiInterface();
        Call<User> call = apiInterface.getCurrentUser();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {

                        String username = user.getUserName();
                        if (username != null && !username.isEmpty()) {
                            String capitalizedUsername = username.substring(0, 1).toUpperCase() + username.substring(1).toLowerCase();
                        } else {
                            Log.e("UserProfile", "Username is null or empty.");
                        }

                        String firstName = user.getFirstName();
                        String lastName = user.getLastName();
                        String capitalizedFirstName = Character.toUpperCase(firstName.charAt(0)) + firstName.substring(1);
                        String capitalizedLastName = Character.toUpperCase(lastName.charAt(0)) + lastName.substring(1);
                        String name = capitalizedFirstName + " " + capitalizedLastName;
                        String designation = user.getDesignation();
                        if (nameText != null) {
                            nameText.setText(name);
                        }
                        if (designationText != null) {
                            designationText.setText(designation);
                        }
                        View headerView = navigationView.inflateHeaderView(R.layout.header_lay);
                        // Find the TextView elements in the header layout
                        RoundedImageView imageProfile = headerView.findViewById(R.id.imageProfile);

                        TextView nameText = headerView.findViewById(R.id.nameText);
                        TextView designationText = headerView.findViewById(R.id.designationText);


                        // Show Profile Image
                        String imageUrl = user.getProfileImage();
                        Glide.with(getApplicationContext()).load(imageUrl).into(imageProfile);

                        RoundedImageView image = findViewById(R.id.toolbarImage);
                        String images = user.getProfileImage();
                        Glide.with(getApplicationContext()).load(images).into(image);

                        nameText.setText(name);
                        designationText.setText(designation);

                        long lastLoginTimestamp = user.getLastLogin();

                        Date lastLoginDate = new Date(lastLoginTimestamp);
                        // Create a SimpleDateFormat object to format the date
                        SimpleDateFormat sdf = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.US);
                        }
                        // Format the date as a string
                        String formattedLastLogin = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            formattedLastLogin = sdf.format(lastLoginDate);
                        }
                        // Last Login
                        TextView lastLoginTextView = headerView.findViewById(R.id.lastLogins);
                        lastLoginTextView.setText(formattedLastLogin);

                        boolean isActive = user.isActive();
                        View green_Dot = findViewById(R.id.green_Dot);

                        // Set the green dot visibility based on the user's activity status
//                        if (isActive) {
//                            green_Dot.setVisibility(View.VISIBLE);
//                        } else {
//                            green_Dot.setVisibility(View.GONE);
//                        }
                    }
                } else {
                    Log.e("UserProfile", "Failed to retrieve current user. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("UserProfile", "Error retrieving profile: " + t.getMessage());
            }
        });

        // Monthly present count of User
        {
            Call<Integer> adminAttendanceCountCall = apiInterface.getUserMonthlyAttendanceCount( userId);
            adminAttendanceCountCall.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                    if (response.isSuccessful()) {
                        Integer count = response.body();
                        if (count != null) {
                            TextView presentCountTextView = findViewById(R.id.present_Counts);
                            if (presentCountTextView != null) {
                                presentCountTextView.setText(String.valueOf(count));
                            }
                            Log.d("API Response", "Present Count: " + count);
                        }
                    } else {
                        Log.e("API Error", "Failed to fetch attendance count. Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                    Log.e("API Error", "Error fetching attendance count: " + t.getMessage());
                }
            });
        }
        // Get meeting count
        {
            Call<List<MeetingDetails>> meetingCount = apiInterface.getAllTodayMeeting( userId);
            meetingCount.enqueue(new Callback<List<MeetingDetails>>() {
                @Override
                public void onResponse(@NonNull Call<List<MeetingDetails>> call, @NonNull Response<List<MeetingDetails>> response) {
                    if (response.isSuccessful()) {
                        meetingDetails = response.body();
                        if (meetingDetails != null && !meetingDetails.isEmpty()) {
                            int meetingCount = meetingDetails.size();
                            String meetingCountString = Integer.toString(meetingCount);
                            Log.d("MeetingCount", "Meeting Count: " + meetingCountString);
                            TextView meetingCountTextView = findViewById(R.id.meetingCounts);
                            meetingCountTextView.setText(meetingCountString);
                        }
                    } else {
                        Toast.makeText(UserHomeActivity.this, "Failed to get meeting details.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<MeetingDetails>> call, @NonNull Throwable t) {
                    Toast.makeText(UserHomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Get task count of user
        {

            Call<List<Task>> taskCount = apiInterface.getAllTask(userId);
            taskCount.enqueue(new Callback<List<Task>>() {
                @Override
                public void onResponse(@NonNull Call<List<Task>> call, @NonNull Response<List<Task>> response) {
                    if (response.isSuccessful()) {
                        tasks = response.body();
                        if (tasks != null && !tasks.isEmpty()) {
                            int taskCount = tasks.size();
                            String taskCountString = Integer.toString(taskCount);
                            Log.d("TaskCount", "Task Count: " + taskCountString);
                            TextView taskCountTextView = findViewById(R.id.userTaskCounts);
                            taskCountTextView.setText(taskCountString);
                        }
                    } else {
                        Toast.makeText(UserHomeActivity.this, "Failed to get task details.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Task>> call, @NonNull Throwable t) {
                    Log.e("TaskFragment", "Network error: " + t.getMessage());
                    Toast.makeText(UserHomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Get the count of user leave
        {
            Call<List<SaveUserLeave>> userLeaveCount = apiInterface.getUserLeave( userId);

            userLeaveCount.enqueue(new Callback<List<SaveUserLeave>>() {
                @Override
                public void onResponse(@NonNull Call<List<SaveUserLeave>> call, @NonNull Response<List<SaveUserLeave>> response) {
                    Log.d("UserLeaveFragment", "Response: " + response.body());

                    if (response.isSuccessful()) {
                        List<SaveUserLeave> leaveList = response.body();
                        if (leaveList != null && !leaveList.isEmpty()) {
                            int userLeaveCount = leaveList.size();
                            // Convert the user leave count to a string
                            String userLeaveCountString = Integer.toString(userLeaveCount);
                            Log.d("UserLeaveCount", "User Leave Count: " + userLeaveCountString);
                            TextView leaveCountTextView = findViewById(R.id.userLeaveCounts);
                            leaveCountTextView.setText(userLeaveCountString);
                        }
                    } else {
                        Log.e("UserLeaveFragment", "Failed to fetch user leave");
                    }
                }
                @Override
                public void onFailure(@NonNull Call<List<SaveUserLeave>> call, @NonNull Throwable t) {
                    Log.e("UserLeaveFragment", "API call failed" + t);
                }
            });
        }

        logoutTextView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserHomeActivity.this);
            // Inflate the custom layout
            View customView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
            TextView dialogTitle = customView.findViewById(R.id.dialogTitle);
            TextView dialogMessage = customView.findViewById(R.id.dialogMessage);
            ImageView iconYes = customView.findViewById(R.id.iconYes);
            ImageView iconNo = customView.findViewById(R.id.iconNo);

            // Customize the title and message
            dialogTitle.setText("Sign Off");
            dialogMessage.setText("Are you sure you want to sign off ?");
            // Set icons for "Yes" and "No"
            iconYes.setImageResource(R.drawable.checkmark);
            iconNo.setImageResource(R.drawable.cross);

            // Set an OnClickListener for the checkmark icon
            final AlertDialog[] alertDialog = {null};

            iconYes.setOnClickListener(view1 -> {
                // Clear the token from local storage or SP.
                apiClient.logout();

                // Perform the logout action here
                preferences.set(sharedUtils.getSharedPreferencesContext());
                preferences.get().edit().clear().apply();
                stopService(intent);
                UserAttendanceFragment.stopLocationUpdate();
                finish();
                startActivity(new Intent(UserHomeActivity.this, LoginActivity.class));
                if (alertDialog[0] != null) {
                    alertDialog[0].dismiss();
                }
            });

            // Set an OnClickListener for the "No" icon to dismiss the dialog
            iconNo.setOnClickListener(view2 -> {
                if (alertDialog[0] != null) {
                    alertDialog[0].dismiss();
                }
            });

            builder.setView(customView)
                    .setNegativeButton("", (dialog, which) -> {
                        // Dismiss the dialog if "No" is clicked
                        if (alertDialog[0] != null) {
                            alertDialog[0].dismiss();
                        }
                    });

            // Create the AlertDialog here
            alertDialog[0] = builder.create();
            alertDialog[0].show();
        });

        drawerIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(item -> {
            Log.d("MenuItemClick", "Item selected: " + item.getTitle());
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                Intent intent2 = new Intent(UserHomeActivity.this, UserHomeActivity.class);
                startActivity(intent2);
            }
            else if (id == R.id.menu_attendance) {
                fetchMonthlyAttendanceAndOpenFragment();
            }
            else if (id == R.id.menu_performance) {
                fetchEmployeePerformanceAndOpenFragment();
            }
            else if (id == R.id.menu_getDoc) {
                fetchUserDocsAndOpenFragment();
            }
            else if (id == R.id.menu_upload_Document) {
                fetchUserDocs();
            }
            else if (id == R.id.menu_monthlyTask) {
                fetchMonthlyTaskAndOpenFragment();
            }

            else if (id == R.id.menu_message) {
                fetchMessageAndOpenFragment();
            }
            else if (id == R.id.menu_uploadDocument) {
                Fragment newFragment = new UpdateProfilePicFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
                Toast.makeText(UserHomeActivity.this, "Upload Document", Toast.LENGTH_SHORT).show();
            }


            // Change Password
            else if (id == R.id.menu_change_Password) {
                drawerLayout.closeDrawer(GravityCompat.START);
                Popup.changePassword(this);
            }
            else if (id == R.id.menu_feedback) {
                Fragment newFragment = new FeedbackFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
                Toast.makeText(UserHomeActivity.this, "Feedback", Toast.LENGTH_SHORT).show();
            }
            else if (id == R.id.menu_logout) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserHomeActivity.this);

                View customView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                TextView dialogTitle = customView.findViewById(R.id.dialogTitle);
                TextView dialogMessage = customView.findViewById(R.id.dialogMessage);
                ImageView iconYes = customView.findViewById(R.id.iconYes);
                ImageView iconNo = customView.findViewById(R.id.iconNo);

                dialogTitle.setText("Sign Off");
                dialogMessage.setText("Are you sure you want to sign off ?");
                // Set icons for "Yes" and "No"
                iconYes.setImageResource(R.drawable.checkmark);
                iconNo.setImageResource(R.drawable.cross);

                final AlertDialog[] alertDialog = {null};
                iconYes.setOnClickListener(view -> {
                    // Clear the token from local storage.
                    apiClient.logout();
                    // Perform the logout action here
                    preferences.set(sharedUtils.getSharedPreferencesContext());
                    preferences.get().edit().clear().apply();
                    stopService(intent);
                    UserAttendanceFragment.stopLocationUpdate();
                    finish();
                    startActivity(new Intent(UserHomeActivity.this, LoginActivity.class));
                    if (alertDialog[0] != null) {
                        alertDialog[0].dismiss();
                    }
                });

                // Set an OnClickListener for the "No" icon to dismiss the dialog
                iconNo.setOnClickListener(view -> {
                    if (alertDialog[0] != null) {
                        alertDialog[0].dismiss();
                    }
                });

                builder.setView(customView)
                        .setNegativeButton("", (dialog, which) -> {
                            // Dismiss the dialog if "No" is clicked
                            if (alertDialog[0] != null) {
                                alertDialog[0].dismiss();
                            }
                        });

                alertDialog[0] = builder.create();
                alertDialog[0].show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        profile.setOnClickListener(v -> {
            Fragment newFragment = new ProfileFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(UserHomeActivity.this, "Profile", Toast.LENGTH_SHORT).show();
        });

        profileImage.setOnClickListener(v -> {
            Fragment newFragment = new ProfileFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(UserHomeActivity.this, "Profile", Toast.LENGTH_SHORT).show();
        });

        attendance.setOnClickListener(v ->{
            Fragment newFragment = new UserAttendanceFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(UserHomeActivity.this, "Attendance", Toast.LENGTH_SHORT).show();
        });

        attendanceImage.setOnClickListener(v ->{
            Fragment newFragment = new UserAttendanceFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(UserHomeActivity.this, "Attendance", Toast.LENGTH_SHORT).show();
        });

        meeting.setOnClickListener(v -> fetchMeetingAndOpenFragment());

        MeetingImg.setOnClickListener(v -> fetchMeetingAndOpenFragment());

        salaryDetails.setOnClickListener(v -> fetchSalaryAndOpenFragment());

        salaryDetailsImg.setOnClickListener(v -> fetchSalaryAndOpenFragment());

        task.setOnClickListener(v -> fetchTasksAndOpenFragment());

        taskImgView.setOnClickListener(v -> fetchTasksAndOpenFragment());

        userLeave.setOnClickListener(v -> fetchUserLeaveAndOpenFragment());

        userLeaveImg.setOnClickListener(v -> fetchUserLeaveAndOpenFragment());

        leaveRequest.setOnClickListener(v -> {
            Fragment newFragment = new LeaveRequestFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(UserHomeActivity.this, "LeaveRequest", Toast.LENGTH_SHORT).show();
        });

        leaveRequestImg.setOnClickListener(v -> {
            Fragment newFragment = new LeaveRequestFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(UserHomeActivity.this, "LeaveRequest", Toast.LENGTH_SHORT).show();
        });

        addTask.setOnClickListener(v -> {
            Fragment newFragment = new AddTasksFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(UserHomeActivity.this, "Add Task", Toast.LENGTH_SHORT).show();
        });

        addTasks.setOnClickListener(v -> {
            Fragment newFragment = new AddTasksFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(UserHomeActivity.this, "Add Task", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchMonthlyTaskAndOpenFragment() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<ResponseBody> call = apiInterface.getMonthlyTaskStatistics( userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        if (responseData != null && !responseData.isEmpty()) {
                            openMontlyTaskGraphFragment();
                        } else {
                            Toast.makeText(UserHomeActivity.this, "Monthly task not found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(UserHomeActivity.this, "Error processing server response.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserHomeActivity.this, "Failed to get monthly task data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("Monthly Attendance", "Network error: " + t.getMessage());
                Toast.makeText(UserHomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTasksAndOpenFragment() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<Task>> call = apiInterface.getAllTask( userId);
        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(@NonNull Call<List<Task>> call, @NonNull Response<List<Task>> response) {
                if (response.isSuccessful()) {
                    List<Task> tasks = response.body();
                    if (tasks != null && !tasks.isEmpty()) {
                        openTaskFragment();
                    } else {
                        Toast.makeText(UserHomeActivity.this, "No tasks found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserHomeActivity.this, "Failed to get task details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Task>> call, @NonNull Throwable t) {
                Log.e("TaskFragment", "Network error: " + t.getMessage());
                Toast.makeText(UserHomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMessageAndOpenFragment() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<Message>> call = apiInterface.getCustomMessage(userId);
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(@NonNull Call<List<Message>> call, @NonNull Response<List<Message>> response) {
                if (response.isSuccessful()) {
                    List<Message> messages = response.body();
                    if (messages != null && !messages.isEmpty()) {
                        openMessageFragment();
                    } else {
                        Toast.makeText(UserHomeActivity.this, "No message found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserHomeActivity.this, "Failed to get message details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Message>> call, @NonNull Throwable t) {
                Log.e("TaskFragment", "Network error: " + t.getMessage());
                Toast.makeText(UserHomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMessageFragment() {
        Fragment newFragment = new MessageFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Message", Toast.LENGTH_SHORT).show();
    }

    private void fetchUserLeaveAndOpenFragment() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<SaveUserLeave>> call = apiInterface.getUserLeave( userId);
        call.enqueue(new Callback<List<SaveUserLeave>>() {
            @Override
            public void onResponse(@NonNull Call<List<SaveUserLeave>> call, @NonNull Response<List<SaveUserLeave>> response) {
                if (response.isSuccessful()) {
                    List<SaveUserLeave> userLeaves = response.body();
                    if (userLeaves != null && !userLeaves.isEmpty()) {
                        openUserLeaveFragment();
                    } else {
                        Toast.makeText(UserHomeActivity.this, "No leaves report found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserHomeActivity.this, "Failed to get user leave details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SaveUserLeave>> call, @NonNull Throwable t) {
                Log.e("UserLeaveFragment", "Network error: " + t.getMessage());
                Toast.makeText(UserHomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMeetingAndOpenFragment() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<MeetingDetails>> call = apiInterface.getAllTodayMeeting(userId);
        call.enqueue(new Callback<List<MeetingDetails>>() {
            @Override
            public void onResponse(@NonNull Call<List<MeetingDetails>> call, @NonNull Response<List<MeetingDetails>> response) {
                if (response.isSuccessful()) {
                    List<MeetingDetails> meetings = response.body();
                    if (meetings != null && !meetings.isEmpty()) {
                        openMeetingFragment();
                    } else {
                        Toast.makeText(UserHomeActivity.this, "No meetings found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserHomeActivity.this, "Failed to get today's meetings.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MeetingDetails>> call, @NonNull Throwable t) {
                Log.e("MeetingFragment", "Network error: " + t.getMessage());
                Toast.makeText(UserHomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSalaryAndOpenFragment() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<Salary>> call = apiInterface.userSalary(userId);
        call.enqueue(new Callback<List<Salary>>() {
            @Override
            public void onResponse(@NonNull Call<List<Salary>> call, @NonNull Response<List<Salary>> response) {
                if (response.isSuccessful()) {
                    List<Salary> salaries = response.body();
                    if (salaries != null && !salaries.isEmpty()) {
                        openSalaryFragment();
                    } else {
                        Toast.makeText(UserHomeActivity.this, "No payroll found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Salary>> call, @NonNull Throwable t) {
                Log.e("SalaryFragment", "Network error: " + t.getMessage());
                Toast.makeText(UserHomeActivity.this, "No payroll found .", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMonthlyAttendanceAndOpenFragment() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<ResponseBody> call = apiInterface.getMonthlyAttendance( userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        if (!responseData.isEmpty()) {
                            openUserAttendanceGraphFragment();
                        } else {
                            Toast.makeText(UserHomeActivity.this, "No monthly attendance data available.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(UserHomeActivity.this, "Error processing server response.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserHomeActivity.this, "No monthly attendance data available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("AttendanceFragment", "Network error: " + t.getMessage());
                Toast.makeText(UserHomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEmployeePerformanceAndOpenFragment() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<EmpPerformance>> call = apiInterface.getSingleEmployeeAllPerformanceByEmpId(userId);
        call.enqueue(new Callback<List<EmpPerformance>>() {
            @Override
            public void onResponse(@NonNull Call<List<EmpPerformance>> call, @NonNull Response<List<EmpPerformance>> response) {
                if (response.isSuccessful()) {
                    List<EmpPerformance> performances = response.body();
                    if (performances != null && !performances.isEmpty()) {
                        openEmployeePerformanceFragment();
                    } else {
                        Toast.makeText(UserHomeActivity.this, "Performance not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserHomeActivity.this, "Failed to get performance data for this employee.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EmpPerformance>> call, @NonNull Throwable t) {
                Log.e("PerformanceFragment", "Network error: " + t.getMessage());
                Toast.makeText(UserHomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDocs() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<ResponseBody> call = apiInterface.getDoc( userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBodyString = response.body().string();
                        if (responseBodyString.length() > 0) {
                            Log.d("Response Body", responseBodyString);
                            openUpdateDocumentFragment();
                        } else {
                            openUploadDocumentFragment();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        openUploadDocumentFragment();
                    }
                } else {
                    openUploadDocumentFragment();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("DocFragment", "Network error: " + t.getMessage());
                Toast.makeText(UserHomeActivity.this, "No document found for the selected user.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDocsAndOpenFragment() {
        ApiClient apiClient = new ApiClient(UserHomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<DocsModel> call = apiInterface.getDocs(userId);
        call.enqueue(new Callback<DocsModel>() {
                         @Override
                         public void onResponse(@NonNull Call<DocsModel> call, @NonNull Response<DocsModel> response) {
                             if (response.isSuccessful()) {
                                 if (response.body() != null) {
                                     DocsModel docsModel = response.body();
                                     List<String> documents = docsModel.getDocuments();
                                     if (documents != null && !documents.isEmpty()) {
                                         openDocFragment();
                                     } else {
                                         Toast.makeText(UserHomeActivity.this, "No documents available.", Toast.LENGTH_SHORT).show();
                                     }
                                 } else {
                                     Toast.makeText(UserHomeActivity.this, "Response body is null.", Toast.LENGTH_SHORT).show();
                                 }
                             } else {
                                 try {
                                     if (response.errorBody() != null) {
                                         JSONObject errorObject = new JSONObject(response.errorBody().string());
                                         String errorMessage = errorObject.optString("message", "Unknown error");
                                         Toast.makeText(UserHomeActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                     } else {
                                         Toast.makeText(UserHomeActivity.this, "Error response body is null.", Toast.LENGTH_SHORT).show();
                                     }
                                 } catch (IOException | JSONException e) {
                                     e.printStackTrace();
                                     Toast.makeText(UserHomeActivity.this, "Error reading response.", Toast.LENGTH_SHORT).show();
                                 }
                             }
                         }

                         @Override
                         public void onFailure(@NonNull Call<DocsModel> call, @NonNull Throwable t) {
                             Log.e("DocFragment", "Network error: " + t.getMessage());
                             Toast.makeText(UserHomeActivity.this, "No document found for the selected user.", Toast.LENGTH_SHORT).show();
                         }
                     }
        );
    }

    private void openUploadDocumentFragment() {
        Fragment newFragment = new UploadDocFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Upload Document", Toast.LENGTH_SHORT).show();

    }
    private void openUpdateDocumentFragment() {
        Fragment newFragment = new UpdateDocumentFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Update Document", Toast.LENGTH_SHORT).show();
    }
    private void openTaskFragment() {
        Fragment newFragment = new TaskFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Tasks", Toast.LENGTH_SHORT).show();
    }

    private void openUserLeaveFragment() {
        Fragment newFragment = new UserLeaveFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Leaves Report", Toast.LENGTH_SHORT).show();
    }

    private void openMeetingFragment() {
        Fragment newFragment = new MeetingFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Meetings", Toast.LENGTH_SHORT).show();
    }

    private void openSalaryFragment() {
        Fragment newFragment = new SalaryDetailsFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Payroll Panel", Toast.LENGTH_SHORT).show();
    }

    private void openUserAttendanceGraphFragment() {
        Fragment newFragment = new UserAttendanceGraphFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Attendance Graph", Toast.LENGTH_SHORT).show();
    }

    private void openEmployeePerformanceFragment() {
        Fragment newFragment = new EmployeePerformanceFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Employee Performance", Toast.LENGTH_SHORT).show();
    }

    private void openDocFragment() {
        Fragment newFragment = new DocFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(UserHomeActivity.this, "Document", Toast.LENGTH_SHORT).show();
    }

   private void openMontlyTaskGraphFragment() {
       Fragment newFragment = new MonthlyTaskFragment();
       FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
       fragmentTransaction.replace(R.id.Container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
       Toast.makeText(UserHomeActivity.this, "Monthly Task", Toast.LENGTH_SHORT).show();
   }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            showExitAlert();
        } else {
            super.onBackPressed();
        }
    }

    private void showExitAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Alert")
                .setMessage("Do you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    finish(); // Finish the activity and exit the application
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.exit_to_app)
                .show();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        ApplicationUtil.stopConnectionCheckThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationUtil.stopConnectionCheckThread();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ApplicationUtil.runConnectionCheckThread(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationUtil.runConnectionCheckThread(this);
        if (!isMyServiceRunning(MyBackgroundLocationService.class)) {
            ContextCompat.startForegroundService(this, new Intent(this, MyBackgroundLocationService.class));
        }
        CommonUtils.isGpsEnabled(this);
        // Dismiss the progress dialog if it's showing
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        ApplicationUtil.stopConnectionCheckThread();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



}
