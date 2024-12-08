package com.mobile.catchy.adapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.catchy.R;
import com.mobile.catchy.ReplacerActivity;
import com.mobile.catchy.model.HomeModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {
    //DONE
    private final List<HomeModel> list;
    Activity context;
    OnPressed onPressed;

    private BroadcastReceiver commentUpdateReceiver;
    private boolean isReceiverRegistered = false;

    public HomeAdapter(List<HomeModel> list, Activity context) {
        this.list = list;
        this.context = context;

        // Khởi tạo BroadcastReceiver
        commentUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("UPDATE_COMMENT_COUNT".equals(intent.getAction())) {
                    String postId = intent.getStringExtra("postId");
                    int newCommentCount = intent.getIntExtra("newCommentCount", 0);

                    // Cập nhật UI
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getId().equals(postId)) {
                            list.get(i).setCommentCount(newCommentCount);
                            notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }
        };

        // Đăng ký receiver
        registerReceiver();
    }

    private void registerReceiver() {
        if (!isReceiverRegistered && context != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.registerReceiver(commentUpdateReceiver, new IntentFilter("UPDATE_COMMENT_COUNT"), Context.RECEIVER_NOT_EXPORTED);
            }
            isReceiverRegistered = true;
        }
    }
    public void unregisterReceiver() {
        if (isReceiverRegistered && context != null && commentUpdateReceiver != null) {
            try {
                context.unregisterReceiver(commentUpdateReceiver);
                isReceiverRegistered = false;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }


    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        holder.userNameTv.setText(list.get(position).getName());
        holder.timeTv.setText(""+ list.get(position).getTimestamp());

        List<String> likeList = list.get(position).getLikes();
        int commentCount = list.get(position).getCommentCount();
        int count = likeList.size();

        if(count == 0){
            holder.likeCountTv.setText("0 Like");
        }
        else if(count == 1){
            holder.likeCountTv.setText(count + " Like");
        }else{
            holder.likeCountTv.setText(count + " Likes");
        }

        //check if already liked
        assert user != null;
        holder.likeCheckBox.setChecked(likeList.contains(user.getUid()));

        holder.descriptionTv.setText(list.get(position).getDescription());

        Random random = new Random();

        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

        Glide.with(context.getApplicationContext())
                .load(list.get(position).getProfileImage())
                .placeholder(R.drawable.ic_person)
                .timeout(6500)
                .into(holder.profileImage);

        Glide.with(context.getApplicationContext())
                .load(list.get(position).getImageUrl())
                .placeholder(new ColorDrawable(color))
                .timeout(7000)
                .into(holder.imageView);

        holder.clickListener(position,
                list.get(position).getId(),
                list.get(position).getName(),
                list.get(position).getUid(),
                list.get(position).getLikes(),
                list.get(position).getImageUrl()
        );

        if (commentCount == 0) {
            holder.commentTV.setVisibility(View.GONE);
        }
        else if(commentCount == 1){
            holder.commentTV.setVisibility(View.VISIBLE);
            holder.commentTV.setText("See all + " + commentCount + " comment");
        }
        else {
            holder.commentTV.setVisibility(View.VISIBLE);
            holder.commentTV.setText("See all + " + commentCount + " comments");
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void OnPressed(OnPressed onPressed) {
        this.onPressed = onPressed;
    }

    public interface OnPressed {
        void onLiked(int position, String id,String uid, List<String> likeList, boolean isChecked);

        //void setCommentCount(TextView textView);

//        void setCommentCount(TextView textView);
    }

     class HomeHolder extends RecyclerView.ViewHolder {

         private final CircleImageView profileImage;
         private final TextView userNameTv;
         private final TextView timeTv;
         private final TextView likeCountTv;
         private final TextView descriptionTv;
         private final ImageView imageView;
         private final CheckBox likeCheckBox;
         private final ImageButton commentBtn;
         private final ImageButton shareBtn;

         TextView commentTV;

        public HomeHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            userNameTv = itemView.findViewById(R.id.nameTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            likeCountTv = itemView.findViewById(R.id.likeCountTv);
            imageView = itemView.findViewById(R.id.imageView);
            likeCheckBox = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            descriptionTv = itemView.findViewById(R.id.descTv);

            commentTV = itemView.findViewById(R.id.commentTV);

            //onPressed.setCommentCount(commentTV);
        }

        public void clickListener(final int position, final String id, String name, final String uid, List<String> likes, final String imageUrl) {
            commentBtn.setOnClickListener(view -> {
                Intent intent = new Intent(context, ReplacerActivity.class);

                intent.putExtra("id", id);
                intent.putExtra("uid", uid);
                intent.putExtra("isComment", true);
                context.startActivity(intent);
            });

           // likeCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> onPressed.onLiked(position, id,uid,likes, isChecked));
            likeCheckBox.setOnClickListener(view -> {
                boolean isChecked = likeCheckBox.isChecked();

                // Cập nhật UI
                int count = likes.size();
                if (isChecked) {
                    // Nếu chưa có like của user hiện tại
                    if (!likes.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                        count += 1;
                    }
                } else {
                    // Nếu đã có like của user hiện tại
                    if (likes.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        count -= 1;
                    }
                }

                // Hiển thị text phù hợp
                if (count == 0) {
                    likeCountTv.setText("0 Like");
                } else if (count == 1) {
                    likeCountTv.setText("1 Like");
                } else {
                    likeCountTv.setText(count + " Likes");
                }


                //update firestore

                DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(uid)
                        .collection("Post Images")
                        .document(id);

                if(likes.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    likes.remove(FirebaseAuth.getInstance().getCurrentUser().getUid()); // unlike

                } else {
                    likes.add(FirebaseAuth.getInstance().getCurrentUser().getUid()); // like
                }

                Map<String, Object> map = new HashMap<>();
                map.put("likes", likes);
                reference.update(map);

            });
            shareBtn.setOnClickListener(v -> {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, imageUrl);
                intent.setType("text/*");
                context.startActivity(Intent.createChooser(intent, "Share link using..."));

            });



        }

    }



}
