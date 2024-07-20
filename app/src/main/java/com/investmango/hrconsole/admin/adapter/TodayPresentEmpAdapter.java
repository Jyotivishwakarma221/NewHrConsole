package com.investmango.hrconsole.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.model.PresentEmployee;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class TodayPresentEmpAdapter extends RecyclerView.Adapter<TodayPresentEmpAdapter.TodayPresentViewHolder> {
    private List<PresentEmployee> todayPresentUserList;
    private final Context context;

    public TodayPresentEmpAdapter(List<PresentEmployee> todayPresentUserList, Context context) {
        this.todayPresentUserList = todayPresentUserList;
        this.context = context;
    }

    @NonNull
    @Override
    public TodayPresentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.today_present_employee, parent, false);
        return new TodayPresentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayPresentViewHolder holder, int position) {
        PresentEmployee user = todayPresentUserList.get(position);

        String designation = toTitleCase(user.getDesignation());

        holder.designation.setText(designation);

        String imageUrl = user.getProfileImage(); // Replace this with the actual image URL
        Glide.with(context)
                .load(imageUrl)
                .into(holder.activeImage);

        // Capitalize the first letter of the username
        String userName = capitalizeFirstLetter(user.getUserName());
        holder.presentEmpName.setText(userName);
    }

    // Method to capitalize the first letter of a string
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    @Override
    public int getItemCount() {
        return todayPresentUserList.size();
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String[] words = input.split(" ");
        StringBuilder titleCase = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                if (titleCase.length() > 0) {
                    titleCase.append(" ");
                }
                titleCase.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
        }

        return titleCase.toString();
    }

    public static class TodayPresentViewHolder extends RecyclerView.ViewHolder {
        TextView presentEmpName;
        TextView designation;
        RoundedImageView activeImage;

        public TodayPresentViewHolder(@NonNull View itemView) {
            super(itemView);
            presentEmpName = itemView.findViewById(R.id.presentEmpName);
            activeImage = itemView.findViewById(R.id.activeImage);
            designation = itemView.findViewById(R.id.designation);
        }
    }
}
