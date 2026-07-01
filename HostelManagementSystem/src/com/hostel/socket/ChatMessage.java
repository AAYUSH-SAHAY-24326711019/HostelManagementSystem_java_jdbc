package com.hostel.socket;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type { TEXT, JOIN, LEAVE, BROADCAST, PRIVATE, SERVER_INFO }

    private Type type;
    private String senderName;
    private String senderRole; // ADMIN / GIRL / PARENT
    private int senderId;
    private int receiverId;     // 0 = broadcast
    private String receiverName;
    private String content;
    private String timestamp;

    public ChatMessage(Type type, String senderName, String senderRole, int senderId,
                       int receiverId, String receiverName, String content) {
        this.type = type;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public Type getType() { return type; }
    public String getSenderName() { return senderName; }
    public String getSenderRole() { return senderRole; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getReceiverName() { return receiverName; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + senderName + " (" + senderRole + "): " + content;
    }
}
