package com.investmango.hrconsole.user.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.cloudinary.CloudinaryConfig;
import com.investmango.hrconsole.model.DocumentModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadDocFragment extends Fragment {
    private Button uploadButton;
    private ImageView aadharCardImageView, panCardImageView, matriculationImageView, intermediateImageView, graduationImageView, postGraduationImageView, experienceImageView, lastCompanyImageView, appointmentLetterImageView, salarySlipImageView;
    private ApiInterface apiInterface;
    private SharedPreferences preferences;
    private long userId;
    private String token;
    private ImageView selectedImageView;
    private Map<String, Uri> documentTypeUriMap = new HashMap<>();

    // Keep track of the number of successful uploads
    private int successfulUploadCount = 0;
    List<String> cloudinaryUrls = new ArrayList<>();
    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<Intent> pickImageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        // Update the selectedImageUri based on the document type
                        Uri selectedImageUri = data.getData();
                        if (selectedImageView != null) {
                            selectedImageView.setImageURI(selectedImageUri);
                        }
                        // Store the document type and corresponding Uri in the map
                        documentTypeUriMap.put(getDocumentType(selectedImageView), selectedImageUri);
                    }
                }
            });

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
    }


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_upload_doc, container, false);
        // Initialize Cloudinary
        CloudinaryConfig.initCloudinary(requireContext());
        progressDialog = new ProgressDialog(requireContext());


        aadharCardImageView = rootView.findViewById(R.id.aadharCardImageView);
        panCardImageView = rootView.findViewById(R.id.PanCardImageView);
        matriculationImageView = rootView.findViewById(R.id.matriculationImageView);
        intermediateImageView = rootView.findViewById(R.id.IntermediateImageView);
        graduationImageView = rootView.findViewById(R.id.graduationImageView);
        postGraduationImageView = rootView.findViewById(R.id.postGraduationImageView);
        experienceImageView = rootView.findViewById(R.id.experienceImageView);
        lastCompanyImageView = rootView.findViewById(R.id.lastCompanyImageView);
        appointmentLetterImageView = rootView.findViewById(R.id.appointmentLetterImageView);
        salarySlipImageView = rootView.findViewById(R.id.salarySlipImageView);

        Button aadharImageButton = rootView.findViewById(R.id.aadharCard);
        Button panImageButton = rootView.findViewById(R.id.PanCard);
        Button matriculationButton = rootView.findViewById(R.id.matriculation);
        Button intermediateButton = rootView.findViewById(R.id.Intermediate);
        Button graduationButton = rootView.findViewById(R.id.graduation);
        Button postGraduationButton = rootView.findViewById(R.id.postGraduation);
        Button experienceButton = rootView.findViewById(R.id.experience);
        Button lastCompanyOfferLetter = rootView.findViewById(R.id.lastCompany);
        Button appointmentLetter = rootView.findViewById(R.id.appointmentLetter);
        Button salarySlip = rootView.findViewById(R.id.salarySlip);

        uploadButton = rootView.findViewById(R.id.submit_Button);

        aadharImageButton.setOnClickListener(v -> pickImage(aadharCardImageView));
        panImageButton.setOnClickListener(v -> pickImage(panCardImageView));
        matriculationButton.setOnClickListener(v -> pickImage(matriculationImageView));
        intermediateButton.setOnClickListener(v -> pickImage(intermediateImageView));
        graduationButton.setOnClickListener(v -> pickImage(graduationImageView));
        postGraduationButton.setOnClickListener(v -> pickImage(postGraduationImageView));
        experienceButton.setOnClickListener(v -> pickImage(experienceImageView));
        lastCompanyOfferLetter.setOnClickListener(v -> pickImage(lastCompanyImageView));
        appointmentLetter.setOnClickListener(v -> pickImage(appointmentLetterImageView));
        salarySlip.setOnClickListener(v -> pickImage(salarySlipImageView));

        uploadButton.setOnClickListener(v -> {
            // Reset successful upload count before starting new uploads
            successfulUploadCount = 0;

            // Validate that images for Aadhar, Pan, Matriculation, Intermediate, and Graduation are selected
            if (documentTypeUriMap.containsKey("aadhar") &&
                    documentTypeUriMap.containsKey("pan") &&
                    documentTypeUriMap.containsKey("matriculation") &&
                    documentTypeUriMap.containsKey("intermediate") &&
                    documentTypeUriMap.containsKey("graduation")) {

                // Upload each document separately
                uploadAndSaveDocument("aadhar");
                uploadAndSaveDocument("pan");
                uploadAndSaveDocument("matriculation");
                uploadAndSaveDocument("intermediate");
                uploadAndSaveDocument("graduation");
                uploadAndSaveDocument("postGraduation");
                uploadAndSaveDocument("experience");
                uploadAndSaveDocument("lastCompany");
                uploadAndSaveDocument("appointmentLetter");
                uploadAndSaveDocument("salarySlip");
            } else {
                // Show an error message indicating missing documents
                Toast.makeText(requireContext(), "Aadhar, Pan, Matriculation, Intermediate and Graduation mark sheets are required.", Toast.LENGTH_LONG).show();
            }
        });
        return rootView;
    }

    private void pickImage(ImageView imageView) {
        selectedImageView = imageView;
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageActivityResultLauncher.launch(pickImageIntent);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            // Image selected successfully
            if (data != null && data.getData() != null) {
                // Update the selectedImageUri based on the document type
                Uri selectedImageUri = data.getData();
                // Check image size before proceeding
                checkImageSize(selectedImageUri);
                if (selectedImageView != null) {
                    selectedImageView.setImageURI(selectedImageUri);
                }
                // Store the document type and corresponding Uri in the map
                documentTypeUriMap.put(getDocumentType(selectedImageView), selectedImageUri);
            }
        }
    }

    private String getDocumentType(ImageView imageView) {
        if (imageView == aadharCardImageView) {
            return "aadhar";
        } else if (imageView == panCardImageView) {
            return "pan";
        } else if (imageView == matriculationImageView) {
            return "matriculation";
        } else if (imageView == intermediateImageView) {
            return "intermediate";
        } else if (imageView == graduationImageView) {
            return "graduation";
        } else if (imageView == postGraduationImageView) {
            return "postGraduation";
        } else if (imageView == experienceImageView) {
            return "experience";
        } else if (imageView == lastCompanyImageView) {
            return "lastCompany";
        } else if (imageView == appointmentLetterImageView) {
            return "appointmentLetter";
        } else if (imageView == salarySlipImageView) {
            return "salarySlip";
        } else {
            // Handle unrecognized ImageView
            return "";
        }
    }

