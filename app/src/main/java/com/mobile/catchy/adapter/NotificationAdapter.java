package com.mobile.catchy.adapter;

import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.catchy.R;
import com.mobile.catchy.model.NotificationModel;

import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHolder> {

    Context context;
    List<NotificationModel> list;

    public NotificationAdapter(Context context, List<NotificationModel> list) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_items, parent, false);
        return new NotificationHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {
        NotificationModel model = list.get(position);

        holder.notification.setText(model.getNotification());
        holder.time.setText(calculateTime(model.getTime()));

        FirebaseFirestore.getInstance().collection("Users")
                .document(model.getFollowerId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(context).load(imageUrl).into(holder.imageView);
                        }
                    }
                });
    }


    static class NotificationHolder extends RecyclerView.ViewHolder {
        TextView time, notification;
        ImageView imageView;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            time = itemView.findViewById(R.id.timeTv);
            notification = itemView.findViewById(R.id.notification);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    String calculateTime(Date date) {
        long millis = date.toInstant().toEpochMilli();
        return DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), 60000, DateUtils.FORMAT_ABBREV_TIME).toString();
    }


    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }
}
