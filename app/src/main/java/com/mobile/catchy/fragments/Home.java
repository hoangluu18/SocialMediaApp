package com.mobile.catchy.fragments;

import android.app.Activity;
import android.media.MediaCas;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.HomeAdapter;
import com.mobile.catchy.model.HomeModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Home extends Fragment {

    private RecyclerView recyclerView;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private FirebaseUser user;

    public Home() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        //reference = FirebaseFirestore.getInstance().collection("Posts").document(user.getUid());
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getContext());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestore();
    }

    private void loadDataFromFirestore() {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(user.getUid())
                        .collection("Post Images");

         reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
             @Override
             public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                 if(error != null) {
                     Log.e("Error: ", error.getMessage());
                     return;
                 }

                 if(value == null) {
                     Log.e("Error: ", "No data found");
                     return;
                 }
                 list.clear();
                 for (QueryDocumentSnapshot snapshot : value) {
                     if( !snapshot.exists() ) {
                         Log.e("Error: ", "No data found");
                         return;
                     }
                     HomeModel model = snapshot.toObject(HomeModel.class);

                     list.add(new HomeModel(
                            model.getName(),
                            model.getProfileImage(),
                            model.getImageUrl(),
                            model.getUid(),
                            model.getComments(),
                            model.getDescription(),
                            model.getId(),
                            model.getTimestamp(),
                            model.getLikeCount()
                     ));

                 }
                 adapter.notifyDataSetChanged();


             }
         });


    }

    private void init(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }
}