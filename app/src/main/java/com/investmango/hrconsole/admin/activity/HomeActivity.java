package com.investmango.hrconsole.admin.activity;

import static com.investmango.hrconsole.service.ApplicationUtil.runConnectionCheckThread;
import static com.investmango.hrconsole.user.activity.UserHomeActivity.progressDialog;

import android.app.ActivityManager;
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
import androidx.annotation.Nullable;
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
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.fragment.AddEventFragment;
import com.investmango.hrconsole.admin.fragment.AdminAttendanceFragment;
import com.investmango.hrconsole.admin.fragment.AdminProfileFragment;
import com.investmango.hrconsole.admin.fragment.AdminTaskFragment;
import com.investmango.hrconsole.admin.fragment.AllActiveUserFragment;
import com.investmango.hrconsole.admin.fragment.ApprovedFragment;
import com.investmango.hrconsole.admin.fragment.AssignMeetingFragment;
import com.investmango.hrconsole.admin.fragment.AssignTaskFragment;
import com.investmango.hrconsole.admin.fragment.AttendanceGraphFragment;
import com.investmango.hrconsole.admin.fragment.EmployeeDocFragment;
import com.investmango.hrconsole.admin.fragment.LeaveRequestAdminFragment;
import com.investmango.hrconsole.admin.fragment.MeetingFragment;
import com.investmango.hrconsole.admin.fragment.PerformanceFragment;
import com.investmango.hrconsole.admin.fragment.SalaryDetailsAdminFragment;
import com.investmango.hrconsole.admin.fragment.TodayPresentEmployeeFragment;
import com.investmango.hrconsole.admin.fragment.UpdateProfileFragment;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.AdminSalaryDetails;
import com.investmango.hrconsole.model.AdminTask;
import com.investmango.hrconsole.model.ApprovedLeaves;
import com.investmango.hrconsole.model.EmpPerformance;
import com.investmango.hrconsole.model.LeaveRequest;
import com.investmango.hrconsole.model.MeetingDetailsAdmin;
import com.investmango.hrconsole.model.PresentEmployee;
import com.investmango.hrconsole.model.User;
import com.investmango.hrconsole.service.CommonUtils;
import com.investmango.hrconsole.service.LoginActivity;
import com.investmango.hrconsole.service.MyBackgroundLocationService;
import com.investmango.hrconsole.service.Popup;
import com.investmango.hrconsole.service.SharedUtils;
import com.investmango.hrconsole.user.fragment.ProfileFragment;
import com.investmango.hrconsole.user.fragment.UserAttendanceFragment;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    NavigationView navigationView;
    ImageView MeetingImg;
    CardView task_View;
    CardView adminAttendance;
    ImageView task_Image_View;
    ImageView salary_detail_image;
    CardView admin_salary_details;
    CardView meeting_card_view;
    ImageView adminAttendanceImage;
    ImageView active_emp_image;
    CardView active_emp_card;
    ImageView adminLogoutImg;
    CardView admin_Logout;
    ImageView drawerIcon;
    ImageView leave_request_image;
    ImageView adminProfileImg;
    CardView admin_Profile;
    CardView leave_request_Card;
    SharedUtils sharedUtils;
    TextView nameText;
    TextView designationText;
    TextView last_Login;
    RoundedImageView image_Profile;
    DrawerLayout adminDrawerLayout;
    TextView present_Count;
    TextView task_Count;
    TextView meeting_Count;
    TextView leave_Count;
    List<AdminTask> adminTask;
    List<MeetingDetailsAdmin> meetingDetailsAdmins;
    private TextView tvTittle;
    private RoundedImageView toolbarImage;
    private TextView logoutTextView;
    private ApiClient apiClient;
    private TextView eventDateTextView;
    private TextView eventTimeTextView;
    private TextView subjectTextView;
    private TextView descriptionText;
    private ImageView posterImageView;
    private ImageView cutImage;
    private LinearLayout event_Layouts;
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);
        // Screenshot restricted.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_home);

        apiClient = new ApiClient(this);

        preferences = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        Intent intent = new Intent(this, MyBackgroundLocationService.class);
        runConnectionCheckThread(this);
        ContextCompat.startForegroundService(this, intent);
        sharedUtils = new SharedUtils(this);
        drawerIcon = findViewById(R.id.drawerIcon);
        active_emp_image = findViewById(R.id.active_emp_image);
        active_emp_card = findViewById(R.id.active_emp_card);
        MeetingImg = findViewById(R.id.MeetingImg);
        salary_detail_image = findViewById(R.id.salary_detail_image);
        present_Count = findViewById(R.id.present_Count);
        task_Count = findViewById(R.id.task_Count);
        leave_Count = findViewById(R.id.leave_Count);
        meeting_Count = findViewById(R.id.meeting_Count);
        last_Login = findViewById(R.id.last_Login);
        admin_salary_details = findViewById(R.id.admin_salary_details);
        meeting_card_view = findViewById(R.id.meeting_card_view);
        task_Image_View = findViewById(R.id.task_Image_View);
        task_View = findViewById(R.id.task_View);
        image_Profile = findViewById(R.id.image_Profile);
        toolbarImage = findViewById(R.id.toolbarImage);
        leave_request_image = findViewById(R.id.leave_request_image);
        leave_request_Card = findViewById(R.id.leave_request_Card);
        nameText = findViewById(R.id.name_Texts);
        adminProfileImg = findViewById(R.id.adminProfileImg);
        admin_Profile = findViewById(R.id.admin_Profile);
        designationText = findViewById(R.id.designation_Texts);
        adminDrawerLayout = findViewById(R.id.adminDrawerLayout);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setItemTextColor(AppCompatResources.getColorStateList(this, R.color.menu_item_text_color_selector));
        adminAttendance = findViewById(R.id.adminAttendance);
        adminAttendanceImage = findViewById(R.id.adminAttendanceImage);
        adminLogoutImg = findViewById(R.id.adminLogoutImg);
        admin_Logout = findViewById(R.id.admin_Logout);
        logoutTextView = findViewById(R.id.logoutTextView);
        tvTittle = findViewById(R.id.tvTittle);

        // Event
        eventDateTextView = findViewById(R.id.eventDates);
        eventTimeTextView = findViewById(R.id.eventTimes);
        subjectTextView = findViewById(R.id.subjectTextView);
        descriptionText = findViewById(R.id.descriptionText);
        posterImageView = findViewById(R.id.posterImageView);
        cutImage = findViewById(R.id.cutImage);
        event_Layouts = findViewById(R.id.event_Layouts);

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
            descriptionText.setText(description);
            Glide.with(getApplicationContext()).load(posterImageUrl).into(posterImageView);
            event_Layouts.setVisibility(View.VISIBLE);
        } else {
            event_Layouts.setVisibility(View.GONE);
        }
        cutImage.setOnClickListener(v -> event_Layouts.setVisibility(View.GONE));

        // Redirect to Home Page
        tvTittle.setOnClickListener(view -> {
            Intent intent12 = new Intent(HomeActivity.this,HomeActivity.class);
            startActivity(intent12);
        });
        // Open Profile pic
        toolbarImage.setOnClickListener(view -> {
            ProfileFragment profileFragment = new ProfileFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, profileFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Get the saved token from SharedPreferences
        AtomicReference<SharedPreferences> preferences= new AtomicReference<>(getSharedPreferences("my_preferences", Context.MODE_PRIVATE));
        String token = preferences.get().getString("token", "0");
        long userId = preferences.get().getLong("userId", 0);

        // Call the API to get the current user data
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<User> call = apiInterface.getCurrentUser(token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        String firstName = user.getFirstName();
                        String lastName = user.getLastName();
                        String capitalizedFirstName = Character.toUpperCase(firstName.charAt(0)) + firstName.substring(1);
                        String capitalizedLastName = Character.toUpperCase(lastName.charAt(0)) + lastName.substring(1);
                        String name = capitalizedFirstName + " " + capitalizedLastName;
                        String designation = user.getDesignation();
                        if (nameText != null) {
                            nameText.setText(name);
                        }
                        if (designationText != null){
                            designationText.setText(designation);
                        }
                        View headerView = navigationView.inflateHeaderView(R.layout.admin_header_lay);
                        TextView nameText = headerView.findViewById(R.id.name_Texts);
                        TextView designationText = headerView.findViewById(R.id.designation_Texts);
                        RoundedImageView imageProfiles = headerView.findViewById(R.id.image_Profile);

                        // Show Profile image
                        String imageUrl = user.getProfileImage();
                        Glide.with(getApplicationContext()).load(imageUrl).into(imageProfiles);

                        nameText.setText(name.toUpperCase());

                        designationText.setText(designation);
                        RoundedImageView image = findViewById(R.id.toolbarImage);
                        String images = user.getProfileImage();
                        Glide.with(getApplicationContext()).load(images).into(image);
                        String formattedLastLogin = getFormattedLastLogin(user);
                        // Last Login
                        TextView lastLoginTextView = headerView.findViewById(R.id.last_Login);
                        lastLoginTextView.setText(formattedLastLogin);

                        boolean isActive = user.isActive();
                        View greens = findViewById(R.id.green_Dots);
                        // Set the green dot visibility based on the user's activity status
//                        if (isActive) {
//                            greens.setVisibility(View.VISIBLE);
//                        } else {
//                            greens.setVisibility(View.GONE);
//                        }
                    }
                } else {
                    Log.e("AdminProfile", "Failed to retrieve current user. Response code: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("AdminProfile", "Error retrieving current user: " + t.getMessage());
            }
        });

        // Monthly present count of Admin
        {Call<Integer> adminAttendanceCountCall = apiInterface.getUserMonthlyAttendanceCount(token, userId);
            adminAttendanceCountCall.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                    if (response.isSuccessful()) {
                        Integer count = response.body();
                        if (count != null) {
                            TextView presentCountTextView = findViewById(R.id.present_Count);
                            if (presentCountTextView != null) {
                                presentCountTextView.setText(String.valueOf(count));
                            }
                            // Print the response body in Logcat
                            Log.d("Attendance", "Present Count: " + count);
                        }
                    } else {
                        Log.e("API Error", "Failed to fetch attendance count. Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                    Log.e("API Error", "Server Error: " + t.getMessage());
                }
            });
        }
        // Get count of Today task.
         {
            ApiClient apiClient = new ApiClient(HomeActivity.this);
            apiInterface = apiClient.getApiInterface();
            Call<List<AdminTask>> callApi = apiInterface.getUserAllTask(token);
            callApi.enqueue(new Callback<List<AdminTask>>() {
                @Override
                public void onResponse(@NonNull Call<List<AdminTask>> call, @NonNull Response<List<AdminTask>> response) {
                    if (response.isSuccessful()) {
                        adminTask = response.body();
                        if (adminTask != null && !adminTask.isEmpty()) {
                            int totalTodayTasks = adminTask.size();
                            Log.d("TaskCount", "Total Today Tasks: " + totalTodayTasks);
                            TextView taskCountTextView = findViewById(R.id.task_Count);
                            taskCountTextView.setText(String.valueOf(totalTodayTasks));
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed to get task details.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<AdminTask>> call, @NonNull Throwable t) {
                    Log.e("TaskFragment", "Network error: " + t.getMessage());
                    Toast.makeText(HomeActivity.this, "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }


        // Get cont of Meeting.
        {
            Call<List<MeetingDetailsAdmin>> meetingCount= apiInterface.getAllMeeting(token, userId);
            meetingCount.enqueue(new Callback<List<MeetingDetailsAdmin>>() {
                @Override
                public void onResponse(@NonNull Call<List<MeetingDetailsAdmin>> call, @NonNull Response<List<MeetingDetailsAdmin>> response) {
                    if (response.isSuccessful()) {
                        meetingDetailsAdmins = response.body();
                        if (meetingDetailsAdmins != null && !meetingDetailsAdmins.isEmpty()) {
                            int meetingCount = meetingDetailsAdmins.size();
                            String meetingCountString = Integer.toString(meetingCount);
                            Log.d("MeetingCount", "Meeting Count: " + meetingCountString);
                            TextView taskCountTextView = findViewById(R.id.meeting_Count);
                            taskCountTextView.setText((meetingCountString));
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<List<MeetingDetailsAdmin>> call, @NonNull Throwable t) {
                    Toast.makeText(HomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
            // Get count of Leave Request.
             {
                Call<List<LeaveRequest>> leaveCount = apiInterface.getAllPendingLeave(token);
                leaveCount.enqueue(new Callback<List<LeaveRequest>>() {
                @Override
                public void onResponse(@NonNull Call<List<LeaveRequest>> call, @NonNull Response<List<LeaveRequest>> response) {
                    List<LeaveRequest> leaveList1 = response.body();
                    if (leaveList1 != null && !leaveList1.isEmpty()) {
                        for (LeaveRequest leave : leaveList1) {
                            Log.d("Leave Request", "LeaveRequest: " + leave.toString());
                        }
                        int leaveRequestCount = leaveList1.size();
                        String leaveCountString = Integer.toString(leaveRequestCount);
                        TextView leaveCountTextView = findViewById(R.id.leave_Count);
                        leaveCountTextView.setText((leaveCountString));
                        Log.d("LeaveCount", "Leave Request Count: " + leaveCountString);
                    } else {
                        Log.e("UserLeaveFragment", "User leave is null or empty");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<LeaveRequest>> call, @NonNull Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(HomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Navigation Drawer setup.
        drawerIcon.setOnClickListener(v -> adminDrawerLayout.openDrawer(GravityCompat.START));
        sharedUtils = new SharedUtils(this);
        navigationView.setNavigationItemSelectedListener(item -> {
            Log.d("MenuItemClick", "Item selected: " + item.getTitle());
            int id = item.getItemId();
            if (id == R.id.menuHomes) {
                Intent intent1 = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent1);
            }
            else if (id == R.id.menu_Performances) {
                fetchPerformanceReportAndOpenFragment();
            }
            else if (id == R.id.menu_attendance) {
                fetchAttendanceGraphAndOpenFragment();
            }
            else if (id == R.id.menu_today_present) {
                fetchTodayPresentEmpAndOpenFragment();
            }
            else if (id == R.id.menu_approved_leaves) {
                fetchApproveLeaveAndOpenFragment();
            }
            else if (id == R.id.menu_addEvent) {
                Fragment newFragment = new AddEventFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
                adminDrawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(HomeActivity.this, "Add Event", Toast.LENGTH_SHORT).show();
            }

//            else if (id == R.id.menu_admin_message) {
//                Fragment newFragment = new AdminMessageFragment();
//                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
//                adminDrawerLayout.closeDrawer(GravityCompat.START);
//                Toast.makeText(HomeActivity.this, "Admin Message", Toast.LENGTH_SHORT).show();
//            }

            else if (id == R.id.update_profile) {
                Fragment newFragment = new UpdateProfileFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
                adminDrawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(HomeActivity.this, "Update Profile", Toast.LENGTH_SHORT).show();
            }
            else if (id == R.id.menu_document) {
                Fragment newFragment = new EmployeeDocFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
                adminDrawerLayout.closeDrawer(GravityCompat.START);

            }
            else if (id == R.id.menu_assignTask) {
                Fragment newFragment = new AssignTaskFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
                adminDrawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(HomeActivity.this, "Assign Task", Toast.LENGTH_SHORT).show();
            }
            else if (id == R.id.menu_assignMeeting) {
                Fragment newFragment = new AssignMeetingFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
                adminDrawerLayout.closeDrawer(GravityCompat.START);
                Toast.makeText(HomeActivity.this, "Assign Meeting", Toast.LENGTH_SHORT).show();
            }

            // Change Password
            else if (id == R.id.menu_update_password) {
                adminDrawerLayout.closeDrawer(GravityCompat.START);
                Popup.changePassword(this);
            }

            // Logout
            else if (id == R.id.admin_menu_logout) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                View customView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                TextView dialogTitle = customView.findViewById(R.id.dialogTitle);
                TextView dialogMessage = customView.findViewById(R.id.dialogMessage);
                ImageView iconYes = customView.findViewById(R.id.iconYes);
                ImageView iconNo = customView.findViewById(R.id.iconNo);

                // Customize the title and message
                dialogTitle.setText("Sign Off");
                dialogMessage.setText("Are you sure you want to sign off ?");
                iconYes.setImageResource(R.drawable.checkmark);
                iconNo.setImageResource(R.drawable.cross);

                final AlertDialog[] alertDialog = {null};
                iconYes.setOnClickListener(view -> {
                    apiClient.logout();

                    preferences.set(sharedUtils.getSharedPreferencesContext());
                    preferences.get().edit().clear().apply();
                    stopService(intent);
                    UserAttendanceFragment.stopLocationUpdate();
                    finish();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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

                // Create the AlertDialog here
                alertDialog[0] = builder.create();
                alertDialog[0].show();
            }
            adminDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        logoutTextView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
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
                // Clear the token from local storage.
                apiClient.logout();
                preferences.set(sharedUtils.getSharedPreferencesContext());
                preferences.get().edit().clear().apply();
                stopService(intent);
                UserAttendanceFragment.stopLocationUpdate();
                finish();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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

        admin_Profile.setOnClickListener(v -> {
            Fragment newFragment = new AdminProfileFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            adminDrawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(HomeActivity.this, "Profile", Toast.LENGTH_SHORT).show();
        });

        adminProfileImg.setOnClickListener(v -> {
            Fragment newFragment = new AdminProfileFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            adminDrawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(HomeActivity.this, "Profile", Toast.LENGTH_SHORT).show();
        });

        active_emp_image.setOnClickListener(v -> {
            Fragment newFragment = new AllActiveUserFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            adminDrawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(HomeActivity.this, "All Active Users", Toast.LENGTH_SHORT).show();
        });

        active_emp_card.setOnClickListener(v -> {
            Fragment newFragment = new AllActiveUserFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            adminDrawerLayout.closeDrawer(GravityCompat.START);
            Toast.makeText(HomeActivity.this, "All Active Users", Toast.LENGTH_SHORT).show();
        });

        adminAttendance.setOnClickListener(v -> {
            Fragment newFragment = new AdminAttendanceFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(HomeActivity.this, "Attendance", Toast.LENGTH_SHORT).show();
        });
        adminAttendanceImage.setOnClickListener(v -> {
            Fragment newFragment = new AdminAttendanceFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
            Toast.makeText(HomeActivity.this, "Attendance", Toast.LENGTH_SHORT).show();
        });

        leave_request_image.setOnClickListener(v -> fetchLeaveRequestAndOpenFragment());
        leave_request_Card.setOnClickListener(v -> fetchLeaveRequestAndOpenFragment());

        task_View.setOnClickListener(v -> fetchTodayTaskAndOpenFragment());
        task_Image_View.setOnClickListener(v -> fetchTodayTaskAndOpenFragment());

        MeetingImg.setOnClickListener(v -> fetchAllMeetingsAndOpenFragment());
        meeting_card_view.setOnClickListener(v -> fetchAllMeetingsAndOpenFragment());

        salary_detail_image.setOnClickListener(v -> fetchAllSalaryDetailAndOpenFragment());

        admin_salary_details.setOnClickListener(v -> fetchAllSalaryDetailAndOpenFragment());

        // Declare alertDialog and initialize it to null
        final AlertDialog[] alertDialog = {null};

        adminLogoutImg.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            // Inflate the custom layout
            View customView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
            TextView dialogTitle = customView.findViewById(R.id.dialogTitle);
            TextView dialogMessage = customView.findViewById(R.id.dialogMessage);
            ImageView iconYes = customView.findViewById(R.id.iconYes);
            ImageView iconNo = customView.findViewById(R.id.iconNo);

            dialogTitle.setText("Sign Off");
            dialogMessage.setText("Are you sure you want to sign off?");
            // Set icons for "Yes" and "No"
            iconYes.setImageResource(R.drawable.checkmark);
            iconNo.setImageResource(R.drawable.cross);

            iconYes.setOnClickListener(view -> {
                // Clear the token from local storage.
                apiClient.logout();
                preferences.set(sharedUtils.getSharedPreferencesContext());
                preferences.get().edit().clear().apply();
                stopService(intent);
                UserAttendanceFragment.stopLocationUpdate();
                finish();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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

            // Create the AlertDialog here
            alertDialog[0] = builder.create();
            alertDialog[0].show();
        });

        admin_Logout.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);

            View customView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
            TextView dialogTitle = customView.findViewById(R.id.dialogTitle);
            TextView dialogMessage = customView.findViewById(R.id.dialogMessage);
            ImageView iconYes = customView.findViewById(R.id.iconYes);
            ImageView iconNo = customView.findViewById(R.id.iconNo);

            dialogTitle.setText("Sign Off");
            dialogMessage.setText("Are you sure you want to sign off ?");

            iconYes.setImageResource(R.drawable.checkmark);
            iconNo.setImageResource(R.drawable.cross);

            // Set an OnClickListener for the checkmark icon
            iconYes.setOnClickListener(view -> {
                preferences.set(sharedUtils.getSharedPreferencesContext());
                preferences.get().edit().clear().apply();
                stopService(intent);
                UserAttendanceFragment.stopLocationUpdate();
                finish();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
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

            // Create the AlertDialog here
            alertDialog[0] = builder.create();
            alertDialog[0].show();
        });
    }

    @Nullable
    private static String getFormattedLastLogin(User user) {
        long lastLoginTimestamp = user.getLastLogin();
        // Convert the timestamp to a Date object
        Date lastLoginDate = new Date(lastLoginTimestamp);
        SimpleDateFormat sdf = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.US);
        }
        String formattedLastLogin = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            formattedLastLogin = sdf.format(lastLoginDate);
        }
        return formattedLastLogin;
    }

    private void fetchLeaveRequestAndOpenFragment() {
        ApiClient apiClient = new ApiClient(HomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<LeaveRequest>> call = apiInterface.getAllPendingLeave(token);
        call.enqueue(new Callback<List<LeaveRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<LeaveRequest>> call, @NonNull Response<List<LeaveRequest>> response) {
                if (response.isSuccessful()) {
                    List<LeaveRequest> leaveList = response.body();
                    if (leaveList != null && !leaveList.isEmpty()) {
                        // Data is available, open the fragment
                        openLeaveRequestFragment();
                    } else {
                        // Data is not available, do not open the fragment
                        Toast.makeText(HomeActivity.this, "No leave request found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to get leave request details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LeaveRequest>> call, @NonNull Throwable t) {
                Log.e("LeaveRequestAdmin", "Network error: " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchTodayTaskAndOpenFragment() {
        ApiClient apiClient = new ApiClient(HomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<AdminTask>> call = apiInterface.getUserAllTask(token);
        call.enqueue(new Callback<List<AdminTask>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminTask>> call, @NonNull Response<List<AdminTask>> response) {
                if (response.isSuccessful()) {
                    adminTask = response.body();
                    if (adminTask != null && !adminTask.isEmpty()) {
                        // Data is available, open the fragment
                        openTodayTaskFragment();
                    } else {
                        // Data is not available, do not open the fragment
                        Toast.makeText(HomeActivity.this, "No tasks found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to get task details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminTask>> call, @NonNull Throwable t) {
                Log.e("TaskFragment", "Network error: " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchAllMeetingsAndOpenFragment() {
        ApiClient apiClient = new ApiClient(HomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<MeetingDetailsAdmin>> call = apiInterface.getAllMeeting(token, userId);
        call.enqueue(new Callback<List<MeetingDetailsAdmin>>() {
            @Override
            public void onResponse(@NonNull Call<List<MeetingDetailsAdmin>> call, @NonNull Response<List<MeetingDetailsAdmin>> response) {
                if (response.isSuccessful()) {
                    List<MeetingDetailsAdmin> meetingList = response.body();
                    if (meetingList != null && !meetingList.isEmpty()) {
                        openMeetingFragment();
                    } else {
                        Toast.makeText(HomeActivity.this, "No meetings found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to fetch meetings.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MeetingDetailsAdmin>> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchAllSalaryDetailAndOpenFragment() {
        ApiClient apiClient = new ApiClient(HomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<AdminSalaryDetails>> call = apiInterface.getAllSalaryDetails(token);
        call.enqueue(new Callback<List<AdminSalaryDetails>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminSalaryDetails>> call, @NonNull Response<List<AdminSalaryDetails>> response) {
                if (response.isSuccessful()) {
                    List<AdminSalaryDetails>   adminSalaryDetails = response.body();
                    if (adminSalaryDetails != null && !adminSalaryDetails.isEmpty()) {
                        openSalaryDetailFragment();
                    } else {
                        Toast.makeText(HomeActivity.this, "No payroll found.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to get meeting details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminSalaryDetails>> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchPerformanceReportAndOpenFragment() {
        ApiClient apiClient = new ApiClient(HomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<EmpPerformance>> call = apiInterface.getSingleEmployeeAllPerformanceByEmpId(token, userId);
        call.enqueue(new Callback<List<EmpPerformance>>() {
            @Override
            public void onResponse(@NonNull Call<List<EmpPerformance>> call, @NonNull Response<List<EmpPerformance>> response) {
                if (response.isSuccessful()) {
                    List<EmpPerformance> empPerformanceList  = response.body();
                    if (empPerformanceList != null && !empPerformanceList.isEmpty()) {
                        openPerformanceFragment();
                    } else {
                        Toast.makeText(HomeActivity.this, "Performance report not found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to fetch empPerformance list.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EmpPerformance>> call, @NonNull Throwable t) {
                Log.e("EmployeePerformance", "Server error", t);
            }
        });
    }
    private void fetchAttendanceGraphAndOpenFragment() {
        ApiClient apiClient = new ApiClient(HomeActivity.this);
        apiInterface = apiClient.getApiInterface();

        Call<ResponseBody> call = apiInterface.getMonthlyAttendance(token, userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        try {
                            String responseData = responseBody.string();
                            if (!responseData.isEmpty()) {
                                openAttendanceGraphFragment();
                            } else {
                                Toast.makeText(HomeActivity.this, "Monthly Attendance.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("AttendanceGraph", "Null response body");
                    }
                } else {
                    Log.e("AttendanceGraph", "Failed to fetch monthly attendance data. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                // Handle failure
                Log.e("AttendanceGraph", "Server error: " + t.getMessage());
            }
        });
    }
    private void fetchTodayPresentEmpAndOpenFragment() {
        ApiClient apiClient = new ApiClient(HomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<PresentEmployee>> call = apiInterface.getAllTodayAttendance(token);
        call.enqueue(new Callback<List<PresentEmployee>>() {
            @Override
            public void onResponse(@NonNull Call<List<PresentEmployee>> call, @NonNull Response<List<PresentEmployee>> response) {
                if (response.isSuccessful()) {
                    List<PresentEmployee> presentEmployees  = response.body();
                    if (presentEmployees != null && !presentEmployees.isEmpty()) {
                        openPresentEmployeeFragment();
                    } else {
                        Toast.makeText(HomeActivity.this, "No present employee found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to get attendance details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PresentEmployee>> call, @NonNull Throwable t) {
                Log.e("TodayPresentEmployee","Network error: " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchApproveLeaveAndOpenFragment() {
        ApiClient apiClient = new ApiClient(HomeActivity.this);
        apiInterface = apiClient.getApiInterface();
        Call<List<ApprovedLeaves>> call = apiInterface.getApprovedLeaves(token);

        call.enqueue(new Callback<List<ApprovedLeaves>>() {
            @Override
            public void onResponse(@NonNull Call<List<ApprovedLeaves>> call, @NonNull Response<List<ApprovedLeaves>> response) {
                if (response.isSuccessful()) {
                    List<ApprovedLeaves> leaveList = response.body();
                    if (leaveList != null && !leaveList.isEmpty()) {
                      openApproveLeaveRequest();
                    } else {
                        Toast.makeText(HomeActivity.this, "No leave request found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("UserLeaveFragment", "Failed to fetch user leave");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ApprovedLeaves>> call, @NonNull Throwable t) {
                Log.e("UserLeaveFragment", "API call failed" + t);
            }
        });
    }


    private void openLeaveRequestFragment() {
        Fragment newFragment = new LeaveRequestAdminFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(HomeActivity.this, "Leave Request", Toast.LENGTH_SHORT).show();
    }

    private void openTodayTaskFragment() {
        Fragment newFragment = new AdminTaskFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(HomeActivity.this, "Today Task", Toast.LENGTH_SHORT).show();
    }

    private void openMeetingFragment() {
        Fragment newFragment = new MeetingFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(HomeActivity.this, "Meetings", Toast.LENGTH_SHORT).show();
    }

    private void openSalaryDetailFragment() {
        Fragment newFragment = new SalaryDetailsAdminFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        Toast.makeText(HomeActivity.this, "Payroll Panel", Toast.LENGTH_SHORT).show();
    }

    private void openPerformanceFragment() {
        Fragment newFragment = new PerformanceFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        adminDrawerLayout.closeDrawer(GravityCompat.START);
        Toast.makeText(HomeActivity.this, "Performance", Toast.LENGTH_SHORT).show();
    }

    private void openAttendanceGraphFragment() {
        Fragment newFragment = new AttendanceGraphFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        adminDrawerLayout.closeDrawer(GravityCompat.START);
        Toast.makeText(HomeActivity.this, "Attendance Graph", Toast.LENGTH_SHORT).show();
    }

    private void openPresentEmployeeFragment() {
        Fragment newFragment = new TodayPresentEmployeeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        adminDrawerLayout.closeDrawer(GravityCompat.START);
        Toast.makeText(HomeActivity.this, "Today Present Employee", Toast.LENGTH_SHORT).show();
    }
    private void openApproveLeaveRequest() {
        Fragment newFragment = new ApprovedFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, newFragment, newFragment.getTag()).addToBackStack(newFragment.getTag()).commit();
        adminDrawerLayout.closeDrawer(GravityCompat.START);
        Toast.makeText(HomeActivity.this, "Approve Leave Request", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (adminDrawerLayout.isDrawerOpen(GravityCompat.START)){
            adminDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }else if (getSupportFragmentManager().getBackStackEntryCount() == 0){
            alertDialog();
        }else{
            super.onBackPressed();
        }
    }

    public void alertDialog() {
        // Check if the activity is in a valid state
        if (!isFinishing() && !isDestroyed()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Exit Alert");
            alertDialogBuilder.setMessage("Do you want to exit?");
            alertDialogBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                dialog.dismiss();
                finish();
            });

            alertDialogBuilder.setNegativeButton(android.R.string.no, (dialog, which) -> {
                dialog.dismiss();
            }).setIcon(R.drawable.exit_to_app);

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isMyServiceRunning(MyBackgroundLocationService.class)) {
            ContextCompat.startForegroundService(this, new Intent(this, MyBackgroundLocationService.class));
        }
        CommonUtils.isGpsEnabled(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
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