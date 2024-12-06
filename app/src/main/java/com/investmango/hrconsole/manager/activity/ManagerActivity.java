package com.investmango.hrconsole.manager.activity;

import static com.investmango.hrconsole.service.ApplicationUtil.runConnectionCheckThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.investmango.hrconsole.EmployeeAction.EmployeeActions;
import com.investmango.hrconsole.EmployeeAction.ViewPagerAdap;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.commonclasses.InitialAvatarView;
import com.investmango.hrconsole.model.Event;
import com.investmango.hrconsole.model.User;
import com.investmango.hrconsole.profile.ProfileFragment;
import com.investmango.hrconsole.service.CommonUtils;
import com.investmango.hrconsole.service.Constant;
import com.investmango.hrconsole.service.DateAndTimeUtility;
import com.investmango.hrconsole.service.MyBackgroundLocationService;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerActivity extends AppCompatActivity {
    String authority;
    long userId;
    private long pressedTime;
    TabLayout tabs;
    Intent intent;
    ViewPager viewPager;
    RelativeLayout eventLay;
    ImageView cutImageView, poster,imgDrawer;
    TextView subjecttext;
    String posterImageUrl, formattedDate, formattedTime, description, subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manager);
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        authority = preferences.getString("Authority", "");


        TextView managerName = findViewById(R.id.managerName);
        subjecttext = findViewById(R.id.subject);
        eventLay = findViewById(R.id.eventLay);
        RoundedImageView image = findViewById(R.id.managerImageProfile);
        tabs = findViewById(R.id.tabs);
        cutImageView = findViewById(R.id.cutImageView);
        poster = findViewById(R.id.poster);
        viewPager = findViewById(R.id.viewPager);
        LinearLayout imgDrawer=findViewById(R.id.imgDrawer);
        InitialAvatarView avatarView=findViewById(R.id.initialAvatar);


        imgDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish(); // Finish the current activity
                startActivity(intent); // Restart the activity

            }
        });

        intent = new Intent(this, MyBackgroundLocationService.class);
        runConnectionCheckThread(this);
        ContextCompat.startForegroundService(this, intent);
//        mService.doForegroundThings();

        if (authority.equals(Constant.USER)){
            addFragment(new ManagerFragment());
        } else setAdapter();


        cutImageView.setOnClickListener(v -> {
            eventLay.setVisibility(View.GONE);
        });
        image.setOnClickListener(v -> {
            ProfileFragment fragmentOne = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("ProfileOne");
            if (fragmentOne != null && fragmentOne.isVisible()) {

            } else
                replaceFragment(new ProfileFragment(), "ProfileOne");
        });




        fetchUpcomingEvents(token);
        ApiClient apiClient = new ApiClient(getApplicationContext());
        ApiInterface apiInterface = apiClient.getApiInterface();
        Call<User> call = apiInterface.getCurrentUser();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        String username = user.getFirstName();
                        managerName.setText(getString(R.string.greeting_hi) + " " + username);

                        // Show Profile Image
                        String imageUrl = user.getProfileImage();
                        if (!Objects.equals(imageUrl, "")) {
                            Log.e("getProfileImage", "onResponse: " + imageUrl);
                            Glide.with(getApplicationContext()).load(imageUrl).into(image);
                            image.setVisibility(View.VISIBLE);
                            avatarView.setVisibility(View.GONE);
                        } else {
                            avatarView.setName(username);
                            image.setVisibility(View.GONE);
                            avatarView.setVisibility(View.VISIBLE);

                        }
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);


    }
    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        Log.e("replaceFragment", "replaceFragment: " + fragment.getClass().getSimpleName().toUpperCase());
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(fragment.getClass().getSimpleName().toUpperCase());
        transaction.commit();
    }
    public void replaceFragment2(Fragment fragment,String tag) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        Log.e("replaceFragment", "replaceFragment: " + fragment.getClass().getSimpleName().toUpperCase());
        transaction.replace(R.id.frameLayout, fragment);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

