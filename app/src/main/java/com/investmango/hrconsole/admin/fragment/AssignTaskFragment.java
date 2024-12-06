package com.investmango.hrconsole.admin.fragment;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.investmango.hrconsole.model.AllActiveUsers;
import com.investmango.hrconsole.model.AssignTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignTaskFragment extends Fragment implements ImageDeleteListener,  AddTaskDialogFragment.UploadTaskListener{
    private ApiInterface apiInterface;
    private static final int PICK_IMAGE_REQUEST = 1;
    private String token;
    private long userId;
    private SharedPreferences preferences;
    private List<AllActiveUsers> allActiveUsers;
    private EditText taskEditText;
    private Spinner spinner_employee;
    private long selectedUserId;
    private Uri imageUri;
    private String imageUrl;
    private ImageView imageView;
    private TextView showImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assign_task, container, false);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        CloudinaryConfig.initCloudinary(requireContext());
        showImage = view.findViewById(R.id.showImages);
        taskEditText = view.findViewById(R.id.taskEditText);
        Button addButton = view.findViewById(R.id.addButton);
        spinner_employee = view.findViewById(R.id.spinner_employee);
        imageView = view.findViewById(R.id.imageSelection);
        spinner_employee = view.findViewById(R.id.spinner_employee);

        imageView.setOnClickListener(v -> openGallery());
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        getAllActiveUser();

        spinner_employee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Check if the selected position is valid
                if (position >= 1 && position < allActiveUsers.size()) {
                    // Access the item in the allActiveUsers list
                    position = position - 1;
                    selectedUserId = allActiveUsers.get(position).getId();
                    String firstName = allActiveUsers.get(position).getFirstName();
                    String lastName = allActiveUsers.get(position).getLastName();
                    String fullName = firstName + " " + lastName;
                    Log.d("SelectedUser", "user Id: " + selectedUserId + ", Name: " + fullName);
                } else {
                    // Handle the case where the selected position is invalid
                    Log.e("AssignTaskFragment", "Invalid selected position: " + position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addButton.setOnClickListener(v -> uploadTask());

        return view;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onDeleteImage() {
        // Implement delete image functionality here
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
            String imageName = getFileName(imageUri);
            showImage.setText(imageName);
        }
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
            assert result != null;
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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

    private void showImagePreviewDialog(Uri imageUri) {
        AddTaskDialogFragment dialogFragment = new AddTaskDialogFragment(imageUri, this);
        dialogFragment.setUploadTaskListener(() -> {
            uploadImageToCloud(imageUri);
        });
        dialogFragment.show(getChildFragmentManager(), "ImagePreviewDialogFragment");
    }


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
                        populateSpinner();
                    } else {
                        showToast("No users found.");
                    }
                } else {
                    showToast("Failed to get user details.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AllActiveUsers>> call, @NonNull Throwable t) {
                showToast("Server Error. Please try again.");
            }
        });
    }

    private void populateSpinner() {
        List<String> userNames = new ArrayList<>();
        userNames.add("Select Employee");
        for (AllActiveUsers user : allActiveUsers) {
            userNames.add(capitalizeEachWord(getFullName(user)));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.color_spinner_layout, userNames);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner_employee.setAdapter(adapter);
    }

    private String getFullName(AllActiveUsers user) {
        return user.getFirstName() + " " + user.getLastName();
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
        if (selectedUserId <= 0) {
            showToast("Please select a user.");
            return;
        }
        AssignTask taskObj = new AssignTask();
        taskObj.setSubject(task);
        taskObj.setUserId(selectedUserId);
//        taskObj.setFileurl(imageUrl);
        Call<AssignTask> call = apiInterface.assignTaskUser(selectedUserId, taskObj);
        call.enqueue(new Callback<AssignTask>() {
            @Override
            public void onResponse(@NonNull Call<AssignTask> call, @NonNull Response<AssignTask> response) {
                if (response.isSuccessful()) {
                    // Redirect to AdminTaskFragment
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.moving, new AdminTaskFragment())
                            .addToBackStack(null)
                            .commit();
                    showToast("Task assigned successfully.");
                } else {
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AssignTask> call, @NonNull Throwable t) {
                showToast("Network Error: " + t.getMessage());
            }
        });
    }

    private void sendTaskWithoutImage(String task) {
        if (selectedUserId <= 0) {
            showToast("Please select a user.");
            return;
        }
        AssignTask taskObj = new AssignTask();
        taskObj.setSubject(task);
        taskObj.setUserId(selectedUserId);
        Call<AssignTask> call = apiInterface.assignTaskUser(selectedUserId, taskObj);
        call.enqueue(new Callback<AssignTask>() {
            @Override
            public void onResponse(@NonNull Call<AssignTask> call, @NonNull Response<AssignTask> response) {
                if (response.isSuccessful()) {
                    // Redirect to AdminTaskFragment
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.moving, new AdminTaskFragment())
                            .addToBackStack(null)
                            .commit();
                    showToast("Task assigned successfully.");
                } else {
                    handleErrorResponse(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AssignTask> call, @NonNull Throwable t) {
                showToast("Network Error: " + t.getMessage());
            }
        });
    }
    private void handleErrorResponse(int code) {
        if (code == 500) {
            showToast("A task already exists for this date");
        } else {
            showToast("Failed to assign task. Error code: " + code);
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void uploadImageToCloud(Uri imageUri) {
        // Check if the image size exceeds 300 KB
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
                .option("public_id", publicId)
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

    @Override
    public void onUploadTask() {
        uploadImageToCloud(imageUri);
    }
}
