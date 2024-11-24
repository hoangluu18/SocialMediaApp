package com.mobile.catchy.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mobile.catchy.R;
import com.mobile.catchy.model.StoriesModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.StoriesHolder> {
    List<StoriesModel> list;
    Activity activity;

    public StoriesAdapter(List<StoriesModel> list, Activity activity) {
        this.list = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public StoriesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stories_layout, parent, false);
        return new StoriesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoriesHolder holder, int position) {
        if(position == 0) {
            Glide.with(activity).load(activity.getResources().getDrawable(R.drawable.ic_add))
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.startActivity(new Intent());
                }
            });
        }
        Glide.with(activity).load(list.get(position).getVideoUrl()).timeout(6500).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }


//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//
//    }

    @Override
    public int getItemCount() {
        return (list != null) ? list.size() : 0;
    }

    static class StoriesHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        public StoriesHolder(@NonNull View itemView) {
            super(itemView);

            imageView = (CircleImageView)   itemView.findViewById(R.id.imageView);
        }
    }

}
