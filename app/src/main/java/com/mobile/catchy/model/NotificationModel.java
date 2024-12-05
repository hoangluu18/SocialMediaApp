package com.mobile.catchy.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationModel {

    private String id;
    private String notification;
    private String followerId;  // Thêm trường followerId
    @ServerTimestamp
    private Date time;

    // Constructor mặc định (bắt buộc để Firestore ánh xạ)
    public NotificationModel() {
    }

    // Constructor đầy đủ
    public NotificationModel(String id, String notification, String followerId, Date time) {
        this.id = id;
        this.notification = notification;
        this.followerId = followerId;
        this.time = time;
    }

    // Getter và Setter cho các thuộc tính
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
