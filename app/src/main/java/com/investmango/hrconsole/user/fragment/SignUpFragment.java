package com.investmango.hrconsole.user.fragment;

import android.annotation.SuppressLint;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.cloudinary.CloudinaryConfig;
import com.investmango.hrconsole.model.Role;
import com.investmango.hrconsole.model.SignUp;
import com.investmango.hrconsole.service.LoginActivity;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpFragment extends Fragment {

    private EditText editTextFirstName, editTextLastName, editTextUserName, editTextPassword,
            editTextEmail, editTextPhone, editTextCurrentAddress, editTextPermanentAddress;

    private TextView textViewDob;
    private ImageView imageViewCalendar;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button buttonSignUp, chooseFile;

    private static final int PICK_IMAGE_REQUEST = 1;

    private String token;
    private SharedPreferences preferences;
    private ApiInterface apiInterface;
    private Context context;
    private Uri imageUri;
    private String imageUrl;
    private AutoCompleteTextView departmentDropdown, designationDropdown;

    private final String[] departments = {"Management", "Human Resource", "Finance", "Sales", "Digital Marketing", "IT & Development"};
    private final String[][] departmentDesignations = {
            {"General Manager", "Administrative Officer", "Chief Executive Officer (CEO)", "Strategic Lead", "Chief Operating Officer (COO)", "Chief Financial Officer (CFO)"},
            {"HR Executive", "HR Manager", "Recruiter"},
            {"Accountant", "Finance Manager", "Account Executive"},
            {"Sales Executive","Sales Manager", "Senior Sales Manager", "P&L Manager", "General Manager", "Customer Relationship Manager (CRM)", "Real Estate Aspirant", "Customer Care Representative", "Portfolio Manager", "Sales Head"},
            {"Digital Marketing Manager", "SEO Executive", "Search Engine Optimization (SEO)", "Intern", "Campaign Manager", "Content Writer", "Social Media Analyst", "Marketing Specialist", "Marketing Analyst", "Public Relation Manager", "Brand Manager", "Graphic Designer"},
            {"Software Development Engineer", "Software Web Developer", "Junior Web Developer", "Full Stack Developer", "Senior Web Designer", "Junior Web Designer","Graphic Designer", "Backend Developer", "Frontend Developer", "Senior App Developer/Android Developer", "Junior App Developer/Android Developer"}
    };
    private final List<String> departmentList = new ArrayList<>();
    private final List<String> designationList = new ArrayList<>();
    private ArrayAdapter<String> departmentAdapter;
    private ArrayAdapter<String> designationAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        departmentAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, departmentList);
        designationAdapter = new ArrayAdapter<>(requireContext(), R.layout.dropdown_menu_popup_item, designationList);

        CloudinaryConfig.initCloudinary(requireContext());

        context = getContext();
        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        editTextFirstName = rootView.findViewById(R.id.editTextFirstName);
        editTextLastName = rootView.findViewById(R.id.editTextLastName);
        editTextUserName = rootView.findViewById(R.id.editTextUserName);
        editTextPassword = rootView.findViewById(R.id.editTextPassword);
        editTextEmail = rootView.findViewById(R.id.editTextEmail);
        editTextPhone = rootView.findViewById(R.id.editTextPhone);
        editTextCurrentAddress = rootView.findViewById(R.id.editTextCurrentAddress);
        editTextPermanentAddress = rootView.findViewById(R.id.editTextPermanentAddress);
        textViewDob = rootView.findViewById(R.id.showDob);
        imageViewCalendar = rootView.findViewById(R.id.calenders);
        radioButtonMale = rootView.findViewById(R.id.radioButtonMale);
        radioButtonFemale = rootView.findViewById(R.id.radioButtonFemale);
        buttonSignUp = rootView.findViewById(R.id.buttonSignUp);
        chooseFile = rootView.findViewById(R.id.chooseFile);

        departmentDropdown = rootView.findViewById(R.id.department_dropdown);
        designationDropdown = rootView.findViewById(R.id.designation_dropdown);

        setupDepartmentDropdown();

        chooseFile.setOnClickListener(view -> openImagePicker());

        imageViewCalendar.setOnClickListener(v -> {
            MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker().build();
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String selectedDate = dateFormat.format(new Date(selection));
                textViewDob.setText(selectedDate);
            });

            // Show the material calendar dialog.
            materialDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        buttonSignUp.setOnClickListener(v -> {
            // Retrieve text from EditText fields
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String userName = editTextUserName.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            String currentAddress = editTextCurrentAddress.getText().toString().trim();
            String permanentAddress = editTextPermanentAddress.getText().toString().trim();
            String designation = designationDropdown.getText().toString().trim();
            String department = departmentDropdown.getText().toString().trim();

            // Get selected date from textViewDob
            String dob = textViewDob.getText().toString().trim();

            // Determine the selected gender
            String gender = radioButtonMale.isChecked() ? "Male" : "Female";

            // Check if any of the fields are empty or null
            if (TextUtils.isEmpty(firstName)) {
                Toast.makeText(context, "Please enter your first name", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(lastName)) {
                Toast.makeText(context, "Please enter your last name", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(dob)) {
                Toast.makeText(context, "Please select your date of birth", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(userName)) {
                Toast.makeText(context, "Please choose a username", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(context, "Please enter a password", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(email)) {
                Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(phone)) {
                Toast.makeText(context, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(currentAddress)) {
                Toast.makeText(context, "Please enter your current address", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(permanentAddress)) {
                Toast.makeText(context, "Please enter your permanent address", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(designation)) {
                Toast.makeText(context, "Please enter your designation", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(department)) {
                Toast.makeText(context, "Please enter your department", Toast.LENGTH_SHORT).show();
            } else if (userName.equals(phone)) {
                Toast.makeText(context, "Username and phone number cannot be the same.", Toast.LENGTH_LONG).show();
            } else {
                // Proceed with sign up process
                SignUp requestBody = new SignUp();
                requestBody.setFirstName(firstName);
                requestBody.setLastName(lastName);
                requestBody.setUserName(userName);
                requestBody.setPassword(password);
                requestBody.setEmail(email);
                requestBody.setPhone(phone);
                requestBody.setCurrentAddress(currentAddress);
                requestBody.setPermanentAddress(permanentAddress);
                requestBody.setDesignation(designation);
                requestBody.setDepartment(department);
                requestBody.setDob(dob);
                requestBody.setGender(gender);

                requestBody.setProfileImage(imageUrl);

                List<Role> roles = new ArrayList<>();

                Role role = new Role();
                role.setRoleName("User");

                // Add the role to the list
                roles.add(role);

                // Set the list of roles in the requestBody
                requestBody.setRoles(roles);

                Call<SignUp> call = apiInterface.signUp(token, requestBody);
                call.enqueue(new Callback<SignUp>() {
                    @Override
                    public void onResponse(@NonNull Call<SignUp> call, @NonNull Response<SignUp> response) {
                        if (response.isSuccessful()) {
                            // Navigate to LoginActivity on successful sign up
                            Intent intent = new Intent(getContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            Toast.makeText(context, "Signed up successfully!", Toast.LENGTH_LONG).show();
                        } else {
                            String errorMessage = "Failed to sign up.";
                            if (response.errorBody() != null) {
                                try {
                                    JSONObject errorObject = new JSONObject(response.errorBody().string());
                                    errorMessage = errorObject.optString("message", "Unknown error");
                                } catch (Exception e) { // Catching Exception here to handle both IOException and JSONException
                                    e.printStackTrace();
                                }
                            }
                            Log.e("API Response", errorMessage);
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SignUp> call, @NonNull Throwable t) {
                        Log.e("SignUp Error", Objects.requireNonNull(t.getMessage()));
                        Toast.makeText(context, "Network error, please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return rootView;
    }

    private void setupDepartmentDropdown() {
        departmentList.clear();
        departmentList.addAll(Arrays.asList(departments));

        departmentDropdown.setAdapter(departmentAdapter);
        departmentDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDepartment = (String) parent.getItemAtPosition(position);
            setupDesignationDropdown(selectedDepartment);
        });
    }

    private void setupDesignationDropdown(String department) {
        designationList.clear();
        int index = getDepartmentIndex(department);
        if (index >= 0 && index < departmentDesignations.length) {
            designationList.addAll(Arrays.asList(departmentDesignations[index]));
        }

        designationDropdown.setAdapter(designationAdapter);
    }

    private int getDepartmentIndex(String department) {
        for (int i = 0; i < departments.length; i++) {
            if (departments[i].equals(department)) {
                return i;
            }
        }
        return -1;
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
            if (imageUri != null && checkFileSize(imageUri)) {
                // Get the file name from the URI
                String fileName = getFileName(imageUri);
                // Set the file name to the button text
                chooseFile.setText(fileName);
                // Upload the image to Cloudinary
                uploadImage();
            } else {
                Toast.makeText(getContext(), "Image selection failed", Toast.LENGTH_SHORT).show();
                imageUri = null;
            }
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
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

    private boolean checkFileSize(Uri imageUri) {
        Cursor cursor = requireActivity().getContentResolver().query(imageUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            long fileSize = cursor.getLong(sizeIndex);
            cursor.close();
            if (fileSize > 300 * 1024) {
                Toast.makeText(getContext(), "Image size should be less than 300KB", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    private void uploadImage() {
        if (imageUri != null) {
            ProgressDialog progressDialog = ProgressDialog.show(requireContext(), "", "Uploading image...", true);
            String folderName = "profile";
            String publicId = folderName + "/" + System.currentTimeMillis();
            // Perform the upload to Cloudinary
            MediaManager.get().upload(imageUri)
                    .option("public_id", publicId)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            // Handle start of upload
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Handle upload progress
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            // Handle successful upload
                            imageUrl = (String) resultData.get("url");
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            // Handle upload error
                            Toast.makeText(requireContext(), "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            // Reschedule if needed
                        }
                    }).dispatch();
        } else {
            Toast.makeText(getContext(), "Please select an image to upload.", Toast.LENGTH_SHORT).show();
        }
    }
}
