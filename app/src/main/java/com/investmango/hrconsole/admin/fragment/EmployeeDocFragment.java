package com.investmango.hrconsole.admin.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.adapter.EmployeeDocAdapter;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.AllActiveUsers;
import com.investmango.hrconsole.model.DocsModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeDocFragment extends Fragment implements EmployeeDocAdapter.OnItemClickListener {

    private ApiInterface apiInterface;
    private String token;
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private EmployeeDocAdapter employeeDocAdapter;
    private AutoCompleteTextView spinnerEmployee;
    private List<AllActiveUsers> allActiveUsers;
    private Button verifyDocumentButton;
    private ImageView verifyDocumentImage;
    private long selectedUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_employee_doc, container, false);

        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        employeeDocAdapter = new EmployeeDocAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(employeeDocAdapter);

        spinnerEmployee = view.findViewById(R.id.spinnerEmployee);

        ApiClient apiClient = new ApiClient(requireContext());
        apiInterface = apiClient.getApiInterface();

        preferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");

        verifyDocumentButton = view.findViewById(R.id.verifyDocumentButton);
        verifyDocumentImage = view.findViewById(R.id.verifyDocumentImage);
        verifyDocumentButton.setOnClickListener(v -> verifyDocument());

        getAllActiveUser();

        spinnerEmployee.setOnItemClickListener((parent, view1, position, id) -> {
            selectedUserId = allActiveUsers.get(position).getId();
            updateUIOnSpinnerSelection();
            // Clear adapter data when a new user is selected
            employeeDocAdapter.clearData();
            getDocs();
        });


        return view;
    }

    private void verifyDocument() {
        if (selectedUserId == 0) {
            Toast.makeText(requireContext(), "Please select a user first.", Toast.LENGTH_SHORT).show();
            return;
        }
        Call<ResponseBody> call = apiInterface.verifyEmpDocument(selectedUserId, true);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    verifyDocumentButton.setVisibility(View.GONE);
                    verifyDocumentImage.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Document is verified successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            String errorMessage = errorObject.getString("message");
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("API Failure", "Failed to verify document. Error: " + t.getMessage());
            }
        });
    }


    private void updateUIOnSpinnerSelection() {
        if (verifyDocumentImage.getVisibility() == View.VISIBLE) {
            verifyDocumentImage.setVisibility(View.GONE);
            verifyDocumentButton.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onItemClick(String imageUrl) {
        showImagePopup(imageUrl);
    }

    private void showImagePopup(String imageUrl) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.image_popup_layout, null);
        PhotoView photoView = view.findViewById(R.id.popupPhotoView);

        Glide.with(requireContext())
                .load(imageUrl)
                .into(photoView);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> progressDialog.dismiss());
        dialog.show();
    }

    private void getAllActiveUser() {
        Call<List<AllActiveUsers>> call = apiInterface.getAllActiveUser();
        call.enqueue(new Callback<List<AllActiveUsers>>() {
            @Override
            public void onResponse(@NonNull Call<List<AllActiveUsers>> call, @NonNull Response<List<AllActiveUsers>> response) {
                if (response.isSuccessful()) {
                    allActiveUsers = response.body();
                    if (allActiveUsers != null && !allActiveUsers.isEmpty()) {
                        List<String> userNames = new ArrayList<>();
                        for (AllActiveUsers user : allActiveUsers) {
                            String fullName = user.getFirstName() + " " + user.getLastName();
                            userNames.add(fullName);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, userNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerEmployee.setAdapter(adapter);
                    } else {
                        Toast.makeText(getActivity(), "No users found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AllActiveUsers>> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void getDocs() {
//        Call<DocsModel> call = apiInterface.getDocs(token, selectedUserId);
//        call.enqueue(new Callback<DocsModel>() {
//                         @Override
//                         public void onResponse(@NonNull Call<DocsModel> call, @NonNull Response<DocsModel> response) {
//                             if (response.isSuccessful()) {
//                                 if (response.body() != null) {
//                                     DocsModel docsModel = response.body();
//                                     List<String> documents = docsModel.getDocuments();
//                                     if (documents != null && !documents.isEmpty()) {
//                                       employeeDocAdapter.setData(documents);
//                                     } else {
//                                         Toast.makeText(requireContext(), "No documents available.", Toast.LENGTH_SHORT).show();
//                                     }
//                                 } else {
//                                     Toast.makeText(requireContext(), "Response body is null.", Toast.LENGTH_SHORT).show();
//                                 }
//                             } else {
//                                 try {
//                                     if (response.errorBody() != null) {
//                                         JSONObject errorObject = new JSONObject(response.errorBody().string());
//                                         String errorMessage = errorObject.optString("message", "Unknown error");
//                                         Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
//                                     } else {
//                                         Toast.makeText(requireContext(), "Error response body is null.", Toast.LENGTH_SHORT).show();
//                                     }
//                                 } catch (IOException | JSONException e) {
//                                     e.printStackTrace();
//                                     Toast.makeText(requireContext(), "Error reading response.", Toast.LENGTH_SHORT).show();
//                                 }
//                             }
//                         }
//
//                         @Override
//                         public void onFailure(@NonNull Call<DocsModel> call, @NonNull Throwable t) {
//                             Log.e("DocFragment", "Network error: " + t.getMessage());
//                             Toast.makeText(requireContext(), "No document found for the selected user.", Toast.LENGTH_SHORT).show();
//                         }
//                     }
//        );
//    }

    private void getDocs() {
        Call<DocsModel> call = apiInterface.getDocs( selectedUserId);
        call.enqueue(new Callback<DocsModel>() {
            @Override
            public void onResponse(@NonNull Call<DocsModel> call, @NonNull Response<DocsModel> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        DocsModel docsModel = response.body();
                        List<String> documents = docsModel.getDocuments();
                        boolean documentVerified = docsModel.isDocumentVerified();

                        // Update UI based on document verification status
                        if (documentVerified) {
                            verifyDocumentImage.setVisibility(View.VISIBLE);
                            Toast.makeText(requireContext(), "Document is already verified.", Toast.LENGTH_SHORT).show();
                            verifyDocumentButton.setVisibility(View.GONE);
                        } else {
                            verifyDocumentImage.setVisibility(View.GONE);
                            verifyDocumentButton.setVisibility(View.VISIBLE);
                        }

                        if (documents != null && !documents.isEmpty()) {
                            employeeDocAdapter.setData(documents);
                        } else {
                            Toast.makeText(requireContext(), "No documents available.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Response body is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObject = new JSONObject(response.errorBody().string());
                            String errorMessage = errorObject.optString("message", "Unknown error");
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Error response body is null.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error reading response.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DocsModel> call, @NonNull Throwable t) {
                Log.e("DocFragment", "Network error: " + t.getMessage());
                Toast.makeText(requireContext(), "No document found for the selected user.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
