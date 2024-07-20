package com.investmango.hrconsole.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.model.Assignment;

import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {
    private List<Assignment> assignmentList;
    private final Context context;

    public AssignmentAdapter(List<Assignment> assignmentList, Context context) {
        this.assignmentList = assignmentList;
        this.context = context;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.assignment_adapter, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = assignmentList.get(position);

//        holder.date_Text.setText(assignment.getDate());
        holder.Name.setText(assignment.getFirstName() + " " + assignment.getLastName());
        holder.assignment_Text_View.setText(assignment.getTask());
        holder.description_TextView.setText(assignment.getDescription());
        holder.deadline_TextView.setText(assignment.getDeadline());
        holder.comment_Text.setText(assignment.getComment());
        holder.status_TextView.setText(assignment.getStatus());
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        // Declare your TextViews here using the IDs from your layout
        TextView date_Text;
        TextView Name;
        TextView assignment_Text_View;

        TextView description_TextView;
        TextView deadline_TextView;
        TextView comment_Text;
        TextView status_TextView;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize your TextViews using their IDs
            date_Text= itemView.findViewById(R.id.date_Text);
            Name = itemView.findViewById(R.id.Name);
            assignment_Text_View = itemView.findViewById(R.id.assignment_Text_View);
            description_TextView = itemView.findViewById(R.id.description_TextView);
            deadline_TextView = itemView.findViewById(R.id.deadline_TextView);
            comment_Text = itemView.findViewById(R.id.comment_Text);
            status_TextView = itemView.findViewById(R.id.status_TextView);
        }
    }
}
