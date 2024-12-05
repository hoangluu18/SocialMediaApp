package com.mobile.catchy.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationModel {

    String id, notification;

    @ServerTimestamp
    Date time;
    String followerId;

    public NotificationModel() {
    }


    public NotificationModel(String id, String notification, Date time, String followerId) {
        this.id = id;
        this.notification = notification;
        this.time = time;
        this.followerId = followerId;
    }

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

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }


}
