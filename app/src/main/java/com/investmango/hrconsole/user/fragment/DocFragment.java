package com.investmango.hrconsole.user.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.DocsModel;
import com.investmango.hrconsole.user.activity.UserHomeActivity;
import com.investmango.hrconsole.user.adapter.DocAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocFragment extends Fragment implements DocAdapter.OnItemClickListener {
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private SharedPreferences preferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private DocAdapter docAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doc, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        docAdapter = new DocAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(docAdapter);

        ApiClient apiClient = new ApiClient(requireContext());
        apiInterface = apiClient.getApiInterface();

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getDocs();
            Toast.makeText(requireContext(), "Refreshing", Toast.LENGTH_SHORT).show();
        });

        preferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        getDocs();
        return view;
    }

    @Override
    public void onItemClick(String imageUrl) {
        // Handle item click, e.g., show image popup
        showImagePopup(imageUrl);
    }

    private void showImagePopup(String imageUrl) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.image_popup_layout, null);
        PhotoView photoView = view.findViewById(R.id.popupPhotoView);

        // Load and display the image using Glide
        Glide.with(requireContext())
                .load(imageUrl)
                .into(photoView);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> progressDialog.dismiss());
        dialog.show();
    }

    private void getDocs() {
        swipeRefreshLayout.setRefreshing(false);
        Call<DocsModel> call = apiInterface.getDocs(token, userId);
        call.enqueue(new Callback<DocsModel>() {
            @Override
            public void onResponse(@NonNull Call<DocsModel> call, @NonNull Response<DocsModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DocsModel docsModel = response.body();
                    List<String> documents = docsModel.getDocuments();
                    if (documents != null && !documents.isEmpty()) {
                        docAdapter.setData(documents);
                    }
                } else {
                    try {
                        assert response.errorBody() != null;
                        JSONObject errorObject = new JSONObject(response.errorBody().string());
                        String errorMessage = errorObject.getString("message");
                        showToast(errorMessage);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DocsModel> call, @NonNull Throwable t) {
            }
        });
    }

    private void showToast(String message) {
        // Show the error message as a Toast
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToUserHomeActivity();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                navigateToUserHomeActivity();
                return true;
            }
            return false;
        });
    }

    private void   navigateToUserHomeActivity() {
        Intent intent = new Intent(getActivity(), UserHomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

}