private void uploadAndSaveDocument(String documentType) {
    Uri documentUri = documentTypeUriMap.get(documentType);
    if (documentUri != null) {
        // Check image size before proceeding with upload
        if (checkImageSize(documentUri)) {
            File selectedImageFile = uriToFile(documentUri);

            if (selectedImageFile != null) {
                String folderName = "document";
                // Construct the public ID by combining folder name and a unique identifier
                String publicId = folderName + "/" + System.currentTimeMillis();
                MediaManager.get().upload(selectedImageFile.getAbsolutePath())
                        .option("public_id", publicId)
                        .callback(new UploadCallback() {
                                    @Override
                                    public void onStart(String requestId) {
                                        // Show progress dialog when upload starts
                                        progressDialog.setMessage("Please wait Uploading...");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                    }

                                    @Override
                                    public void onProgress(String requestId, long bytes, long totalBytes) {
                                        // Calculate upload progress percentage
                                        int progress = (int) ((bytes * 100) / totalBytes);
                                        progressDialog.setMessage("Uploading... " + progress + "%");
                                    }

                                    @Override
                                    public void onSuccess(String requestId, Map resultData) {
                                        progressDialog.dismiss();
                                        successfulUploadCount++;
                                        if (resultData.containsKey("secure_url")) {
                                            String cloudinaryUrl = (String) resultData.get("secure_url");
                                            cloudinaryUrls.add(cloudinaryUrl);
                                            if (successfulUploadCount == documentTypeUriMap.size()) {
                                                saveUserDoc(cloudinaryUrls);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(String requestId, ErrorInfo error) {
                                        // Handle upload error
                                    }

                                    @Override
                                    public void onReschedule(String requestId, ErrorInfo error) {
                                        // Handle upload reschedule
                                    }
                                })
                        .dispatch();
            }
        } else {
            Toast.makeText(requireContext(), "File size exceeds 300 KB limit. Please upload a file smaller than 300 KB", Toast.LENGTH_SHORT).show();
        }
    }
}

    private boolean checkImageSize(Uri selectedImageUri) {
        try {
            // Open an input stream from the selected image URI
            InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);

            // Get the length of the input stream (image size)
            int size = inputStream.available();
            inputStream.close();

            // Convert size to kilobytes
            int sizeInKB = size / 1024;

            // Check if size is greater than 300KB
            return sizeInKB <= 300;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private File uriToFile(Uri uri) {
        try {
            File file;
            if ("content".equals(uri.getScheme())) {
                Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    String filePath = cursor.getString(idx);
                    cursor.close();
                    file = new File(filePath);
                } else {
                    return null;
                }
            } else {
                file = new File(uri.getPath());
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveUserDoc(List<String> cloudinaryUrls) {
        DocumentModel documentModel = new DocumentModel(cloudinaryUrls);

        Call<DocumentModel> call = apiInterface.saveDocByUserId(token, documentModel, userId);
        call.enqueue(new Callback<DocumentModel>() {
            @Override
            public void onResponse(Call<DocumentModel> call, Response<DocumentModel> response) {
                if (response.isSuccessful()) {
                    DocumentModel responseDocumentModel = response.body();
                    progressDialog.dismiss();
                    // Redirect to DocFragment
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.removeUploadDoc, new DocFragment())
                            .addToBackStack(null)
                            .commit();
                    Toast.makeText(requireContext(), "Document uploaded successfully!", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        String errorMessage = response.errorBody().string();
                        Log.e("SaveDocumentError", "API call not successful. Error Message: " + errorMessage);
                        // Extract the message from the JSON response
                        JSONObject errorJson = new JSONObject(errorMessage);
                        String message = errorJson.optString("message");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Log.e("SaveDocumentError", "Error parsing error response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<DocumentModel> call, Throwable t) {
                // Handle API call failure
                String failureMessage = "API call failed: " + t.getMessage();
                Log.e("SaveDocumentError", failureMessage);
                Toast.makeText(requireContext(), "Failed to upload document. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
