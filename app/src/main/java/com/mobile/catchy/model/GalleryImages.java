package com.mobile.catchy.model;

import android.net.Uri;

public class GalleryImages {
    //DONE
    public Uri picUri;

    public GalleryImages() {
    }



    public GalleryImages(Uri picUri) {
        this.picUri = picUri;
    }

    public Uri getPicUri() {
        return picUri;
    }

    public void setPicUri(Uri picUri) {
        this.picUri = picUri;
    }

}