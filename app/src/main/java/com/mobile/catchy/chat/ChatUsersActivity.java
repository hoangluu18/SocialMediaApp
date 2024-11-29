package com.mobile.catchy.chat;

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
}