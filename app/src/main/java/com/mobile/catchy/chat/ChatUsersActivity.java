package com.mobile.catchy.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.ChatUserAdapter;
import com.mobile.catchy.model.ChatModel;
import com.mobile.catchy.model.ChatUserModel;
import com.mobile.catchy.model.Users;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ChatUsersActivity extends AppCompatActivity {
    ChatUserAdapter adapter;
    List<ChatUserModel> list;
    List<ChatUserModel> tmpList;
    List<String> uidList;
    FirebaseUser user;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_user);
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users");
        uidList = new ArrayList<>();
        tmpList = new ArrayList<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        init();
        fetchUserData();
        searchView = findViewById(R.id.searchView);
        searchUser();
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
            tmpList.clear();
            tmpList.addAll(list);
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

    void searchUser() {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    String query = newText.trim().toLowerCase();
                    reference.orderBy("search")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                uidList.clear();
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    Users users = snapshot.toObject(Users.class);
                                    if (users != null &&
                                            users.getName().toLowerCase().contains(query) &&
                                            !users.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        uidList.add(users.getUid());
                                    }
                                }
                                boolean check = true;
                                list.clear();
                                list.addAll(tmpList);
                                for (ChatUserModel test : list) {
                                    Log.d("AUID", "AUID: " + test.getUid());
                                }
                                Iterator<ChatUserModel> iterator = list.iterator();
                                while (iterator.hasNext()) {
                                    check = true;
                                    ChatUserModel user = iterator.next();
                                    for (String uid : uidList) {
                                        if (user.getUid().contains(uid)) {
                                            check = false;
                                            break;
                                        }
                                    }
                                    if (check == true) {
                                        iterator.remove();
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                // Xử lý khi có lỗi xảy ra
                            });
                } else {
                    fetchUserData();
                }
                return true;
            }
        });

    }
}