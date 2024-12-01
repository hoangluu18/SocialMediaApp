package com.mobile.catchy.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.ChatUserAdapter;
import com.mobile.catchy.model.ChatUserModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatUsersActivity extends AppCompatActivity {
    ChatUserAdapter adapter;

    List<ChatUserModel> list;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_user);

        init();
        fetchUserData();
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
                ChatUserModel model = snapshot.toObject(ChatUserModel.class);
                list.add(model);
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
                    oppositeUID = uids.get(0);
                } else {
                    oppositeUID = uids.get(1);
                }

                Intent intent = new Intent(ChatUsersActivity.this, ChatActivity.class);
                intent.putExtra("uid", oppositeUID);
                intent.putExtra("id", chatID);
                startActivity(intent);
            }


        });
    }
}