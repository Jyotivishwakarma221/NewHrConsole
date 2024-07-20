package com.investmango.hrconsole.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.model.AllActiveUsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllActiveUserAdapter extends RecyclerView.Adapter<AllActiveUserAdapter.AllActiveUserViewHolder> {
    private List<AllActiveUsers> allActiveUsersList;
    private final Context context;

    public AllActiveUserAdapter(List<AllActiveUsers> allActiveUsersList, Context context) {
        this.allActiveUsersList = allActiveUsersList;
        this.context = context;
    }

    @NonNull
    @Override
    public AllActiveUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.all_active_users, parent, false);
        return new AllActiveUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllActiveUserAdapter.AllActiveUserViewHolder holder, int position) {
        AllActiveUsers user = allActiveUsersList.get(position);
        holder.Ids.setText(String.valueOf(user.getId()));

        holder.joiningDate_Text_View.setText(formatDate(user.getCreatedDate()));

        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        if (!firstName.isEmpty()) {
            firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
        }
        if (!lastName.isEmpty()) {
            lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
        }

        holder.firstName_Text_View.setText(firstName);
        holder.lastName_TextView.setText(lastName);

        // Format Date of Birth
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            Date dobDate = inputFormat.parse(user.getDob());
            String formattedDob = outputFormat.format(dobDate);
            holder.Date_Of_Birth_TextView.setText(formattedDob);
        } catch (ParseException e) {
            e.printStackTrace();
            holder.Date_Of_Birth_TextView.setText(user.getDob()); // If parsing fails, set original dob
        }


        holder.Alternative_TextView.setText(user.getPhone());
        holder.Phone_TextView.setText(user.getUserName());
        holder.currentAddress_TextView.setText(user.getCurrentAddress());
        holder.permanentAddress_TextView.setText(user.getPermanentAddress());

        String gender = user.getGender();
        if (gender != null && !gender.isEmpty()) {
            gender = gender.substring(0, 1).toUpperCase() + gender.substring(1).toLowerCase();
        }
        holder.gender_TextView.setText(gender);

        holder.email_TextView.setText(user.getEmail());
        holder.department_TextView.setText(user.getDepartment());
        holder.designation_TextView.setText(user.getDesignation());
    }

    @Override
    public int getItemCount() {
        return allActiveUsersList.size();
    }

    public static class AllActiveUserViewHolder extends RecyclerView.ViewHolder {
        // Declare the UI elements here
        TextView Ids;
        TextView joiningDate_Text_View;
        TextView firstName_Text_View;
        TextView lastName_TextView;
        TextView Date_Of_Birth_TextView;
        TextView Alternative_TextView;
        TextView Phone_TextView;
        TextView currentAddress_TextView;
        TextView permanentAddress_TextView;
        TextView gender_TextView;
        TextView email_TextView;
        TextView department_TextView;
        TextView designation_TextView;

        public AllActiveUserViewHolder(@NonNull View itemView) {
            super(itemView);

            Ids = itemView.findViewById(R.id.Ids);
            joiningDate_Text_View = itemView.findViewById(R.id.joiningDate_Text_View);
            firstName_Text_View = itemView.findViewById(R.id.firstName_Text_View);
            lastName_TextView = itemView.findViewById(R.id.lastName_TextView);
            Date_Of_Birth_TextView = itemView.findViewById(R.id.Date_Of_Birth_TextView);
            Alternative_TextView = itemView.findViewById(R.id.Alternative_TextView);
            Phone_TextView = itemView.findViewById(R.id.Phone_TextView);
            currentAddress_TextView = itemView.findViewById(R.id.currentAddress_TextView);
            permanentAddress_TextView = itemView.findViewById(R.id.permanentAddress_TextView);
            gender_TextView = itemView.findViewById(R.id.gender_TextView);
            email_TextView = itemView.findViewById(R.id.email_TextView);
            department_TextView = itemView.findViewById(R.id.department_TextView);
            designation_TextView = itemView.findViewById(R.id.designation_TextView);
        }
    }

    // Format timestamp as "dd-MMM-yyyy"
    private String formatDate(long timestamp) {
        if (timestamp > 0) {
            Date date = new Date(timestamp);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            return outputFormat.format(date);
        } else {
            return "Invalid Date";
        }
    }
}
