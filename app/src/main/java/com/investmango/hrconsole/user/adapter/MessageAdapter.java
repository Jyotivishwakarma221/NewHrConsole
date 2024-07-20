package com.investmango.hrconsole.user.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.model.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private Context context;
    private List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView idTextView;
        private TextView purposeTextView;
        private TextView messageTextView;
        private TextView meetingTimeTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.Id);
            purposeTextView = itemView.findViewById(R.id.purpose);
            messageTextView = itemView.findViewById(R.id.message);
            meetingTimeTextView = itemView.findViewById(R.id.meetingTimeText);

            messageTextView.setOnClickListener(v -> showReasonPopup(messageTextView.getContext(), messageTextView.getText().toString()));
        }

        private AlertDialog dialog;
        private void showReasonPopup(Context context, String reasonText) {
            if (context == null) {
                Log.e("MessageAdapter", "Context is null");
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.reason_popup_dialog, null);
            if (view == null) {
                Log.e("MessageAdapter", "Failed to inflate layout");
                return;
            }
            builder.setView(view);

            TextView popupReasonTextView = view.findViewById(R.id.popup_reason);
            popupReasonTextView.setText(reasonText);

            ImageView closeButton = view.findViewById(R.id.close_button);
            closeButton.setOnClickListener(v -> {
                // Dismiss the dialog when the close button is clicked
                if (dialog != null) {
                    dialog.dismiss();
                } else {
                    Log.e("MessageAdapter", "Dialog is null");
                }
            });

            dialog = builder.create();
            if (dialog != null) {
                dialog.show();
            } else {
                Log.e("MessageAdapter", "Failed to create dialog");
            }
        }

        public void bind(Message message) {
            idTextView.setText(String.valueOf(message.getId()));
            purposeTextView.setText(message.getPurpose());
            messageTextView.setText(message.getMessage());

            // Assuming getMeetingTime() returns a long representing time in milliseconds since the epoch
            long meetingTimeMillis = message.getMeetingTime();
            Date meetingTime = new Date(meetingTimeMillis);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            String meetingTimeStr = dateFormat.format(meetingTime);
            meetingTimeTextView.setText(meetingTimeStr);
        }




    }
}
