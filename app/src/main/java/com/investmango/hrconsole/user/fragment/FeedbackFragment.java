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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.investmango.hrconsole.ImagePreview.AddTaskDialogFragment;
import com.investmango.hrconsole.ImagePreview.ImageDeleteListener;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.cloudinary.CloudinaryConfig;
import com.investmango.hrconsole.model.FeedbackRequest;
import com.investmango.hrconsole.user.activity.UserHomeActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackFragment extends Fragment implements ImageDeleteListener, AddTaskDialogFragment.UploadTaskListener{
    private static final int PICK_IMAGE_REQUEST = 1;
    private long userId;
    private String token;
    private SharedPreferences preferences;
    private Spinner spinnerSection;
    private EditText feedbackEditText;
    private Button saveButton;
    private ApiInterface apiInterface;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView imageView;
    private String imageUrl;
    private TextView showImage;
    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        CloudinaryConfig.initCloudinary(requireContext());

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        showImage = view.findViewById(R.id.showImages);
        imageView = view.findViewById(R.id.selectImages);

        imageView.setOnClickListener(v -> openGallery());

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        saveButton = view.findViewById(R.id.feedbackSave);
        feedbackEditText = view.findViewById(R.id.feedback);
        spinnerSection = view.findViewById(R.id.spinner_section);

        setupSpinner();

        saveButton.setOnClickListener(v -> uploadTask());
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

    private void showImagePreviewDialog(Uri imageUri) {
        AddTaskDialogFragment dialogFragment = new AddTaskDialogFragment(imageUri, this);
        dialogFragment.setUploadTaskListener(() -> {
            uploadImageToCloud(imageUri);
        });
        dialogFragment.show(getChildFragmentManager(), "ImagePreviewDialogFragment");
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

    private void uploadImageToCloud(Uri imageUri) {
        long fileSize = getFileSize(imageUri);
        if (fileSize > 300 * 1024) {
            Toast.makeText(requireContext(), "File size exceeds 300 KB limit. Please upload a file smaller than 300 KB", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = ProgressDialog.show(requireContext(), "", "Uploading image...", true);
        String folderName = "feedback";
        String publicId = folderName + "/" + System.currentTimeMillis();

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



    private void setupSpinner() {
        List<String> sections = new ArrayList<>();
        sections.add("Select");
        sections.add("Task");
        sections.add("Attendance");
        sections.add("Salary");
        sections.add("Upload Document");
        sections.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.color_spinner_layout, sections);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

        spinnerSection.setAdapter(adapter);
    }

    private void uploadTask() {
        saveFeedback(imageUrl);
    }

    private void saveFeedback(String imageUrl) {
        String feedbackText = feedbackEditText.getText().toString();
        String selectedSection = spinnerSection.getSelectedItem().toString();

        if (!feedbackText.isEmpty() && !selectedSection.equals("Select")) {
            FeedbackRequest feedbackRequest = new FeedbackRequest();
            feedbackRequest.setFeedback(feedbackText);
            feedbackRequest.setSection(selectedSection);
            feedbackRequest.setUrl(imageUrl);
            Call<FeedbackRequest> call = apiInterface.saveNewFeedbacks(userId, feedbackRequest);
            call.enqueue(new Callback<FeedbackRequest>() {
                @Override
                public void onResponse(@NonNull Call<FeedbackRequest> call, @NonNull Response<FeedbackRequest> response) {
                    if (response.isSuccessful()) {
                        navigateToHome();
                        Toast.makeText(requireContext(), "Feedback saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to save feedback", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<FeedbackRequest> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), "Failed to save feedback", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "Please provide feedback and select a section", Toast.LENGTH_LONG).show();

        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(getActivity(), UserHomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onUploadTask() {
        uploadImageToCloud(imageUri);
    }

}
