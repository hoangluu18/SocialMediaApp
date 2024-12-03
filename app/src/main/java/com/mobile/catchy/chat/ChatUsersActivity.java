package com.mobile.catchy.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.ChatUserAdapter;
import com.mobile.catchy.model.ChatModel;
import com.mobile.catchy.model.ChatUserModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class ChatUsersActivity extends AppCompatActivity {
    ChatUserAdapter adapter;
    List<ChatUserModel> list;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_user);

        user = FirebaseAuth.getInstance().getCurrentUser();
        init();
        fetchUserData();
        clickListener();
    }
    void init() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        list = new ArrayList<>();
        adapter = new ChatUserAdapter(this, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    void fetchUserData() {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        reference.whereArrayContains("uid", user.getUid()).addSnapshotListener((value, error) -> {
            if(error != null) {
                return;
            }
            if(value.isEmpty()) {
                return;
            }
            list.clear();
            for(QueryDocumentSnapshot snapshot : value) {
                if(snapshot.exists()) {
                    ChatUserModel model = snapshot.toObject(ChatUserModel.class);
                    list.add(model);
                }
            }
            if (list == null || list.isEmpty()) {
                Log.d("ChatUserList", "List is empty or null");
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            for (ChatUserModel chatUser : list) {
                Log.d("ChatUserList", "ID: " + chatUser.getId());
                Log.d("ChatUserList", "Last Message: " + chatUser.getLastMessage());
                Log.d("ChatUserList", "UIDs: " + (chatUser.getUid() != null ? chatUser.getUid().toString() : "null"));
                Log.d("ChatUserList", "Time: " + (chatUser.getTime() != null ? sdf.format(chatUser.getTime()) : "null"));
                Log.d("ChatUserList", "---------------------------------------");
            }
            adapter.notifyDataSetChanged();
        });
    }

    void clickListener() {
        adapter.OnStartChat(new ChatUserAdapter.OnStartChat() {
            @Override
            public void clicked(int position, List<String> uids, String chatID) {
                String oppositeUID;
                if(uids.get(0).equalsIgnoreCase(user.getUid())) {
                    oppositeUID = uids.get(1);
                } else {
                    oppositeUID = uids.get(0);
                }
                Intent intent = new Intent(ChatUsersActivity.this, ChatActivity.class);
                intent.putExtra("uid", oppositeUID);
                intent.putExtra("id", chatID);
                startActivity(intent);
            }
        });
    }
}