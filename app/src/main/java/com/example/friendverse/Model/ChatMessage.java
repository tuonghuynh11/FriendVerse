package com.example.friendverse.Model;

import java.util.Date;

public class ChatMessage {
    public static final String IDKEY="id";
    public static final String SENDERIDKEY="senderId";
    public static final String RECEIVERIDKEY="receiverId";
    public static final String MESSAGEKEY="message";
    public static final String DATETIMEKEY="dateTime";
    public static final String DATEOBJECTKEY="dateObject";

    public static final String KEY_COLLECTION_CONVERSATION="conversations";
    public static final String KEY_SENDER_NAME="senderName";
    public static final String KEY_RECEIVER_NAME="receiverName";
    public static final String KEY_SENDER_IMAGE="senderImage";
    public static final String KEY_RECEIVER_IMAGE="receiverImage";
    public static final String KEY_LAST_MESSAGE="lastMessage";
    public static final String KEY_MESSAGE_TYPE="messageType";

    private String id;
    private String senderId;
    private String receiverId;
    private String message;
    private String dateTime;

    private Date dateObject;


    private String messageType;


    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getConversionId() {
        return conversionId;
    }

    public void setConversionId(String conversionId) {
        this.conversionId = conversionId;
    }

    public String getConversionName() {
        return conversionName;
    }

    public void setConversionName(String conversionName) {
        this.conversionName = conversionName;
    }

    public String getConversionImage() {
        return conversionImage;
    }

    public void setConversionImage(String conversionImage) {
        this.conversionImage = conversionImage;
    }

    private String conversionId;
    private String conversionName;
    private String conversionImage;
    public ChatMessage(String id, String senderId, String receiverId, String message, Date dateObject, String messageType) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.dateObject = dateObject;
        this.messageType=messageType;
    }

    public ChatMessage() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
