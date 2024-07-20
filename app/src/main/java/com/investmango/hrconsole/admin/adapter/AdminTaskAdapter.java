package com.investmango.hrconsole.admin.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.text.TextUtils;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.AddTask;
import com.investmango.hrconsole.model.AdminTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AdminTaskAdapter extends RecyclerView.Adapter<AdminTaskAdapter.AdminTaskViewHolder> {
    private List<AdminTask> adminTaskList;
    private final Context context;

    private ApiInterface apiInterface;
    private SharedPreferences preferences;
    private String token;


    public AdminTaskAdapter(List<AdminTask> adminTaskList, Context context) {
        this.adminTaskList = adminTaskList;
        this.context = context;
        ApiClient apiClient = new ApiClient(context);
        apiInterface = apiClient.getApiInterface();

        preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");

    }

    @NonNull
    @Override
    public AdminTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.admin_task, parent, false);
        return new AdminTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminTaskViewHolder holder, int position) {
        AdminTask adminTask = adminTaskList.get(position);

        long timestamp = adminTask.getCreatedTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date date = new Date(timestamp);
        String formattedTime = timeFormat.format(date);

        String userName = adminTask.getUserName();
        if (!TextUtils.isEmpty(userName)) {
            userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
        }

        holder.timeTextView.setText(formattedTime);
        holder.nameTextView.setText(userName);
        holder.taskTextView.setText(adminTask.getSubject());
        holder.commentTextView.setText(adminTask.getComments());

        long taskId = adminTask.getId();
        System.out.println("id: " + taskId);

        long userId = adminTask.getUserId();
        System.out.println("User Id: " + userId);


        holder.addCommentImageView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            showDialog(context, taskId, userId);
        });

        String status = adminTask.getStatus();
        if (status.equalsIgnoreCase("REVIEWED")) {
            status = "Reviewed";
        } else if (status.equalsIgnoreCase("PENDING")) {
            status = "Pending";
        } else if (status.equalsIgnoreCase("DONE")) {
            status = "Done";
        } else if (status.equalsIgnoreCase("DUE")) {
            status = "Due";
        } else if (status.equalsIgnoreCase("ASSIGNED")) {
            status = "Assigned";
        }
        holder.statusTextView.setText(status);

        if (adminTask.getFileUrl() != null && !adminTask.getFileUrl().isEmpty()) {
            holder.fileTextView.setVisibility(View.VISIBLE);
            holder.fileTextView.setOnClickListener(v -> showDialogWithImage(context, adminTask.getFileUrl()));
        } else {
            holder.fileTextView.setVisibility(View.GONE);
        }
    }

    private void showDialog(Context context, long taskId, long userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Comment");
        builder.setMessage("Please enter your comment.");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String comment = input.getText().toString();
            if (!TextUtils.isEmpty(comment)) {
                addComment(taskId, comment, userId);
            } else {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(ContextCompat.getColor(context, R.color.Button));
            negativeButton.setTextColor(ContextCompat.getColor(context, R.color.Button));
        });

        dialog.show();
    }

    private void addComment(long taskId, String comment, long userId) {
        AddTask taskObj = new AddTask();
        taskObj.setId(taskId);
        taskObj.setComments(comment);
        Call<AddTask> call = apiInterface.addComment(token, taskObj, userId);
        call.enqueue(new Callback<AddTask>() {
            @Override
            public void onResponse(@NonNull Call<AddTask> call, @NonNull Response<AddTask> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Comment added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AddTask> call, @NonNull Throwable t) {
                Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show();
            }
        });
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
        return adminTaskList.size();
    }

    public class AdminTaskViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView nameTextView;
        TextView taskTextView;
        TextView commentTextView;
        TextView statusTextView;
        ImageView fileTextView;
        ImageView addCommentImageView;

        public AdminTaskViewHolder(@NonNull View itemView) {
            super(itemView);

            timeTextView = itemView.findViewById(R.id.time);
            nameTextView = itemView.findViewById(R.id.Name_Text_View);
            taskTextView = itemView.findViewById(R.id.task_Text_View);
            commentTextView = itemView.findViewById(R.id.comment_TextView);
            statusTextView = itemView.findViewById(R.id.status_TextView);
            fileTextView = itemView.findViewById(R.id.fileTextView);
            addCommentImageView = itemView.findViewById(R.id.addCommentImageView);

            taskTextView.setOnClickListener(v -> showTaskPopup(taskTextView.getContext(), taskTextView.getText().toString()));
        }
    }
    private AlertDialog dialog;
    private void showTaskPopup(Context context, String taskText) {
        if (context == null) {
            Log.e("LeaveRequestAdapter", "Context is null");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.reason_popup_dialog, null);
        if (view == null) {
            Log.e("LeaveRequestAdapter", "Failed to inflate layout");
            return;
        }
        builder.setView(view);

        TextView popupReasonTextView = view.findViewById(R.id.popup_reason);
        popupReasonTextView.setText(taskText);

        ImageView closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> {
            // Dismiss the dialog when the close button is clicked
            if (dialog != null) {
                dialog.dismiss();
            } else {
                Log.e("AdminTaskAdapter", "Dialog is null");
            }
        });

        dialog = builder.create();
        if (dialog != null) {
            dialog.show();
        } else {
            Log.e("AdminTaskAdapter", "Failed to create dialog");
        }
    }
    }


