package com.mobile.catchy.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.NotificationAdapter;
import com.mobile.catchy.model.NotificationModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Notification extends Fragment {

    RecyclerView recyclerView;
    NotificationAdapter adapter;
    FirebaseUser user;
    List<NotificationModel> list;
    public Notification() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        loadNotification();

    }

    void init(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        list = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), list);
        recyclerView.setAdapter(adapter);
        user = FirebaseAuth.getInstance().getCurrentUser();
    }


    void loadNotification() {
        CollectionReference reference =  FirebaseFirestore.getInstance().collection("Notifications");
        reference.whereEqualTo("uid", user.getUid())
                //.orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null) {
                    return;
                }
                if(value.isEmpty()) {
                    return;
                }
                for(QueryDocumentSnapshot snapshot : value) {
                   NotificationModel model = snapshot.toObject(NotificationModel.class);
                    list.add(model);
                }
                list.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
                adapter.notifyDataSetChanged();
            }
        });
    }


}