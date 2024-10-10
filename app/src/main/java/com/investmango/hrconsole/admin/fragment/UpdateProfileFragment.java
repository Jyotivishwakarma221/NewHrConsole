package com.investmango.hrconsole.admin.fragment;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.cloudinary.CloudinaryConfig;
import com.investmango.hrconsole.model.User;
import com.investmango.hrconsole.user.activity.UserHomeActivity;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfileFragment extends Fragment {
    private Button uploadButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences preferences;
    private long userId;
    private String token;
    private Uri imageUri;
    private ImageView doc1ImageView;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ApiInterface apiInterface;
    ImageView doc1;
    private String imageUrl;
    private String apiKey = "974981595445112";
    private String apiSecret = "4URnjaut9IehzWDZZ8_AVH8pKoQ";

    private String publicId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        getCurrentUser(token);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_update_profile, container, false);
        doc1ImageView = rootView.findViewById(R.id.image_View);
        doc1 = rootView.findViewById(R.id.select_Image);
        doc1.setOnClickListener(v -> openImagePicker());

        CloudinaryConfig.initCloudinary(requireContext());

        uploadButton = rootView.findViewById(R.id.submit);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);

        uploadButton.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToCloud(imageUri);
            } else {
                Toast.makeText(getContext(), "Please select an image to upload.", Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    private void uploadImageToCloud(Uri imageUri) {
        // Show progress dialog
        ProgressDialog progressDialog = ProgressDialog.show(requireContext(), "", "Uploading image...", true);
        String folderName = "profile";
        String publicId1 = folderName + "/" + System.currentTimeMillis();
        MediaManager.get().upload(imageUri)
                .option("public_id", publicId1)
                .callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {}

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {}

            @Override
            public void onSuccess(String requestId, Map resultData) {
                imageUrl = (String) resultData.get("url");
                uploadFile(userId, imageUrl);
                // Delete the old image from Cloudinary in a background thread
                new Thread(() -> deleteImageFromCloudinary(publicId)).start();
                // Dismiss progress dialog when upload is successful
                progressDialog.dismiss();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Toast.makeText(requireContext(), "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                // Dismiss progress dialog on error
                progressDialog.dismiss();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {}
        }).dispatch();
    }


    private void openImagePicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            String fileName = getFileNameFromUri(imageUri);

            // Check image size before uploading
            if (getImageSize(imageUri) <= 300 * 1024) { // 300kb in bytes
                Toast.makeText(getContext(), "Selected Image: " + fileName, Toast.LENGTH_SHORT).show();
                Glide.with(this)
                        .load(imageUri)
                        .into(doc1ImageView);
            } else {
                Toast.makeText(getContext(), "Image size exceeds 300kb limit", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to get the size of the selected image
    private long getImageSize(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        long size = 0;
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (sizeIndex != -1) {
                size = cursor.getLong(sizeIndex);
            }
            cursor.close();
        }
        return size;
    }


    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (columnIndex != -1) {
                fileName = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return fileName;
    }

    private void getCurrentUser(String token) {
        Call<User> call = apiInterface.getCurrentUser(token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        String userImage = user.getProfileImage();
                        Log.d("profile", "Image----" + userImage);

                        // Extracting public_id from the Cloudinary URL
                        String hello = extractPublicId(userImage);
                        String prefixedPublicId = "profile/" + hello;

                        publicId =prefixedPublicId;

                        Log.d("profile", "Public ID: " + publicId);


                    }
                } else {
                    Log.e("ProfileFragment", "Failed to retrieve current user. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("ProfileFragment", "Error retrieving current user: " + t.getMessage());
            }
        });
    }
    private void deleteImageFromCloudinary(String publicId) {
        // Initialize Cloudinary with your cloud name, API key, and API secret
        Cloudinary cloudinary = new Cloudinary("cloudinary://" + apiKey + ":" + apiSecret + "@dzvsmmraz");

        // Delete the image from Cloudinary
        try {
            cloudinary.uploader().destroy(publicId, null);
            Log.d("Cloudinary", "Image deleted successfully from Cloudinary-----" + publicId);
        } catch (IOException e) {
            Log.e("Cloudinary", "Error deleting image from Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicId(String imageUrl) {
        // Extracting public_id from Cloudinary URL
        int startIndex = imageUrl.lastIndexOf("/") + 1;
        int endIndex = imageUrl.lastIndexOf(".");
        return imageUrl.substring(startIndex, endIndex);
    }

    private void uploadFile(Long userId, String imageUrl) {
        // Pass the imageUrl directly instead of a RequestBody
        Call<ResponseBody> call = apiInterface.uploadFile( userId, imageUrl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(getContext(), UserHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    // If you're in an Activity and want to clear the current activity stack
                    // finish();
                    Toast.makeText(getContext(), "Profile uploaded successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Profile upload failed", Toast.LENGTH_SHORT).show();
                    Log.e("Upload Failure", "Error Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Server error " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Failure", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

}
