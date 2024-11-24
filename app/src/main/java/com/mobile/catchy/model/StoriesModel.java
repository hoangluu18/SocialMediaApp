package com.mobile.catchy.model;

public class StoriesModel {

    String videoUrl, id, name, uid;
    public StoriesModel() {

    }

    public StoriesModel(String videoUrl, String id, String name, String uid) {
        this.id = id;
        this.name = name;
        this.videoUrl = videoUrl;
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
