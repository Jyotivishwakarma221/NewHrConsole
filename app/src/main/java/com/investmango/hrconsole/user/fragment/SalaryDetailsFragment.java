package com.investmango.hrconsole.user.fragment;

import android.Manifest;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.Salary;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class SalaryDetailsFragment extends Fragment {
    private TextView basicSalaryTextView;
    private TextView hraTextView;
    private TextView medicalFundTextView;
    private TextView bonusTextView;
    private TextView convienceTextView;
    private TextView totalSalaryTextView;
    private TextView monthlySalaryTextView;
    private ImageView all_total_salary;
    private ApiInterface apiInterface;
    private ProgressDialog progressDialog;
    private long userId;
    private String token;
    private static final String CHANNEL_ID = "download_notification_channel";
    private String currentFileName = "";
    ImageView downloadSalary;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salary_details, container, false);
        downloadSalary = view.findViewById(R.id.downloadSalary);
        downloadSalary.setOnClickListener(v -> {
            // Call method to download the PDF when the user is clicked.
            downloadUserSalaryPdf(userId);
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshSalaryDetails);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            refreshSalaryDetails();
        });

        all_total_salary = view.findViewById(R.id.all_total_salary);

        all_total_salary.setOnClickListener(v -> {
            AllTotalSalaryFragment fragment =  new  AllTotalSalaryFragment();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(fragment, "calculation_fragment");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss();
        });
        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        // Find the TextView elements by their IDs
        basicSalaryTextView = view.findViewById(R.id.basicSalaryTextView);
        hraTextView = view.findViewById(R.id.hraTextView);
        medicalFundTextView = view.findViewById(R.id.medicalFundTextView);
        bonusTextView = view.findViewById(R.id.bonusTextView);
        convienceTextView = view.findViewById(R.id.convienceTextView);
        totalSalaryTextView = view.findViewById(R.id.totalSalaryTextView);
        monthlySalaryTextView = view.findViewById(R.id.monthlySalaryTextView);

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        SharedPreferences preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        // Call the method to get the user's salary details
        getUserSalaryDetails(userId);
        return view;
    }
    private void refreshSalaryDetails() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        getUserSalaryDetails(userId);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getUserSalaryDetails(long userId) {
        progressDialog.show();
        Call<List<Salary>> call = apiInterface.userSalary( userId);
        call.enqueue(new Callback<List<Salary>>() {
            @Override
            public void onResponse(@NonNull Call<List<Salary>> call, @NonNull Response<List<Salary>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    List<Salary> salaryList = response.body();
                    if (salaryList != null && !salaryList.isEmpty()) {
                        Salary salary = salaryList.get(0);
                        basicSalaryTextView.setText(String.valueOf(salary.getBasicSalary()));
                        hraTextView.setText(String.valueOf(salary.getHra()));
                        medicalFundTextView.setText(String.valueOf(salary.getMedicalFund()));
                        bonusTextView.setText(String.valueOf(salary.getBonus()));
                        convienceTextView.setText(String.valueOf(salary.getConvience()));
                        // Display total salary and monthly salary
                        totalSalaryTextView.setText(String.valueOf(salary.getTotalSalary()));
                        double monthlySalaryValue = salary.getMonthlySalary();
                        // Create a DecimalFormat object with the desired format
                        DecimalFormat decimalFormat = new DecimalFormat("#.00");
                        // Format the double value to have two decimal places
                        String formattedMonthlySalary = decimalFormat.format(monthlySalaryValue);
                        monthlySalaryTextView.setText(formattedMonthlySalary);

                    } else {
                        Log.e("SalaryDetailsFragment", "Salary data is null or empty");
                    }
                } else {
                    Log.e("SalaryDetailsFragment", "Failed to fetch salary details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Salary>> call, @NonNull Throwable t) {
                // Handle API call failure (e.g., network error)
                Log.e("SalaryDetailsFragment", "Server error", t);
            }
        });
    }

    private void downloadUserSalaryPdf(long userId) {
        progressDialog.show();
        Call<Void> call = apiInterface.getUserSalaryPdf( userId);
        String title = "PDF Download";
        final String[] message = {"Downloading PDF..."};

        showNotification(title, message[0]);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    String timeStamp = String.valueOf(System.currentTimeMillis());
                    currentFileName = "UserSalary_" + timeStamp + ".pdf";
                    String pdfUrl = response.raw().request().url().toString();
                    startDownload(pdfUrl, currentFileName); // Use the unique file name

                    message[0] = "PDF download completed!";
                } else {
                    Log.e("SalaryDetailsFragment", "Failed to download salary PDF");
                    message[0] = "Failed to download PDF.";
                }
                updateNotificationMessage(title, message[0],currentFileName);
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e("SalaryDetailsFragment", "Server error while downloading PDF", t);
                message[0] = "Network Error. Please try again.";
                updateNotificationMessage(title, message[0],currentFileName);
            }
        });
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
}
