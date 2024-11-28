package com.mobile.catchy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gowtham.library.utils.CompressOption;
import com.gowtham.library.utils.TrimType;
import com.gowtham.library.utils.TrimVideo;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.gowtham.library.utils.LogMessage;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StoryAddActivity extends AppCompatActivity {

    private static final int select_video = 101;
    //private static final String TAG = "VideoTrimmer";

    FirebaseUser user;
    VideoView videoView;
    StylishAlertDialog alertDialog;
    ImageButton uploadbutton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_story_add);

        init();

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, select_video);
    }





    void uploadVideotoStorage(Uri uri) {
        alertDialog = new StylishAlertDialog(this, StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Uploading...").setCancelable(false);
        alertDialog.show();


        File file = new File(uri.getPath());

        StorageReference storageReference =  FirebaseStorage.getInstance().getReference().child("Stories/"  + System.currentTimeMillis() + ".mp4");



        storageReference.putFile(Uri.fromFile(file) ).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {

                assert task.getResult() != null;
                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> uploadVideodatatoFireStore(String.valueOf(uri1)));
            } else {
                assert task.getException() != null;
                String error = task.getException().getMessage();
                Toast.makeText(StoryAddActivity.this, "Error: " + error,Toast.LENGTH_SHORT).show();
            }
         });
    }

    void uploadVideodatatoFireStore(String url) {
        CollectionReference reference =  FirebaseFirestore.getInstance().collection("Stories");

        String id = reference.document().getId();
        Map<String, Object> map = new HashMap<>();
        map.put("videoUrl", url);
        map.put("id", id);
        map.put("uid", user.getUid());
        map.put("name", user.getDisplayName());

        reference.document().set(map);

        alertDialog.dismiss();
        finish();
    }

    void init() {
        videoView = findViewById(R.id.videoView);
        uploadbutton = findViewById(R.id.uploadStoryBtn);

        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == select_video) {
            Uri uri = data.getData();
            TrimVideo.activity(String.valueOf(uri)).setCompressOption(new CompressOption())
                    .setTrimType(TrimType.MIN_MAX_DURATION)
                    .setMinToMax(5, 38)
                    .setHideSeekBar(true)
                    .start(this,startForResult);
        }
    }

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK &&
                        result.getData() != null) {


                    Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));

                    videoView.setVideoURI(uri);
                    videoView.start();
                    uploadbutton.setVisibility(View.VISIBLE);

                    uploadbutton.setOnClickListener(view -> {
                        uploadbutton.setVisibility(View.GONE);
                        videoView.pause();
                        uploadVideotoStorage(uri);
                    });
                    uploadVideotoStorage(uri);
//                    Log.d(TAG, "Trimmed path:: " + uri);

                } else {
                    Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
}