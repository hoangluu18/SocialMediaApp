package com.mobile.catchy.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.catchy.R;
import com.mobile.catchy.model.ChatUserModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.CharUserHolder> {
    Activity context;
    List<ChatUserModel> list;
    public OnStartChat startChat;
    public ChatUserAdapter(Activity context, List<ChatUserModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CharUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_user_items, parent, false);
        return new CharUserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharUserHolder holder, int position) {
        fetchImageUrl(list.get(position).getUid(), holder);
        holder.time.setText(list.get(position).getTime().toString());
        holder.lastMessage.setText(list.get(position).getLastMessage());

        holder.itemView.setOnClickListener(v->{

            startChat.clicked(position, list.get(position).getUid(), list.get(position).getId());

        });
//        Glide.with(context.getApplicationContext().load())
    }

    void fetchImageUrl (List<String> uids, CharUserHolder holder ) {
        String oppositeUID;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(uids.get(0).equalsIgnoreCase(user.getUid())) {
            oppositeUID = uids.get(0);
        } else {
            oppositeUID = uids.get(1);
        }
        FirebaseFirestore.getInstance().collection("User").document(oppositeUID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot snapshot = task.getResult();
                            Glide.with(context.getApplicationContext()).load(snapshot.getString("profileImage")).into(holder.imageView);
                            holder.name.setText(snapshot.getString("name"));
                        } else {
                            Toast.makeText(context, "Error: " +  task.getException().getMessage(), Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }



    static class CharUserHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        TextView name, lastMessage, time, count;


        public CharUserHolder(@NonNull View itemview) {
            super(itemview);
            imageView = itemview.findViewById(R.id.profileImage);
            name = itemview.findViewById(R.id.nameTV);
            lastMessage = itemview.findViewById(R.id.messageTV);

            time = itemview.findViewById(R.id.timeTv);

            count = itemview.findViewById(R.id.messageCountTV);



        }
    }

    public interface OnStartChat {
        void clicked(int position, List<String> uids, String chatID);
    }

    public void OnStartChat (OnStartChat startChat) {
        this.startChat = startChat;
    }
}
