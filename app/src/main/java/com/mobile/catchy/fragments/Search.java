package com.mobile.catchy.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.UserAdapter;
import com.mobile.catchy.model.Users;

import java.util.ArrayList;
import java.util.List;


public class Search extends Fragment {
    //DONE
    SearchView searchView;
    RecyclerView recyclerView;
    UserAdapter adapter;
    private List<Users> list;
    CollectionReference reference;

    OnDataPass onDataPass;

    public Search() {
        // Required empty public constructor
    }

    public interface OnDataPass {
        void onChange(String uid);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        onDataPass = (OnDataPass) context;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        reference = FirebaseFirestore.getInstance().collection("Users");

        loadUserData();

        searchUser();

        clickListener();


    }

    private void clickListener() {
        adapter.OnUserClicked(new UserAdapter.OnUserClicked() {
            @Override
            public void onClicked( String uid) {
                onDataPass.onChange(uid);
            }
        });
    }

    private void searchUser() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    // Tìm kiếm khi người dùng nhấn submit
                    query = query.trim().toLowerCase();
                    String finalQuery = query;
                    reference.orderBy("search")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                list.clear();
                                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                    Users users = snapshot.toObject(Users.class);
                                    if (users != null &&
                                            users.getName().toLowerCase().contains(finalQuery) &&
                                            !users.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        list.add(users);
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(),
                                        "Lỗi tìm kiếm: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                }
                return true; // Đổi thành true để ẩn bàn phím sau khi submit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Chỉ load lại data khi text rỗng
                if (newText.isEmpty()) {
                    loadUserData();
                }
                return false;
            }
        });
    }



    private void loadUserData() {
        // Load toàn bộ users (trừ current user)
        reference.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Users users = snapshot.toObject(Users.class);
                        // Kiểm tra không phải current user
                        if (users != null &&
                                !users.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            list.add(users);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Lỗi tải danh sách: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void init(View view) {
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        adapter = new UserAdapter(list);
        recyclerView.setAdapter(adapter);
    }
}