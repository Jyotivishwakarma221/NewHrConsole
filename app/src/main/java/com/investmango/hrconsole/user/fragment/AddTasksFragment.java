package com.investmango.hrconsole.user.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.investmango.hrconsole.ImagePreview.AddTaskDialogFragment;
import com.investmango.hrconsole.ImagePreview.ImageDeleteListener;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.cloudinary.CloudinaryConfig;
import com.investmango.hrconsole.model.AddTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTasksFragment extends Fragment implements ImageDeleteListener, AddTaskDialogFragment.UploadTaskListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private ImageView imageView;
    private EditText taskEditText;
    private Uri imageUri;
    private String imageUrl;
    private TextView showImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        CloudinaryConfig.initCloudinary(requireContext());

        SharedPreferences preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        showImage = view.findViewById(R.id.showImage);

        imageView = view.findViewById(R.id.selectImage);
        taskEditText = view.findViewById(R.id.taskEditText);
        Button addButton = view.findViewById(R.id.addButton);

        imageView.setOnClickListener(v -> openGallery());
        addButton.setOnClickListener(v -> uploadTask());

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);

        return view;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onDeleteImage() {

        imageUri = null;
        imageView.setImageURI(null);
        showImage.setText("");
        Toast.makeText(requireContext(), "Image deleted", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            long fileSize = getFileSize(imageUri);
            if (fileSize > 300 * 1024) { // 300 KB in bytes
                Toast.makeText(requireContext(), "Image size exceeds 300 KB. Please select a smaller image.", Toast.LENGTH_SHORT).show();
                return;
            }
            showImagePreviewDialog(imageUri);
            // Set the image name in the TextView
            String imageName = getFileName(imageUri);
            showImage.setText(imageName);
        }
    }

    private long getFileSize(Uri uri) {
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        assert cursor != null;
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        long fileSize = cursor.getLong(sizeIndex);
        cursor.close();
        return fileSize;
    }


    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void showImagePreviewDialog(Uri imageUri) {
        AddTaskDialogFragment dialogFragment = new AddTaskDialogFragment(imageUri, this);
        dialogFragment.setUploadTaskListener(() -> {
            uploadImageToCloud(imageUri);
        });
        dialogFragment.show(getChildFragmentManager(), "ImagePreviewDialogFragment");
    }

    private void uploadImageToCloud(Uri imageUri) {
        long fileSize = getFileSize(imageUri);
        if (fileSize > 300 * 1024) {
            Toast.makeText(requireContext(), "File size exceeds 300 KB limit. Please upload a file smaller than 300 KB", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = ProgressDialog.show(requireContext(), "", "Uploading image...", true);
        String folderName = "taskfiles";
        String publicId = folderName + "/" + System.currentTimeMillis();
        // Configure the Cloudinary upload options
        MediaManager.get().upload(imageUri)
                .option("public_id", publicId) // Specify the folder name
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        progressDialog.dismiss();
                        imageUrl = (String) resultData.get("url");
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        // Dismiss progress dialog when upload is successful
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }


    private void uploadTask() {
        String task = taskEditText.getText().toString().trim();
        if (task.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter task.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            sendTaskWithoutImage(task);
        } else {
            sendTaskWithImage(imageUrl, task);
        }
    }

    private void sendTaskWithImage(String imageUrl, String task) {
        ApiClient apiClient = new ApiClient(requireContext());
        apiInterface = apiClient.getApiInterface();
        AddTask taskObj = new AddTask();
        taskObj.setSubject(task);
//        taskObj.setFileUrl(imageUrl);

        Call<AddTask> call = apiInterface.addTask(taskObj, userId);
        call.enqueue(new Callback<AddTask>() {
            @Override
            public void onResponse(@NonNull Call<AddTask> call, @NonNull Response<AddTask> response) {
                if (response.isSuccessful()) {
                  // Task and file added successfully, now redirect to TaskFragment
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.remove, new TaskFragment())
                            .addToBackStack(null)
                            .commit();
                    Toast.makeText(requireContext(), "Task and file added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    handleApiCallFailure(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddTask> call, @NonNull Throwable t) {
                handleApiCallFailure(null);
            }
        });
    }

    private void sendTaskWithoutImage(String task) {
        ApiClient apiClient = new ApiClient(requireContext());
        apiInterface = apiClient.getApiInterface();
        AddTask taskObj = new AddTask();
        taskObj.setSubject(task);

        Call<AddTask> call = apiInterface.addTask(taskObj, userId);
        call.enqueue(new Callback<AddTask>() {
            @Override
            public void onResponse(@NonNull Call<AddTask> call, @NonNull Response<AddTask> response) {
                if (response.isSuccessful()) {
                    // Task added successfully, now redirect to TaskFragment
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.remove, new TaskFragment())
                            .addToBackStack(null)
                            .commit();
                    Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    handleApiCallFailure(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddTask> call, @NonNull Throwable t) {
                handleApiCallFailure(null);
            }
        });
    }

    private void handleApiCallFailure(@Nullable Response<AddTask> response) {
        if (response != null && response.errorBody() != null) {
            try {
                String errorBodyString = response.errorBody().string();
                JSONObject errorJson = new JSONObject(errorBodyString);
                String errorMessage = errorJson.getString("message");
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                Log.e("API Response", errorMessage);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(requireContext(), "Failed to add task. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUploadTask() {
        uploadImageToCloud(imageUri);
    }
}
