package com.example.appcore.ui.chatbox.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appcore.R;
import com.example.appcore.ui.chatbox.model.ChatMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {
    private List<ChatMessage> chatMessages;
    private SimpleDateFormat timeFormat;

    public ChatMessageAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout messageContainer;
        private TextView textViewMessage;
        private TextView textViewTime;

        public ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }

        public void bind(ChatMessage message) {
            textViewMessage.setText(message.getMessage());
            textViewTime.setText(timeFormat.format(new Date(message.getTimestamp())));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageContainer.getLayoutParams();
            
            if (message.isFromUser()) {
                // Tin nhắn từ user - căn phải, màu xanh
                params.gravity = Gravity.END;
                messageContainer.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.user_message_bg));
                textViewMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            } else {
                // Tin nhắn từ AI - căn trái, màu xám
                params.gravity = Gravity.START;
                messageContainer.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ai_message_bg));
                textViewMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
            }
            
            messageContainer.setLayoutParams(params);
        }
    }
}

