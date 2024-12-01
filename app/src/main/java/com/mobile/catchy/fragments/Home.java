package com.mobile.catchy.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.MutableLiveData;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.HomeAdapter;
import com.mobile.catchy.adapter.StoriesAdapter;
import com.mobile.catchy.model.HomeModel;
import com.mobile.catchy.model.StoriesModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Home extends Fragment {

    private RecyclerView recyclerView;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private FirebaseUser user;
    private final MutableLiveData<Integer> commentCount = new MutableLiveData<>();

    RecyclerView storiesRecyclerView;
    StoriesAdapter storiesAdapter;
    List<StoriesModel> storiesModelList;
    Activity activity;
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
        activity = getActivity();
        init(view);

        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestore();

        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onLiked(int position, String id, String uid, List<String> likeList, boolean isChecked) {
                DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(uid)
                        .collection("Post Images")
                        .document(id);

                if(likeList.contains(user.getUid())) {
                    likeList.remove(user.getUid()); // unlike

                } else {
                    likeList.add(user.getUid()); // like
                }

                Map<String, Object> map = new HashMap<>();
                map.put("likes", likeList);

                reference.update(map);

            }
//KHONG SU DUNG CAI NAY NUA
//            @Override
//            public void setCommentCount(TextView textView) {
//
////                commentCount.observe((LifecycleOwner) getContext(), integer -> {
////                    if( integer == 0){
////                        textView.setVisibility(View.GONE);
////                    }else{
////                        textView.setVisibility(View.VISIBLE);
////                        textView.setText("See all + " + integer + " comments");
////                    }
////
////                });
//
//            }

        });
    }

    private void loadDataFromFirestore() {

        final DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());
        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");

        reference.get().addOnSuccessListener(value -> {
            if (value == null) return;

            List<String> uidList = (List<String>) value.get("following");
            if (uidList == null || uidList.isEmpty()) return;

            list.clear();

            collectionReference.whereIn("uid", uidList)
                    .get()
                    .addOnSuccessListener(value1 -> {
                        for (QueryDocumentSnapshot snapshot : value1) {
                            snapshot.getReference().collection("Post Images")
                                    .get()
                                    .addOnSuccessListener(value11 -> {
                                        for (final QueryDocumentSnapshot snapshot1 : value11) {
                                            if (!snapshot1.exists()) continue;

                                            HomeModel model = snapshot1.toObject(HomeModel.class);

                                            // Lấy số lượng comment
                                            snapshot1.getReference().collection("Comments")
                                                    .get()
                                                    .addOnSuccessListener(commentsSnapshot -> {
                                                        model.setCommentCount(commentsSnapshot.size());

                                                        // Thêm vào list và cập nhật UI
                                                        if (!list.contains(model)) {  // Kiểm tra trùng lặp
                                                            list.add(model);
                                                            adapter.notifyDataSetChanged();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        model.setCommentCount(0);
                                                        if (!list.contains(model)) {
                                                            list.add(model);
                                                            adapter.notifyDataSetChanged();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });

            loadStories(uidList);
        });
    }

    void loadStories(List<String> followingList) {

        Query query = FirebaseFirestore.getInstance().collection("Stories");
        query.whereIn("uid", followingList).addSnapshotListener((value, error) -> {

            if (error != null) {
                Log.d("Error: ", error.getMessage());
            }

            if (value == null)
                return;

            for (QueryDocumentSnapshot snapshot : value) {

                if (!value.isEmpty()) {
                    StoriesModel model = new StoriesModel();
                    model.setId(snapshot.getString("id"));
                    model.setUid(snapshot.getString("uid"));
                    model.setName(snapshot.getString("name"));
                    model.setUrl(snapshot.getString("url"));
                    model.setType(snapshot.getString("type"));
                    storiesModelList.add(model);
                }


            }
            storiesAdapter.notifyDataSetChanged();

        });

    }

    private void init(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        storiesRecyclerView = view.findViewById(R.id.storiesRecyclerView);
        storiesRecyclerView.setHasFixedSize(true);
        storiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        storiesModelList= new ArrayList<>();
        storiesModelList.add(new StoriesModel("", "", "", "", ""));
        storiesAdapter = new StoriesAdapter( storiesModelList, getActivity());
        storiesRecyclerView.setAdapter(storiesAdapter);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (adapter != null) {
            adapter.unregisterReceiver();
        }
    }
}