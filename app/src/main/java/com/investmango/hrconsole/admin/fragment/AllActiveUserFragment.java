package com.investmango.hrconsole.admin.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.adapter.AllActiveUserAdapter;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.AllActiveUsers;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllActiveUserFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<AllActiveUsers> allActiveUsers;
    private ApiInterface apiInterface;
    private String token;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final int PERMISSION_STORAGE_CODE = 1000;
    private static final String CHANNEL_ID = "download_notification_channel";
    private boolean isDownloading = false; // Flag to track if a download is in progress
    private String currentFileName = ""; // Store the current downloaded file name

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_active_user, container, false);
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        imageView.setOnClickListener(v -> showDownloadDialog());

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            refreshData();
        });

        recyclerView = view.findViewById(R.id.activeAllUser_RecyclerView);
        progressDialog = new ProgressDialog(requireActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");

        // Create the notification channel
        createNotificationChannel();
        getAllActiveUser();
        return view;
    }

    private void createNotificationChannel() {
        CharSequence name = "Download Notification Channel";
        String description = "Channel for download notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription(description);
        }
        NotificationManager notificationManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notificationManager = requireActivity().getSystemService(NotificationManager.class);
        }
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void refreshData() {
        getAllActiveUser();
    }

    private void getAllActiveUser() {
        progressDialog.show();
        swipeRefreshLayout.setRefreshing(false);

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();
        Call<List<AllActiveUsers>> call = apiInterface.getAllActiveUser(token);
        call.enqueue(new Callback<List<AllActiveUsers>>() {
            @Override
            public void onResponse(@NonNull Call<List<AllActiveUsers>> call, @NonNull Response<List<AllActiveUsers>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    allActiveUsers = response.body();
                    if (allActiveUsers != null && !allActiveUsers.isEmpty()) {
                        // Calculate the count of active users
                        int activeUserCount = allActiveUsers.size();
                        // Now you have the count of active users
                        Log.d("Active User Count", "Count: " + activeUserCount);
                        setAllActiveUserAdapter(allActiveUsers);
                    } else {
                        Toast.makeText(getActivity(), "No users found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorBody = response.errorBody().toString();
                    Log.e("AllActiveUserFragment", "API Error: " + errorBody);
                    Toast.makeText(getActivity(), "Failed to get user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AllActiveUsers>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e("AllActiveUserFragment", "Network error: " + t.getMessage());
                Toast.makeText(getActivity(), "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDownloadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Download Option")
                .setItems(new CharSequence[]{"Excel", "PDF"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Excel
                                downloadExcel();
                                break;
                            case 1: // PDF
                                downloadPdf();
                                break;
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void downloadExcel() {
        if (isDownloading) {
            Toast.makeText(getActivity(), "Download is already in progress...", Toast.LENGTH_SHORT).show();
            return;
        }

        isDownloading = true;
        // Make an api call for Download in Excel
        Call<Void> call = apiInterface.downloadExcel();
        String title = "Excel Download";
        final String[] message = {"Downloading Excel..."};

        showNotification(title, message[0]);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                isDownloading = false;

                if (response.isSuccessful()) {
                    // Generate a unique file name based on timestamp
                    String timeStamp = String.valueOf(System.currentTimeMillis());
                    currentFileName = "ActiveUser_" + timeStamp + ".xls";
                    // Handle successful response, such as initiating the download
                    String excelUrl = response.raw().request().url().toString();
                    startDownload(excelUrl, currentFileName); // Use the unique file name
                    message[0] = "Excel download completed!";
                } else {
                    // Handle API error here
                    message[0] = "Failed to download Excel.";
                }

                updateNotificationMessage(title, message[0], currentFileName);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                isDownloading = false;
                // Handle network failure here
                message[0] = "Network Error. Please try again.";
                updateNotificationMessage(title, message[0], currentFileName);
            }
        });
    }

    private void downloadPdf() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, PERMISSION_STORAGE_CODE);
        } else {
            initiatePdfDownload();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiatePdfDownload();
            } else {
                Toast.makeText(getActivity(), "Permission denied. Cannot download PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initiatePdfDownload() {
        if (isDownloading) {
            Toast.makeText(getActivity(), "Download is already in progress...", Toast.LENGTH_SHORT).show();
            return;
        }

        isDownloading = true;
        // Make an api call for download in PDF
        Call<Void> call = apiInterface.downloadPdf();
        String title = "PDF Download";
        final String[] message = {"Downloading PDF..."};

        showNotification(title, message[0]);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                isDownloading = false;

                if (response.isSuccessful()) {
                    // Generate a unique file name based on timestamp
                    String timeStamp = String.valueOf(System.currentTimeMillis());
                    currentFileName = "ActiveUser_" + timeStamp + ".pdf";
                    // Handle successful response, such as initiating the download
                    String pdfUrl = response.raw().request().url().toString();
                    startDownload(pdfUrl, currentFileName); // Use the unique file name
                    message[0] = "PDF download completed!";
                } else {
                    // Handle API error here
                    message[0] = "Failed to download PDF.";
                }

                updateNotificationMessage(title, message[0], currentFileName);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                isDownloading = false;
                // Handle network failure here
                message[0] = "Network Error. Please try again.";
                updateNotificationMessage(title, message[0], currentFileName);
            }
        });
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
        } else {
            // Request the notification permission
            requestNotificationPermission();
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



    private void requestNotificationPermission() {
        // You can customize the notification permission request dialog here
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Notification Permission Required")
                .setMessage("Please grant notification permission to receive updates.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Redirect the user to app settings to manually grant notification permission
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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

    public void setAllActiveUserAdapter(List<AllActiveUsers> allActiveUsers) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        AllActiveUserAdapter adapter = new AllActiveUserAdapter(allActiveUsers, getActivity());
        recyclerView.setAdapter(adapter);
    }
}
