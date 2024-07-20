package com.investmango.hrconsole.user.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.Task;
import com.investmango.hrconsole.model.UpdateTaskStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.LeadsViewHolder> {
    private final List<Task> tasks;
    private final Context context;
    private final ApiInterface apiInterface;
    private final SharedPreferences preferences;
    private final String token;
    private final long userId;

    public TaskAdapter(List<Task> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
        ApiClient apiClient = new ApiClient(context);
        apiInterface = apiClient.getApiInterface();

        preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
    }

    @NonNull
    @Override
    public LeadsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list, parent, false);
        return new LeadsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeadsViewHolder holder, int position) {
        Task currentTask = tasks.get(position);
        setupStatusSpinner(holder, currentTask.getId(), holder.getAdapterPosition(),formatStatus(currentTask.getStatus()));

        holder.dateTextView.setText(formatDate(currentTask.getCreatedTime()));
        holder.nameTextView.setText(capitalizeFirstLetter(currentTask.getUserName()));
        holder.UserId.setText(String.valueOf(currentTask.getId()));
        holder.taskTextView.setText(currentTask.getSubject());
        holder.commentTextView.setText(currentTask.getComments());

        if (currentTask.getFileUrl() != null && !currentTask.getFileUrl().isEmpty()) {
            holder.fileTextView.setVisibility(View.VISIBLE);
            holder.fileTextView.setOnClickListener(v -> showDialogWithImage(context, currentTask.getFileUrl()));
        } else {
            holder.fileTextView.setVisibility(View.GONE);
        }

    }

    private void setupStatusSpinner(@NonNull LeadsViewHolder holder, long taskId, int adapterPosition, String status) {
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add(formatStatus(status));
        statusOptions.add("Select");
        statusOptions.add("Done");
        statusOptions.add("Pending");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.color_spinner_layout, statusOptions);
        arrayAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        holder.spinner.setAdapter(arrayAdapter);

        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                if (i > 1) {
                    UpdateTaskStatus.Status selectedStatus;
                    switch (i) {
                        case 2:
                            selectedStatus = UpdateTaskStatus.Status.DONE;
                            break;
                        case 3:
                            selectedStatus = UpdateTaskStatus.Status.PENDING;
                            break;
                        default:
                            selectedStatus = null;
                            break;
                    }

                    UpdateTaskStatus requestBody = new UpdateTaskStatus();
                    requestBody.setId(taskId);
                    requestBody.setStatus(selectedStatus);

                    Call<ResponseBody> call = apiInterface.updateUserTaskStatus(requestBody, userId);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "Status updated successfully!", Toast.LENGTH_LONG).show();
                            } else {
                                handleErrorResponse(response);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            Log.e("TaskAdapter", "Error updating task status", t);
                            Toast.makeText(context, "Failed to update task status. Please try again later.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }



    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String capitalizeFirstLetter(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String formatStatus(String status) {
        switch (status.toLowerCase()) {
            case "due":
                return "Due";
            case "reviewed":
                return "Reviewed";
            case "pending":
                return "Pending";
            case "assigned":
                return "Assigned";
            case "done":
                return "Done";
            default:
                return status;
        }
    }

    private void handleErrorResponse(Response<ResponseBody> response) {
        try {
            String errorResponse = response.errorBody().string();
            JSONObject errorObject = new JSONObject(errorResponse);
            String errorMessage = errorObject.optString("message", "Unknown Error");
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Unknown Error", Toast.LENGTH_LONG).show();
        }
    }

    private void showDialogWithImage(Context context, String imageUrl) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_image_preview);

        ImageView imageView = dialog.findViewById(R.id.imageView);
        Glide.with(context)
                .load(imageUrl)
                .into(imageView);

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class LeadsViewHolder extends RecyclerView.ViewHolder {
        TextView UserId, dateTextView, nameTextView, taskTextView, commentTextView, statusTextView;
        Spinner spinner;
        ImageView fileTextView;

        public LeadsViewHolder(View itemView) {
            super(itemView);
            UserId = itemView.findViewById(R.id.UserId);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            taskTextView = itemView.findViewById(R.id.taskTextView);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            fileTextView = itemView.findViewById(R.id.fileTextView);
            spinner = itemView.findViewById(R.id.spinner);
        }
    }
}
