package com.investmango.hrconsole.ImagePreview;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.investmango.hrconsole.R;

public class AddTaskDialogFragment extends DialogFragment {
    private final Uri imageUri;
    private final ImageDeleteListener deleteListener;
    private UploadTaskListener uploadTaskListener; // Interface instance

    public AddTaskDialogFragment(Uri imageUri, ImageDeleteListener deleteListener) {
        this.imageUri = imageUri;
        this.deleteListener = deleteListener;
    }

    public interface UploadTaskListener {
        void onUploadTask();
    }

    public void setUploadTaskListener(UploadTaskListener listener) {
        this.uploadTaskListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_image_preview_dialog, null);

        ImageView imageView = dialogView.findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);

        LinearLayout layout = dialogView.findViewById(R.id.layout);
        Button cutImage = dialogView.findViewById(R.id.cutImage);

        cutImage.setOnClickListener(v -> {
            if (uploadTaskListener != null) {
                layout.setVisibility(View.GONE);
                dismiss();
                uploadTaskListener.onUploadTask();
            } else {
                Toast.makeText(requireContext(), "No listener attached", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setView(dialogView)
                .setPositiveButton("Delete", (dialogInterface, i) -> {
                    deleteListener.onDeleteImage();
                    dismiss();
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.setOnShowListener(dialog -> {
            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(getResources().getColor(R.color.Button));
        });

        builder.setView(dialogView);

        return alertDialog;
    }
}
