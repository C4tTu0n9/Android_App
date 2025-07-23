package com.example.appcore.ui.chatbox.model;

public class ChatMessage {
    private String message;
    private boolean isFromUser;
    private long timestamp;

    public ChatMessage() {
        // Constructor rỗng cho Firebase
    }

    public ChatMessage(String message, boolean isFromUser, long timestamp) {
        this.message = message;
        this.isFromUser = isFromUser;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFromUser() {
        return isFromUser;
    }

    public void setFromUser(boolean fromUser) {
        isFromUser = fromUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

