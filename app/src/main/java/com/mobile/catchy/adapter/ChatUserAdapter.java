package com.mobile.catchy.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.catchy.model.ChatUserModel;

import java.util.List;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.CharUserHolder> {
    Activity context;
    List<ChatUserModel> list;

    public ChatUserAdapter(Activity context, List<ChatUserModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CharUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull CharUserHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class CharUserHolder extends RecyclerView.ViewHolder {
        public CharUserHolder(@NonNull View itemview) {
            super(itemview);
        }
    }


}
