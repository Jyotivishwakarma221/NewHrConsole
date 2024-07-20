package com.investmango.hrconsole.admin.adapter;

import android.app.Dialog;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.model.PreviousTask;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PreviousTaskAdapter extends RecyclerView.Adapter<PreviousTaskAdapter.PreviousTaskViewHolder> {
    private final List<PreviousTask> previousTaskList;
    private final Context context;

    public PreviousTaskAdapter(List<PreviousTask> previousTaskList, Context context) {
        this.previousTaskList = previousTaskList;
        this.context = context;
    }
    @NonNull
    @Override
    public PreviousTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.previous_task, parent, false);
        return new PreviousTaskViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull PreviousTaskViewHolder holder, int position) {

        PreviousTask previousTask = previousTaskList.get(position);
        holder.dateTextView.setText(convertTimestampToDateTime(previousTask.getCreatedTime()));
        String userName = previousTask.getUserName();
        if (!TextUtils.isEmpty(userName)) {
            userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
        }
        holder.naaamTexView.setText(userName);
        holder.taskTextView.setText(previousTask.getSubject());
        holder.commentTextView.setText(previousTask.getComments());
        String status = previousTask.getStatus();
        if (status.equalsIgnoreCase("REVIEWED")) {
            status = "Reviewed";
        }
        else if (status.equalsIgnoreCase("PENDING")) {
            status = "Pending";
        }
        else if (status.equalsIgnoreCase("DONE")) {
            status = "Done";
        }
        else if (status.equalsIgnoreCase("DUE")) {
            status = "Due";
        }
        else if (status.equalsIgnoreCase("ASSIGNED")) {
            status = "Assigned";
        }
        holder.statusTextView.setText(status);

        if (previousTask.getFileUrl() != null && !previousTask.getFileUrl().isEmpty()) {
            holder.fileTextView.setVisibility(View.VISIBLE);
            holder.fileTextView.setOnClickListener(v -> showDialogWithImage(context, previousTask.getFileUrl()));
        } else {
            holder.fileTextView.setVisibility(View.GONE);
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

    // Helper method to convert timestamp to a date and time
    private String convertTimestampToDateTime(long timestamp) {
        SimpleDateFormat dateFormat = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm a", Locale.getDefault());
        }
        Date date = new Date(timestamp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return dateFormat.format(date);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return previousTaskList.size();
    }

    public static class PreviousTaskViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView naaamTexView;
        TextView taskTextView;
        TextView commentTextView;
        TextView statusTextView;
        ImageView fileTextView;
        public PreviousTaskViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.Date);
            naaamTexView = itemView.findViewById(R.id.NAME);
            taskTextView = itemView.findViewById(R.id.taskText_View);
            commentTextView = itemView.findViewById(R.id.comment_Text_View);
            statusTextView = itemView.findViewById(R.id.status_Text_View);
            fileTextView = itemView.findViewById(R.id.fileTextView);
        }
    }
}
