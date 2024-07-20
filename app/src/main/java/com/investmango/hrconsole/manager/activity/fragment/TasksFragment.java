package com.investmango.hrconsole.manager.activity.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.investmango.hrconsole.EmployeeAction.AssignTask;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.databinding.FragmentTasksBinding;
import com.investmango.hrconsole.manager.activity.ManagerActivity;
import com.investmango.hrconsole.model.TaskItems;
import com.investmango.hrconsole.model.TaskResponse;
import com.investmango.hrconsole.model.TotalEmpResponseItem;
import com.investmango.hrconsole.model.UpdateTaskStatus;
import com.investmango.hrconsole.service.Constant;
import com.investmango.hrconsole.service.DateAndTimeUtility;
import com.shawnlin.numberpicker.NumberPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TasksFragment extends Fragment {
    private FragmentTasksBinding binding;
    private List<TaskItems> tasks;
    private long userId;
    private String from;
    List<String> employeList;
    private ApiInterface apiInterface;
    List<TotalEmpResponseItem> allActiveUsers;
    private String currentFileName = "";
    TextView startDate, endDate;
    Boolean isFiltered = false;
    List<?> list;
    private static final String CHANNEL_ID = "download_notification_channel";

    int count = 0;
    String status = "";
    long startingDate, endingDate, childuserId;
    ;
    String authority;
    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_tasks, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences preferences = getContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        userId = preferences.getLong("userId", 0);
        authority = preferences.getString("Authority", "");

        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        if (authority.equals(Constant.MANAGER)) {
            employeList = getChildActiveuser();
        } else if (authority.equals(Constant.ADMIN)) {
            employeList = getAllActiveUser();
        }
        getbundle();


        binding.numberPicker.setMaxValue(20);
        binding.numberPicker.setOrientation(LinearLayout.VERTICAL);
        binding.numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.d("numberPicker", String.format(Locale.US, "oldVal: %d, newVal: %d", oldVal, newVal));

            }
        });
        binding.previousPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (count == 0) {
                    count = 0;
                } else {
                    count--;
                    if (isFiltered) {
                        getFiltered(status, startingDate, endingDate);
                    } else {
                        if (from.equals("Own")) {
                            getTaskList(status);
                        } else memberTaskOFManager();
                    }
                }
            }
        });
        binding.nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                count++;
                if (isFiltered) {
                    getFiltered(status, startingDate, endingDate);
                } else {
                    if (from.equals("Own")) {
                        getTaskList(status);
                    } else memberTaskOFManager();
                }

            }

        });

        binding.filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (from.equals("Own"))
                    showFilterBox(getContext());
                else {
                    if (authority.equals(Constant.MANAGER) || authority.equals(Constant.ADMIN)) {
                        showManagerOrAdminFilter(getContext());
                    } else if (authority.equals(Constant.USER))
                        showFilterBox(getContext());
                }
            }
        });

        list = Arrays.asList(getResources().getStringArray(R.array.TaskType));
        ArrayAdapter<?> adapter = new ArrayAdapter<>(getContext(), R.layout.color_spinner_layout, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.status.setAdapter(adapter);
        binding.status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.e("onitemClick", "onViewCreated: 1  " + position);
                    UpdateTaskStatus.Status selectedStatus = null;

                    if (position == 0) {

                    } else if (position == 1) {
                        selectedStatus = UpdateTaskStatus.Status.PENDING;
                    } else if (position == 2) {
                        selectedStatus = UpdateTaskStatus.Status.DONE;

                    }
                    UpdateTaskStatus requestBody = new UpdateTaskStatus();
                    if (tasks != null && !tasks.isEmpty())
                        requestBody.setId(tasks.get(0).getId());
                    requestBody.setStatus(selectedStatus);
                    ChangeTaskStatus(requestBody);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        binding.status.setOnItemClickListener((parent, view1, position, id) -> {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Log.e("onitemClick", "onViewCreated: 1  " + position);
//                UpdateTaskStatus.Status selectedStatus = null;
//                if (position == 0) {
//                    selectedStatus = UpdateTaskStatus.Status.PENDING;
//                } else if (position == 1) {
//                    selectedStatus = UpdateTaskStatus.Status.DONE;
//
//                }
//                UpdateTaskStatus requestBody = new UpdateTaskStatus();
//                requestBody.setId(tasks.get(0).getId());
//                requestBody.setStatus(selectedStatus);
//                ChangeTaskStatus(requestBody);
//            }
//        });

        binding.pagenumber.setOnClickListener(view1 -> {
            showCustomDialog();
        });
        binding.downloadIcon.setOnClickListener(view1 -> {
            showFilterBoxForDownload(requireContext());
        });

        binding.addnewTask.setOnClickListener(view1 -> {
            if (from.equals("Own")) {
                ((ManagerActivity) getContext()).replaceFragment(new NewTaskFragment());
            } else ((ManagerActivity) getContext()).replaceFragment(new AssignTask());

        });

        binding.taskComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewTaskFragment fragment = new NewTaskFragment();
                Bundle bb = new Bundle();
                ArrayList<TaskItems> task = new ArrayList<>();
                if (tasks != null && !tasks.isEmpty()) {
                    task.add(tasks.get(0));
                }

                bb.putSerializable("editTask", task);
                fragment.setArguments(bb);
                ((ManagerActivity) getContext()).replaceFragment(fragment);
            }
        });
    }

    private void getbundle() {
        assert getArguments() != null;
        from = getArguments().getString("ViewOf");
        if (from.equals("Own")) {
            getTaskList("");
            binding.taskreport.setVisibility(View.VISIBLE);
        } else {
            memberTaskOFManager();
            binding.Addcomments.setVisibility(View.VISIBLE);
            binding.myteamTask.setVisibility(View.VISIBLE);

        }
    }

    private void ChangeTaskStatus(UpdateTaskStatus requestBody) {
        Call<ResponseBody> call = apiInterface.updateUserTaskStatus(requestBody, userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
//                    Toast.makeText(getContext(), "Status updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("TaskAdapter", "Error updating task status", t);
                Toast.makeText(getContext(), "Failed to update task status. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleErrorResponse(Response<ResponseBody> response) {
        try {
            String errorResponse = response.errorBody().string();
            JSONObject errorObject = new JSONObject(errorResponse);
            String errorMessage = errorObject.optString("message", "Unknown Error");
            Log.e("taskfragemnet", "handleErrorResponse: " + errorMessage);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }


    private void showManagerOrAdminFilter(Context context) {

        BottomSheetDialog dialog1 = new BottomSheetDialog(context, R.style.BottomSheetDialog);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setCancelable(true);
        dialog1.setCanceledOnTouchOutside(true);
        dialog1.setContentView(R.layout.manger_admin_filter);
        Window window = dialog1.getWindow();
        assert window != null;
        window.setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        Spinner statusSpin = dialog1.findViewById(R.id.status);
        Spinner childSpinner = dialog1.findViewById(R.id.personal);
        TextView showResult = dialog1.findViewById(R.id.showresult);

        ArrayAdapter arrayAdapter = new ArrayAdapter(requireContext(), R.layout.color_spinner_layout, list);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        assert statusSpin != null;
        statusSpin.setAdapter(arrayAdapter);


        if (employeList != null && !employeList.isEmpty()) {
            ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<>(
                    requireContext(),
                    R.layout.color_spinner_layout,
                    employeList
            );
            arrayAdapter2.setDropDownViewResource(R.layout.spinner_dropdown_layout);

            assert childSpinner != null;
            childSpinner.setAdapter(arrayAdapter2);
            if (!employeList.isEmpty()) {
                childSpinner.setSelection(0);
            }

            childSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    childuserId = allActiveUsers.get(position).getId();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } else Toast.makeText(context, "Unable to Load employee.", Toast.LENGTH_SHORT).show();
        startDate = dialog1.findViewById(R.id.startDate);
        endDate = dialog1.findViewById(R.id.endDate);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker("start");

            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker("");
            }
        });

        showResult.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                long startdate = DateAndTimeUtility.dateToEpoch(startDate.getText().toString());
                Log.e("startdate", "showFilterBox: " + startdate);

                long enddate = DateAndTimeUtility.dateToEpoch(endDate.getText().toString());
//                if (from.equals("own")) {
//                    if (!statusSpin.getSelectedItem().toString().equals("--"))
//                    getFiltered(statusSpin.getSelectedItem().toString(), startdate, enddate);
//                    else getFiltered("", startdate, enddate);
//
//                } else {
                if (childuserId != 0) {
                    if (!statusSpin.getSelectedItem().toString().equals("--"))
                        getFilteredFromManger(statusSpin.getSelectedItem().toString(), startdate, enddate, childuserId);
                    else
                        getFilteredFromManger("", startdate, enddate, childuserId);
//                }
                }
                dialog1.dismiss();
            }
        });
        dialog1.show();
    }

    private List<String> getAllActiveUser() {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();
        List<String> userNames = new ArrayList();

        Call<List<TotalEmpResponseItem>> call = apiInterface.getAllEmployee(true);
        call.enqueue(new Callback<List<TotalEmpResponseItem>>() {
            @Override
            public void onResponse(Call<List<TotalEmpResponseItem>> call, Response<List<TotalEmpResponseItem>> response) {
                if (response.isSuccessful()) {
                    allActiveUsers = response.body();
                    for (int i = 0; i <= allActiveUsers.size() - 1; i++) {
                        userNames.add(allActiveUsers.get(i).getUserName().toUpperCase());
                    }
                } else
                    Toast.makeText(getContext(), "Something went wrrong", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<TotalEmpResponseItem>> call, Throwable t) {
                Log.e("onFailure", "onFailure: " + t.getMessage());
            }
        });
        return userNames;
    }

    private List<String> getChildActiveuser() {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();
        List<String> userNames = new ArrayList();

        Call<List<TotalEmpResponseItem>> call = apiInterface.getTotalEmp(userId, true);
        call.enqueue(new Callback<List<TotalEmpResponseItem>>() {
            @Override
            public void onResponse(Call<List<TotalEmpResponseItem>> call, Response<List<TotalEmpResponseItem>> response) {
                if (response.isSuccessful()) {
                    allActiveUsers = response.body();
                    for (int i = 0; i <= allActiveUsers.size() - 1; i++) {
                        userNames.add(allActiveUsers.get(i).getUserName().toUpperCase());
                    }
                } else
                    Toast.makeText(getContext(), "Something went wrrong", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<TotalEmpResponseItem>> call, Throwable t) {
                Log.e("onFailure", "onFailure: " + t.getMessage());
            }

        });
        return userNames;
    }

    private void showFilterBox(Context context) {

        BottomSheetDialog dialog1 = new BottomSheetDialog(context, R.style.BottomSheetDialog);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setCancelable(true);
        dialog1.setCanceledOnTouchOutside(true);
        dialog1.setContentView(R.layout.filter_layput);
        Window window = dialog1.getWindow();
        assert window != null;
        window.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);


        Spinner statusSpin = dialog1.findViewById(R.id.status);
        TextView showResult = dialog1.findViewById(R.id.showresult);


        ArrayAdapter arrayAdapter;
        arrayAdapter = new ArrayAdapter(requireContext(), R.layout.color_spinner_layout, list);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
        statusSpin.setAdapter(arrayAdapter);

        startDate = dialog1.findViewById(R.id.startDate);
        endDate = dialog1.findViewById(R.id.endDate);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker("start");

            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker("");
            }
        });

        showResult.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                long startdate = DateAndTimeUtility.dateToEpoch(startDate.getText().toString());
                Log.e("startdate", "showFilterBox: " + startdate);

                long enddate = DateAndTimeUtility.dateToEpoch(endDate.getText().toString());
                if (!statusSpin.getSelectedItem().toString().equals("--"))
                    getFiltered(statusSpin.getSelectedItem().toString(), startdate, enddate);
                else
                    getFiltered("", startdate, enddate);
                dialog1.dismiss();
            }
        });
        dialog1.show();
    }

    private void showFilterBoxForDownload(Context context) {

        BottomSheetDialog dialog1 = new BottomSheetDialog(context, R.style.BottomSheetDialog);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setCancelable(true);
        dialog1.setCanceledOnTouchOutside(true);
        dialog1.setContentView(R.layout.filter_layput);
        Window window = dialog1.getWindow();
        assert window != null;
        window.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);


        Spinner statusSpin = dialog1.findViewById(R.id.status);
        TextView showResult = dialog1.findViewById(R.id.showresult);
        TextView statusText = dialog1.findViewById(R.id.statustxt);
        statusSpin.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);

        startDate = dialog1.findViewById(R.id.startDate);
        endDate = dialog1.findViewById(R.id.endDate);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker("start");

            }
        });
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker("");
            }
        });

        showResult.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                long startdate = DateAndTimeUtility.dateToEpoch(startDate.getText().toString());
                Log.e("startdate", "showFilterBox: " + startdate);

                long enddate = DateAndTimeUtility.dateToEpoch(endDate.getText().toString());
                downloadUserTaskPdf(userId, startdate, enddate);

                dialog1.dismiss();
            }
        });
        dialog1.show();
    }

    private void openDatePicker(String type) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select Date");
        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            if (type == "start") {
                startDate.setText(selectedDate);
            } else endDate.setText(selectedDate);
        });
        picker.show(requireFragmentManager(), picker.toString());
    }


    @Override
    public void onResume() {
        super.onResume();
//        setadapter();
    }

    private void downloadUserTaskPdf(long userId, long startDateee, long endDatee) {

        progressDialog.show();
        String title = "PDF Download";
        final String[] message = {"Downloading PDF..."};

        showNotification(title, message[0]);
        if (startDateee == 0 || endDatee == 0) {
            Call<Void> call = apiInterface.getTask(userId);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        currentFileName = "UserTask" + timeStamp + ".pdf";
                        String pdfUrl = response.raw().request().url().toString();
                        startDownload(pdfUrl, currentFileName); // Use the unique file name

                        message[0] = "PDF download completed!";
                        Toast.makeText(getContext(), "PDF download completed!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("TaskFragment", "Failed to download task PDF");
                        message[0] = "Failed to download PDF.";
                    }
                    updateNotificationMessage(title, message[0], currentFileName);
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    message[0] = "Network Error. Please try again.";
                    updateNotificationMessage(title, message[0], currentFileName);
                }
            });
        } else {
            Call<Void> call = apiInterface.getFilteredTaskDownload(userId, startDateee, endDatee);
            Log.e("downloadUserTaskPdf", "downloadUserTaskPdf: " + startDateee + "  " + endDatee);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        String timeStamp = String.valueOf(System.currentTimeMillis());
                        currentFileName = "UserTask" + timeStamp + ".pdf";
                        String pdfUrl = response.raw().request().url().toString();
                        startDownload(pdfUrl, currentFileName); // Use the unique file name

                        message[0] = "PDF download completed!";
                        Toast.makeText(getContext(), "PDF download completed!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("TaskFragment", "Failed to download task PDF");
                        message[0] = "Failed to download PDF.";
                    }
                    updateNotificationMessage(title, message[0], currentFileName);
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    Log.e("SalaryTaskFragment", "Server error while downloading PDF", t);
                    message[0] = "Network Error. Please try again.";
                    updateNotificationMessage(title, message[0], currentFileName);
                }
            });
        }
    }

    private void updateNotificationMessage(String title, String message, String fileName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.iconhr)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create a pending intent to open the downloaded file using FileProvider
        Context context = requireContext();
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Get the FileProvider authority using your app's package name
        String authority = context.getApplicationContext().getPackageName() + ".provider";

        // Create a content URI for the file using FileProvider
        Uri fileUri = FileProvider.getUriForFile(context, authority, new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName));

        // Grant read permission to the content URI
        intent.setDataAndType(fileUri, context.getContentResolver().getType(fileUri));

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Clear existing task and create a new one
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build()); // You can use a unique notification ID
    }

    private void showNotification(String title, String message) {
        if (isNotificationPermissionGranted()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.iconhr)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(1, builder.build()); // You can use a unique notification ID
        }
    }

    private boolean isNotificationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
            }
        } else {
            return true;
        }
        return false;
    }

    private void startDownload(String fileUrl, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setTitle("Download File");
        request.setDescription("Downloading file");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
        }
    }

    private void showCustomDialog() {
        // Inflate the custom view
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_view, null);

        // Find views in the custom layout
        EditText customEditText = dialogView.findViewById(R.id.number);
        TextView showResult = dialogView.findViewById(R.id.showResult);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setCancelable(true);
        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();

        showResult.setOnClickListener(view -> {
            count = Integer.parseInt(customEditText.getText().toString());
            getTaskList("");
            alertDialog.dismiss();
        });

        // Show the AlertDialog
        alertDialog.show();
    }

    private void setAdapt() {
        try {
            if (tasks.get(0).getComments() != null)
                binding.taskComments.setText(tasks.get(0).getSubject() + "\n \n" + tasks.get(0).getComments());
            else binding.taskComments.setText(tasks.get(0).getSubject());
            binding.date.setText(DateAndTimeUtility.getDateAndTimeFromLong(tasks.get(0).getCreatedTime()));

            binding.id.setText(tasks.get(0).getUserId().toString());
            Integer position = list.indexOf(tasks.get(0).getStatus());

            binding.status.setSelection(position);
            binding.name.setText(tasks.get(0).getUserName().toString());
//            binding.recyclerView.adapter = CommonAdapter(this)
//            binding.recyclerView.layoutManager =
//                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        } catch (Exception e) {
            Log.e("TAG", "setAdapt: " + e);
        }

    }


    private void getTaskList(String status) {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();
        Call<TaskResponse> call = apiInterface.getAllTaskwithpage(userId, count, status, 1);
        call.enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                if (response.isSuccessful()) {
                    tasks = response.body().getContent();
                    Log.e("performance", "onResponse: " + tasks);
                    setAdapt();
                    if (count <= response.body().getTotalElements())
                        binding.pagenumber.setText(String.valueOf(count + " /" + response.body().getTotalElements()));

                } else {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Failed to fetch Task list.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                Log.e("EmployeePerformance", "Server error", t);
            }
        });
    }

    private void memberTaskOFManager() {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();
        Call<TaskResponse> call = apiInterface.memberTaskOFManager(userId, count, 1);
        Log.e("TaskFragment", "onResponse: " + userId);

        call.enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {

                if (response.isSuccessful()) {
                    tasks = response.body().getContent();
                    Log.e("TaskFragment", "onResponse: " + tasks);
                    setAdapt();
                    binding.pagenumber.setText(String.valueOf(count + " /" + response.body().getTotalElements()));

                } else {
                    try {
                        JSONObject errorObject = new JSONObject(response.errorBody().toString());
                        Log.e("TaskFragment", "onResponse: " + errorObject.optString("message"));

                    } catch (JSONException e) {
                        Log.e("TaskFragment", "onResponse: " + e);
//                        throw new RuntimeException(e);

                    }
                    Toast.makeText(getContext(), "Failed to fetch Task list.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                Log.e("EmployeePerformance", "Server error", t);
            }
        });
    }

    private void getFiltered(String statuss, long startDate, long endDate) {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();
        if (startDate == 0 || endDate == 0) {
            Call<TaskResponse> call = apiInterface.getFilterTaskwithpageWithoutDate(userId, count, statuss, 1);
            call.enqueue(new Callback<TaskResponse>() {
                @Override
                public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                    if (response.isSuccessful()) {
                        tasks = response.body().getContent();
                        Log.e("performance", "onResponse: " + tasks);
                        setAdapt();
                        isFiltered = true;

                        status = statuss;
                        startingDate = startDate;
                        endingDate = endDate;

                        binding.pagenumber.setText(count + " /" + response.body().getTotalElements());

                    } else {
                        if (requireContext()!=null)
                        Toast.makeText(requireContext(), "Failed to fetch Task list.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                    Log.e("EmployeePerformance", "Server error", t);
                }
            });
        } else {
            Call<TaskResponse> call = apiInterface.getFilterTaskwithpage(userId, count, statuss, startDate, endDate, 1);
            call.enqueue(new Callback<TaskResponse>() {
                @Override
                public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                    if (response.isSuccessful()) {
                        tasks = response.body().getContent();
                        Log.e("performance", "onResponse: " + tasks);
                        setAdapt();
                        isFiltered = true;

                        status = statuss;
                        startingDate = startDate;
                        endingDate = endDate;

                        binding.pagenumber.setText(count + " /" + response.body().getTotalElements());

                    } else {
                        Toast.makeText(getContext(), "Failed to fetch Task list.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                    Log.e("EmployeePerformance", "Server error", t);
                }
            });
        }
    }

    private void getFilteredFromManger(String statuss, long startDate, long endDate, long childId) {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();

        if (startDate == 0 || endDate == 0) {
            Call<TaskResponse> call = apiInterface.getFilterTaskwithpageWithoutDate(childId, count, statuss, 1);
            call.enqueue(new Callback<TaskResponse>() {
                @Override
                public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                    if (response.isSuccessful()) {
                        tasks = response.body().getContent();
                        Log.e("performance", "onResponse: " + tasks);
                        setAdapt();
                        isFiltered = true;

                        status = statuss;
                        startingDate = startDate;
                        endingDate = endDate;

                        binding.pagenumber.setText(String.valueOf(count + " /" + response.body().getTotalElements()));

                    } else {
                        Toast.makeText(getContext(), "Failed to fetch Task list.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                    Log.e("EmployeePerformance", "Server error", t);
                }
            });
        } else {
            Call<TaskResponse> call = apiInterface.getFilterTaskwithpage(childId, count, statuss, startDate, endDate, 1);
            call.enqueue(new Callback<TaskResponse>() {
                @Override
                public void onResponse(@NonNull Call<TaskResponse> call, @NonNull Response<TaskResponse> response) {
                    if (response.isSuccessful()) {
                        tasks = response.body().getContent();
                        Log.e("performance", "onResponse: " + tasks);
                        setAdapt();
                        isFiltered = true;

                        status = statuss;
                        startingDate = startDate;
                        endingDate = endDate;

                        binding.pagenumber.setText(String.valueOf(count + " /" + response.body().getTotalElements()));

                    } else {
                        Toast.makeText(getContext(), "Failed to fetch Task list.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                    Log.e("EmployeePerformance", "Server error", t);
                }
            });
        }
    }


}