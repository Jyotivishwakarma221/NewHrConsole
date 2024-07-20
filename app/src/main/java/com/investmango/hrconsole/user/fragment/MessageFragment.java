package com.investmango.hrconsole.user.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.Message;
import com.investmango.hrconsole.user.adapter.MessageAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends Fragment {
    private long userId;
    private String token;
    private ApiInterface apiInterface;
    private MessageAdapter messageAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        SharedPreferences  preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        recyclerView = view.findViewById(R.id.listRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        messageAdapter = new MessageAdapter(getActivity(), null);
        recyclerView.setAdapter(messageAdapter);

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        fetchCustomMessages();

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchCustomMessages);

        return view;
    }

    private void fetchCustomMessages() {
        Call<List<Message>> call = apiInterface.getCustomMessage(token, userId);
        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(@NonNull Call<List<Message>> call, @NonNull Response<List<Message>> response) {
                if (response.isSuccessful()) {
                    List<Message> messages = response.body();
                    if (messages != null && !messages.isEmpty()) {
                        messageAdapter.setMessageList(messages);
                    } else {
                        showMessage("No messages available.");
                    }
                } else {
                    showMessage("Failed to fetch messages. Please try again later.");
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<Message>> call, @NonNull Throwable t) {
                showMessage("Failed to fetch messages. Please check your internet connection.");
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void showMessage(String message) {
        Log.e("Message Fragment", "Message" + message);
    }
}
