package com.mobile.catchy.model;

public class ChatUserModel {
    private String id, userid, name, imageURL;

    public ChatUserModel() {

    }

    public ChatUserModel(String id, String userid, String name, String imageURL) {
        this.id = id;
        this.userid = userid;
        this.name = name;
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public String getUserid() {
        return userid;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
