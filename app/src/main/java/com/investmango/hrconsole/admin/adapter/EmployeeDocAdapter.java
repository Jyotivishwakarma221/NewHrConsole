package com.investmango.hrconsole.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.investmango.hrconsole.R;

import java.util.List;

public class EmployeeDocAdapter extends RecyclerView.Adapter<EmployeeDocAdapter.ViewHolder> {
    private final List<String> data;
    private final OnItemClickListener onItemClickListener;

    public EmployeeDocAdapter(List<String> data, OnItemClickListener onItemClickListener) {
        this.data = data;
        this.onItemClickListener = onItemClickListener;
    }

    public void setData(List<String> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }
    public void clearData() {
        data.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emp_doc_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = data.get(position);
        Glide.with(holder.image.getContext())
                .load(imageUrl)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(imageUrl);
            }
        });
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image= itemView.findViewById(R.id.documents);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String imageUrl);
    }
}