public void addFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        Log.e("replaceFragment", "replaceFragment: " + fragment.getClass().getSimpleName().toUpperCase());
        transaction.add(R.id.frameLayout, fragment);
        transaction.commit();

    }

    public void replaceFragment(Fragment fragment, String tag) {

        Log.e("fragmentCheck", "replaceFragment: " + fragment);

        FragmentManager manager = getSupportFragmentManager();

        // on below line creating a variable for fragment transaction.
        FragmentTransaction transaction = manager.beginTransaction();

        // on below line replacing the transaction with the fragment.
        transaction.replace(R.id.frameLayout, fragment, tag);

        // on below line adding back stack as null.
        transaction.addToBackStack(null);

        // on below line committing changes.
        transaction.commit();

    }

    private boolean isAnyFragmentPresent() {
        return !getSupportFragmentManager().getFragments().isEmpty();
    }

    private boolean isFragmentPresent(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return fragment != null && fragment.isVisible();
    }

    @Override
    public void onBackPressed() {
        Log.e("fragmentCheck", "replaceFragment: " + getSupportFragmentManager().getFragments().size());

//        if (getSupportFragmentManager().findFragmentById(R.id.frameLayout) instanceof ManagerFragment || getSupportFragmentManager().findFragmentById(R.id.frameLayout) instanceof EmployeeActions ) {
        if (getSupportFragmentManager().getFragments().size() == 2) {
            if (pressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                finish();
            } else {
                Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
            }
            pressedTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }

    }

    private void setAdapter() {
        ViewPagerAdap adapter = new ViewPagerAdap(getSupportFragmentManager(), 0);

        // add fragment to the list
        adapter.addFragment("My Console", new ManagerFragment());
        adapter.addFragment("My Team", new EmployeeActions());
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
    }

    private void fetchUpcomingEvents(String token) {

        ApiClient apiClient = new ApiClient(getApplicationContext());
        ApiInterface apiInterface = apiClient.getApiInterface();
        Call<List<Event>> call = apiInterface.upcomingEvents();
        call.enqueue(new Callback<List<Event>>() {
            @SuppressLint("SuspiciousIndentation")
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                if (response.isSuccessful()) {
                    List<Event> events = response.body();
                    if (events != null && !events.isEmpty()) {
                        String posterImageUrl = events.get(0).getPoster();
                        if (!TextUtils.isEmpty(posterImageUrl)) {
                            Log.d("Poster", "Poster ->" + posterImageUrl);
                        }
                        formattedDate = DateAndTimeUtility.getDATEFromLong(events.get(0).getEventDateTime());
                        formattedTime = DateAndTimeUtility.getTimeInHourFromLong(events.get(0).getEventDateTime());
                        description = events.get(0).getDescription();
                        subject = events.get(0).getSubject();

                        if (subject != null)
                            subjecttext.setText(subject);
                        if (poster != null)
                            Glide.with(getApplicationContext()).load(posterImageUrl).into(poster);
                        eventLay.setVisibility(View.VISIBLE);
                    } else eventLay.setVisibility(View.GONE);
                } else {
                    // Handle unsuccessful response
                    handleApiCallFailure(response);
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                // Handle failure
                Log.e("UpcomingEvents", "Failed to fetch upcoming events: " + t.getMessage());
                Toast.makeText(ManagerActivity.this, "Failed to fetch upcoming events: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApiCallFailure(@Nullable Response<List<Event>> response) {
        if (response != null && response.errorBody() != null) {
            try {
                // Read the error body as a string
                String errorBodyString = response.errorBody().string();

                // Convert the string to a JSONObject
                JSONObject errorJson = new JSONObject(errorBodyString);
                String errorMessage = errorJson.getString("message");

                // Log the error message
                Log.e("handleApiCallFailure", errorMessage);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("handleApiCallFailure", "IOException: " + e.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("handleApiCallFailure", "JSONException: " + e.toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CommonUtils.isGpsEnabled(this);
    }

    public void stoploactionService() {
        stopService(intent);
    }
}
