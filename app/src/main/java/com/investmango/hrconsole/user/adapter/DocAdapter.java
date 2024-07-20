package com.investmango.hrconsole.user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.investmango.hrconsole.R;

import java.util.List;

public class DocAdapter extends RecyclerView.Adapter<DocAdapter.ViewHolder> {
    private final List<String> data;
    private final OnItemClickListener onItemClickListener;

    public DocAdapter(List<String> data, OnItemClickListener onItemClickListener) {
        this.data = data;
        this.onItemClickListener = onItemClickListener;
    }

    public void setData(List<String> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doc, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = data.get(position);

        // Extracting publicId
        String[] parts = imageUrl.split("/");
        String publicIdWithExtension = parts[parts.length - 1];
        String[] publicIdParts = publicIdWithExtension.split("\\.");
        String publicId = publicIdParts[0];

        // Printing publicId to log
        System.out.println("PublicId: " + publicId);

        Glide.with(holder.imageView.getContext())
                .load(imageUrl)
                .into(holder.imageView);

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
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String imageUrl);
    }
}
