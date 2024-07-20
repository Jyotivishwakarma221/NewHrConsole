package com.investmango.hrconsole.user.fragment;

import static android.os.Looper.getMainLooper;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.Attendance;
import com.investmango.hrconsole.model.LocationModel;
import com.investmango.hrconsole.service.CommonUtil;
import com.investmango.hrconsole.service.CommonUtils;
import com.investmango.hrconsole.service.DateAndTimeUtility;
import com.investmango.hrconsole.service.SharedPreferencesConstants;
import com.investmango.hrconsole.service.SharedUtils;
import com.investmango.hrconsole.user.adapter.FirebaseService.FirebaseConnection;
import com.investmango.hrconsole.user.adapter.UserAttendanceHistoryAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAttendanceFragment extends Fragment {
    RecyclerView recyclerView;
    private List<Attendance> attendances;
    private ApiInterface apiInterface;
    SharedUtils sharedUtils;
    private TextView currentInTime;
    private TextView currentOutTime;
    private String attendanceString;
    private static FusedLocationProviderClient mLocationClient;
    private static LocationCallback mLocationCallback;
    private static Context mContext;
    private long userId;
    private SharedPreferences preferences;
    private String token;
    private ProgressDialog progressDialog;
    private static int count = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_attendance, container, false);

        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog); 
        progressDialog.setMessage("Loading attendance...");
        progressDialog.setCancelable(false);

        // Create an instance of ApiClient and initialize apiInterface
        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        // Initialize SharedPreferences and retrieve the token and userId
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId",0);

        sharedUtils = new SharedUtils(getActivity());
        mContext = getContext();
        recyclerView = view.findViewById(R.id.attendanceHistoryRecyclerView);
        ImageView inTimeAttendanceIcon = view.findViewById(R.id.inTimeAttendanceImg);
        ImageView outTimeAttendanceIcon = view.findViewById(R.id.outTimeAttendanceImg);
        TextView currentDate = view.findViewById(R.id.currentAttendanceDate);
        currentInTime = view.findViewById(R.id.currentInTimeAttendance);
        currentOutTime = view.findViewById(R.id.currentOutTimeAttendance);
        mLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mLocationCallbackInitialization();

        inTimeAttendanceIcon.setOnClickListener(v -> {
            if (CommonUtils.isGpsEnabled(getActivity())) {
                try {
                    if (sharedUtils.getUserAttendancePreference(SharedPreferencesConstants.OUT).trim().isEmpty() || !sharedUtils.getUserAttendancePreference(SharedPreferencesConstants.OUT).equals(
                            CommonUtil.getDate())) {

                        if (sharedUtils.getUserAttendancePreference(SharedPreferencesConstants.IN).trim().isEmpty() || !sharedUtils.getUserAttendancePreference(SharedPreferencesConstants.IN)
                                .equals(CommonUtil.getDate())) {

                            attendanceString = "IN";
                            mLocationCallbackInitialization();
                            getLocationUpdates();
                        } else {
                            Toast.makeText(mContext, "Already user updated in time.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, "Already user updated out time.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Log the current date when the punch-in button is clicked
            Log.d("UserAttendanceFragment", "Current date: " + CommonUtil.getDate());
        });

        currentDate.setText(CommonUtil.getDate());
        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentInTime.setText(new SimpleDateFormat("hh:mm:ss", Locale.US).format(new Date()));
                currentOutTime.setText(new SimpleDateFormat("hh:mm:ss", Locale.US).format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        outTimeAttendanceIcon.setOnClickListener(v -> {
            try {
                if (!sharedUtils.getUserAttendancePreference(SharedPreferencesConstants.IN).trim().isEmpty()
                        && sharedUtils.getUserAttendancePreference(SharedPreferencesConstants.IN)
                        .equals(CommonUtil.getDate())) {

                    if (sharedUtils.getUserAttendancePreference(SharedPreferencesConstants.OUT).isEmpty()
                            || !sharedUtils.getUserAttendancePreference(SharedPreferencesConstants.OUT)
                            .equals(CommonUtil.getDate())) {
                        attendanceString = "OUT";
                        saveUserAttendance(sharedUtils.getStringPreference(SharedPreferencesConstants.LAT_LONG));
                    } else {
                        Toast.makeText(mContext, "Already user updated out time.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, "Please updated in time.", Toast.LENGTH_SHORT).show();
                }
                // Log the current date when the punch-out button is clicked
                Log.d("UserAttendanceFragment", "Current date: " + CommonUtil.getDate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        attendanceList(userId);
        progressDialog.show(); // Show the progress dialog before making the API call
        return view;
    }

    private void mLocationCallbackInitialization() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (mLocationCallback == null) {
                    return;
                }
                Location locations = locationResult.getLastLocation();
                userLocationResult(Objects.requireNonNull(locations));
            }
        };
    }

    public static void stopLocationUpdate() {
        if (mLocationClient != null) {
            mLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void userLocationResult(Location locations) {
        LocationModel locationModel = new LocationModel();
        double latitude = locations.getLatitude();
        double longitude = locations.getLongitude();
        String latLng = latitude + "," + longitude;
        locationModel.setDateAndTime(DateAndTimeUtility.getCurrentDateAndTime());
        locationModel.setLatitude(latitude);
        locationModel.setLongitude(longitude);

        if (userId > 0) {
            FirebaseConnection.saveCoordinates(locationModel, userId);
            sharedUtils.setStringPreference(SharedPreferencesConstants.LAT_LONG, latLng);
            if (sharedUtils.getStringPreference(SharedPreferencesConstants.IN) == null || !sharedUtils.getStringPreference(SharedPreferencesConstants.IN).equalsIgnoreCase(CommonUtil.getDate())) {
                saveUserAttendance(latLng);
            }
        }
    }

    private void saveUserAttendance(String latLng) {
        Attendance attendance;
        if (attendanceString.equalsIgnoreCase("IN"))
        {
            attendanceString = "";
            attendance = new Attendance();
            attendance.setInLatLong(latLng);
            saveUserInTimeAndLocation(attendance, userId);
        }
        else if (attendanceString.equalsIgnoreCase("OUT"))
        {
            attendanceString = "";
            attendance = new Attendance();
            attendance.setOutLatLong(latLng);
            updateUserOutTimeAndLocation(attendance, userId);
        }
    }

    private void getLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(5000).setFastestInterval(2000);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        HandlerThread handlerThread = new HandlerThread("LocationThread");
        handlerThread.start();
        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, handlerThread.getLooper());
    }

    private void updateUserOutTimeAndLocation(Attendance attendance, long userId) {
        Log.d("UserAttendanceFragment", "userId: " + userId);
        Call<ResponseBody> call = apiInterface.updateUserAttendance(token, attendance, userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        count = 0;
                        sharedUtils.setStringPreference(SharedPreferencesConstants.OUT, CommonUtil.getDate());
                        Toast.makeText(mContext, Objects.requireNonNull(response.body()).string(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sharedUtils.setLastUsedTimeInSharedPreference(DateAndTimeUtility.getCurrentDateAndTime());
                    attendanceList(userId);
                } else {
                    Toast.makeText(getActivity(), "Server error " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("Failure", response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Server error " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Failure", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void saveUserInTimeAndLocation(Attendance attendance, long userId) {
        Log.d("UserAttendanceFragment", "userId: " + userId);
        Call<ResponseBody> call = apiInterface.saveAttendance( attendance, userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        count++;
                        sharedUtils.setStringPreference(SharedPreferencesConstants.IN, CommonUtil.getDate());
                        String date = CommonUtil.getDate();
                        Log.d("CommonUtil Date", date);
                        Toast.makeText(mContext, Objects.requireNonNull(response.body()).string(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sharedUtils.setLastUsedTimeInSharedPreference(DateAndTimeUtility.getCurrentDateAndTime());
                    Log.d("UserAttendanceFragment", "Current Date and Time: " + DateAndTimeUtility.getCurrentDateAndTime());
                    attendanceList(userId);
                } else {
                    Toast.makeText(getActivity(), "Server error " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("Failure", response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Server error " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Failure", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    public void attendanceList(long userId) {
        Log.d("UserAttendanceFragment", "userId: " + userId);
        Call<List<Attendance>> call = apiInterface.getUserAttendance( userId);
        call.enqueue(new Callback<List<Attendance>>() {
            @Override
            public void onResponse(@NonNull Call<List<Attendance>> call, @NonNull Response<List<Attendance>> response) {
                progressDialog.dismiss(); // Dismiss the progress dialog after receiving a response
                if (response.code() == 200) {
                    sharedUtils.setLastUsedTimeInSharedPreference(DateAndTimeUtility.getCurrentDateAndTime());
                    attendances = response.body();
                    setUserAttendanceAdapter(attendances);
                } else {
                    Toast.makeText(mContext, "Server error " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("Failure", response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Attendance>> call, @NonNull Throwable t) {
                progressDialog.dismiss(); // Dismiss the progress dialog on failure
                Toast.makeText(mContext, "Server error " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Failure", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    public void setUserAttendanceAdapter(List<Attendance> attendanceList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        UserAttendanceHistoryAdapter adapter = new UserAttendanceHistoryAdapter(attendanceList, getActivity());
        recyclerView.setAdapter(adapter);
    }
}
