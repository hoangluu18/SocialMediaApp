package com.mobile.catchy.utils;

import android.net.Uri;

import com.mobile.catchy.model.GalleryImages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageContent {

    static final List<GalleryImages> list = new ArrayList<>();
    public static void loadImages(File file){
        GalleryImages images = new GalleryImages();
        images.picUri = Uri.fromFile(file);
        addImage(images);
    }



    private static void addImage(GalleryImages images){
        list.add(0,images);
    }

    public static void loadSavedImages(File directory){
        list.clear();

        if( directory.exists()){
            File[] files = directory.listFiles();
            for(File file: files){
                String absolutePath = file.getAbsolutePath();
                String extention = absolutePath.substring(absolutePath.lastIndexOf("."));

                if(extention.equals(".jpg") || extention.equals(".png")){
                    loadImages(file);
                }
            }
        }
    }



}
