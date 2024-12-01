package com.mobile.catchy.chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.ChatAdapter;
import com.mobile.catchy.model.ChatModel;
import com.mobile.catchy.model.ChatUserModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    FirebaseUser user;
    CircleImageView imageView;
    TextView name, status;
    EditText chatET;
    ImageView sendBtn;
    RecyclerView recyclerView;

    ChatAdapter adapter;
    List<ChatModel> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        init();

        loadUserData();
        loadMessages();
        sendBtn.setOnClickListener(v -> {
            String message = chatET.getText().toString().trim();
            if(message.isEmpty()) {
                return;
            }
            Map<String, Object> map = new HashMap<>();

        });
    }

    void init() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        imageView = findViewById(R.id.profileImage);

        name = findViewById(R.id.nameTV);

        status = findViewById(R.id.statusTV);

        chatET = findViewById(R.id.chatET);

        sendBtn = findViewById(R.id.sendBtn);

        recyclerView = findViewById(R.id.recyclerView);

        list = new ArrayList<>();
        adapter = new ChatAdapter(this, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    void loadUserData() {
        String oppositeUID = getIntent().getStringExtra("uid");
        if(oppositeUID == null || oppositeUID.isEmpty()) {
            Log.e("UserData", "Cannot load");
            return;
        }
        FirebaseFirestore.getInstance().collection("Users").document(oppositeUID).addSnapshotListener((value, error) -> {
            if(error!= null) {
                Log.e("UserData", "Cannot load2");
                return;
            }
            if (value != null && value.exists()) {
                Boolean isOnline = value.getBoolean("online");
                if (isOnline != null) {
                    status.setText(isOnline ? "Online" : "Offline");
                }
                String profileImage = value.getString("profileImage");
                if (profileImage != null) {
                    Glide.with(getApplicationContext()).load(profileImage).into(imageView);
                }
                String userName = value.getString("name");
                if (userName != null) {
                    name.setText(userName);
                }
            }
            Boolean isOnline = value.getBoolean("online");
            if (isOnline != null) {
                status.setText(isOnline ? "Online" : "Offline");
            } else {
                status.setText("Offline");
            }

            Glide.with(this).load(value.getString("profileImage")).into(imageView);
            name.setText(value.getString("name"));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    void loadMessages() {
        String chatID = getIntent().getStringExtra("id");

        if(chatID == null) {
            Log.e("Chat","ChatID is null");
            return;
        }
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages").document(chatID).collection("Messages");
        reference.orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null) {
                    return;
                }
                if(value == null  || value.isEmpty()) {
                    return;
                }
                list.clear();
                for(QueryDocumentSnapshot snapshot : value) {
                    ChatModel model = snapshot.toObject(ChatModel.class);
                    list.add(model);
                }
                if(adapter != null) {
                    adapter.notifyDataSetChanged();
                }

            }
        });

    }
}