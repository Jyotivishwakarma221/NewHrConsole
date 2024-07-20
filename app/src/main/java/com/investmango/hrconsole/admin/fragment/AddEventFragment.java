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
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.investmango.hrconsole.ImagePreview.AddTaskDialogFragment;
import com.investmango.hrconsole.ImagePreview.ImageDeleteListener;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.activity.HomeActivity;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.cloudinary.CloudinaryConfig;
import com.investmango.hrconsole.model.AddEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEventFragment extends Fragment implements ImageDeleteListener,  AddTaskDialogFragment.UploadTaskListener {
    private ApiInterface apiInterface;
    private String token;
    private EditText descriptionEditText;
    private EditText subjectEditText;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button datePickerButton;
    private Button timePickerButton;
    private String imageUrl;
    private Uri imageUri;
    private ImageView imageView;

    private TextView showImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);

        SharedPreferences preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        // Initialize Cloudinary
        CloudinaryConfig.initCloudinary(requireContext());
        showImage = view.findViewById(R.id.showImages);
        descriptionEditText = view.findViewById(R.id.edit_description);
        subjectEditText = view.findViewById(R.id.edit_subject);
        imageView = view.findViewById(R.id.edit_poster);
        datePickerButton = view.findViewById(R.id.date_picker_button);
        timePickerButton = view.findViewById(R.id.time_picker_button);
        Button submitButton = view.findViewById(R.id.submit_button);

        imageView.setOnClickListener(v ->openGallery());
        datePickerButton.setOnClickListener(v -> openDatePicker());
        timePickerButton.setOnClickListener(v -> openTimePicker());

        submitButton.setOnClickListener(v -> {
            // Update the poster variable with the imageUrl
            String description = descriptionEditText.getText().toString().trim();
            String subject = subjectEditText.getText().toString().trim();
            long eventDateTime = getSelectedDateTimeInMillis();
            String poster = imageUrl;
            // Check if any of the fields are empty
            if (description.isEmpty() || subject.isEmpty() || poster == null || poster.isEmpty() || eventDateTime == 0) {
                Toast.makeText(requireContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("AddEventFragment", "Description: " + description);
            Log.d("AddEventFragment", "Event Date Time: " + eventDateTime);
            Log.d("AddEventFragment", "Poster URL: " + imageUrl);
            Log.d("AddEventFragment", "Subject: " + subject);

            AddEvent addEvent = new AddEvent(description, eventDateTime, poster, subject);

            Call<AddEvent> call = apiInterface.saveNewAnnouncement(token, addEvent);
            call.enqueue(new Callback<AddEvent>() {
                @Override
                public void onResponse(@NonNull Call<AddEvent> call, @NonNull Response<AddEvent> response) {
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(getContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(requireContext(), "Event added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        handleErrorResponse(response);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<AddEvent> call, @NonNull Throwable t) {
                    Toast.makeText(requireContext(), "Network error. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
            });
        });


        return view;
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
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
        if (Objects.equals(uri.getScheme(), "content")) {
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

    private void showImagePreviewDialog(Uri imageUri) {
        AddTaskDialogFragment dialogFragment = new AddTaskDialogFragment(imageUri, this);
        dialogFragment.setUploadTaskListener(() -> uploadImageToCloud(imageUri));
        dialogFragment.show(getChildFragmentManager(), "ImagePreviewDialogFragment");
    }

    private void uploadImageToCloud(Uri imageUri) {
        long fileSize = getFileSize(imageUri);
        if (fileSize > 300 * 1024) {
            Toast.makeText(requireContext(), "File size exceeds 300 KB limit. Please upload a file smaller than 300 KB", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = ProgressDialog.show(requireContext(), "", "Uploading image...", true);
        String folderName = "announcement";
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

    private long getFileSize(Uri uri) {
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        assert cursor != null;
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        long fileSize = cursor.getLong(sizeIndex);
        cursor.close();
        return fileSize;
    }
    private long getSelectedDateTimeInMillis() {
        String dateStr = datePickerButton.getText().toString();
        String timeStr = timePickerButton.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            String dateTimeStr = dateStr + " " + timeStr;
            Date date = sdf.parse(dateTimeStr);

            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void openDatePicker() {
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
            datePickerButton.setText(selectedDate);
        });
        picker.show(requireFragmentManager(), picker.toString());
    }

    private void openTimePicker() {
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder();
        builder.setTitleText("Select Time");
        builder.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);
        MaterialTimePicker picker = builder.build();
        picker.addOnPositiveButtonClickListener(dialog -> {
            int hourOfDay = picker.getHour();
            int minute = picker.getMinute();
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            timePickerButton.setText(selectedTime);
        });
        picker.show(requireFragmentManager(), picker.toString());
    }

    private void handleErrorResponse(Response<?> response) {

    }

    @Override
    public void onUploadTask() {
        uploadImageToCloud(imageUri);
    }

    @Override
    public void onDeleteImage() {
        imageView.setImageURI(null);
        showImage.setText("");
        Toast.makeText(requireContext(), "Image deleted", Toast.LENGTH_SHORT).show();
    }
}
