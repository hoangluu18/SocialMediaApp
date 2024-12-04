package com.mobile.catchy.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.catchy.MainActivity;
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

    String chatID;
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
            CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
            //String pushID = reference.document().getId();

            Map<String,  Object> map = new HashMap<>();

            map.put("lastMessage", message);
            map.put("time", FieldValue.serverTimestamp());


            reference.document(chatID).update(map);

//            CollectionReference messageRef = FirebaseFirestore.getInstance().collection("Messages").document(pushID).collection("Messages");

            String messageID = reference.document(chatID).collection("Messages").document().getId();


            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", messageID);
            messageMap.put("message", message);
            messageMap.put("senderID", user.getUid());
            messageMap.put("time", FieldValue.serverTimestamp());

            reference.document(chatID).collection("Messages").document(messageID).set(messageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        chatET.setText("");
                    } else {
                        Toast.makeText(ChatActivity.this, "Something is daijoubu janai", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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


//        for (ChatModel chat : list) {
//            Log.d("ChatList", "ID: " + chat.getId());
//            Log.d("ChatList", "Message: " + chat.getMessage());
//            Log.d("ChatList", "SenderID: " + chat.getSenderID());
//            Log.d("ChatList", "Time: " + chat.getTime());
//        }
//        adapter = new ChatAdapter(this, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setAdapter(adapter);
        Log.d("ChatList", "Initialization complete");
    }

    void loadUserData() {
        String oppositeUID = getIntent().getStringExtra("uid");
        if(oppositeUID == null || oppositeUID.isEmpty()) {
            Log.e("UserData", "Cannot load");
            return;
        }
        Log.d("UserData", "oppositeUID: " + oppositeUID);
        FirebaseFirestore.getInstance().collection("Users").document(oppositeUID).addSnapshotListener((value, error) -> {
            if(error!= null) {
                Log.e("UserData", "Cannot load2");
                return;
            }
            if (value != null && value.exists()) {

                String userName = value.getString("name");
                Boolean isOnline = value.getBoolean("online");
                String profileImage = value.getString("profileImage");

                if (userName != null) {
                    System.out.println(userName);
                    name.setText(userName);
                }
                if (isOnline != null) {
                    System.out.println(isOnline);
                    status.setText(isOnline ? "Online" : "Offline");
                } else {
                    status.setText("Offline");
                }
                if (profileImage != null) {
                    System.out.println(profileImage);
                    Glide.with(this).load(profileImage).into(imageView);
                }

            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    void loadMessages() {
        chatID = getIntent().getStringExtra("id");
        Log.d("ChatList", "userName: " + chatID);
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
                } else {
                    adapter = new ChatAdapter(ChatActivity.this, list);
                    recyclerView.setAdapter(adapter);
                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("TAB_INDEX", 0); // Giả sử 0 là vị trí của tab Home
        startActivity(intent);
        finish();
    }
}