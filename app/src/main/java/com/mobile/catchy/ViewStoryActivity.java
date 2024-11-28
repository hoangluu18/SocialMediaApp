package com.mobile.catchy;

import android.os.Bundle;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ViewStoryActivity extends AppCompatActivity {
    public static final String VIDEO_URL_KEY = "videoURL";

    PlayerView exoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_story);
        init();
        String url = getIntent().getStringExtra(VIDEO_URL_KEY);

        if(url == null || url.isEmpty()) {
            finish();
        }
        MediaItem item = MediaItem.fromUri(url);
        ExoPlayer player = new ExoPlayer.Builder(this).build();
        player.setMediaItem(item);
        exoPlayer.setPlayer(player);
        player.play();

    }

    void init() {
        exoPlayer = findViewById(R.id.videoView);
    }
}