package com.mobile.catchy.fragments;

import static android.app.Activity.RESULT_OK;

import static com.mobile.catchy.MainActivity.IS_SEARCHED_USER;
import static com.mobile.catchy.MainActivity.USER_ID;
import static com.mobile.catchy.fragments.Home.LIST_SIZE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobile.catchy.MainActivity;
import com.mobile.catchy.R;
import com.mobile.catchy.model.PostImageModel;

import de.hdodenhof.circleimageview.CircleImageView;
import com.canhub.cropper.CropImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Profile extends Fragment {

    private TextView nameTv, toolbarNameTv, statusTv, followingCountTv, followersCountTv, postCountTv;
    private CircleImageView profileImage;
    private Button followBtn, startChatBtn;
    private RecyclerView recyclerView;
    private LinearLayout countLayout;
    String userUID;
    FirestoreRecyclerAdapter<PostImageModel, PostImageHolder> adapter;
    private FirebaseUser user;
    private ImageButton editProfileBtn;
    boolean isMyProfile = false;
    private LinearLayout buttonLayout;
    boolean isFollowed;
    List<Object> followersList,followingList_2;
    List<Object>followingList;
    DocumentReference userRef, myRef;

    // ActivityResultLauncher để cắt ảnh
    private final ActivityResultLauncher<CropImageContractOptions> cropImageLauncher =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful()) {
                    Uri uri = result.getUriContent();
                    // Hiển thị ảnh đã cắt lên profileImage
                    Glide.with(requireContext())
                            .load(uri)
                            .into(profileImage);
                    uploadImage(uri);
                } else {
                    // Xử lý lỗi
                    Exception error = result.getError();
                    if (error != null) {
                        Log.e("CropImage", error.getMessage());
                    }
                }
            });
    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);



        myRef = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        if(IS_SEARCHED_USER){
            isMyProfile = false;
            userUID = USER_ID;

            loadData();
        }
        else{
            isMyProfile = true;
            userUID = user.getUid();
        }
        if(isMyProfile) {
            editProfileBtn.setVisibility(View.VISIBLE);
            followBtn.setVisibility(View.GONE);
            countLayout.setVisibility(View.VISIBLE);
        }
        else {
            countLayout.setVisibility(View.VISIBLE);
            editProfileBtn.setVisibility(View.GONE);
            buttonLayout.setVisibility(View.VISIBLE);
        }
        userRef = FirebaseFirestore.getInstance().collection("Users")
                .document(userUID);

        loadBasicData();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        loadPostImages();

        recyclerView.setAdapter(adapter);



        
        clickListeners();
    }

    private void loadData() {
        myRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Log.e("Tag_b"," " + error.getMessage());
                    return;
                }

                if(value == null || !value.exists()){
                   return;
                }
                followingList_2 = (List<Object>) value.get("following");
            }
        });
    }

    private void clickListeners() {

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFollowed){
                    followersList.remove(user.getUid());

                    followingList_2.remove(userUID);

                    final Map<String,Object> map_2 = new HashMap<>();
                    map_2.put("following", followingList_2);

                    Map<String,Object> map = new HashMap<>();
                    map.put("followers", followersList);


                    userRef.update(map).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            followBtn.setText("Follow");
                            isFollowed = false;
                            myRef.update(map_2).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(getContext(), "UnFollowed", Toast.LENGTH_SHORT).show();
                                } else {
                                    assert task1.getException() != null;
                                    Log.e("Tag_3", task1.getException().getMessage());
                                }
                            });

                        } else {
                            assert task.getException() != null;
                            Log.e("Tag", "" + task.getException().getMessage());
                        }
                    });

                }else{
                    followersList.add(user.getUid());

                    followingList_2.add(userUID);

                    final Map<String,Object> map_2 = new HashMap<>();
                    map_2.put("following", followingList_2);

                    Map<String,Object> map = new HashMap<>();
                    map.put("followers", followersList);

                    userRef.update(map).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            followBtn.setText("UnFollow");
                            isFollowed = true;
                            myRef.update(map_2).addOnCompleteListener(task12 -> {
                                if (task12.isSuccessful()) {
                                    Toast.makeText(getContext(), "Followed", Toast.LENGTH_SHORT).show();
                                } else {
                                    assert task12.getException() != null;
                                    Log.e("tag_3_1", task12.getException().getMessage());
                                }
                            });


                        } else {
                            assert task.getException() != null;
                            Log.e("Tag", "" + task.getException().getMessage());
                        }
                    });



                }
            }
        });
        editProfileBtn.setOnClickListener(v -> {
            // Tạo các tùy chọn cho việc cắt ảnh
            CropImageOptions cropImageOptions = new CropImageOptions();
            cropImageOptions.guidelines = CropImageView.Guidelines.ON; // Hiển thị hướng dẫn cắt ảnh
            cropImageOptions.fixAspectRatio = true; // Cố định tỷ lệ cắt ảnh
            cropImageOptions.aspectRatioX = 1; // Đặt tỷ lệ chiều rộng là 1
            cropImageOptions.aspectRatioY = 1; // Đặt tỷ lệ chiều cao là 1

            // Tạo CropImageContractOptions với tùy chọn đã cấu hình
            CropImageContractOptions options = new CropImageContractOptions(null, cropImageOptions);

            // Khởi chạy cropImageLauncher với options đã cấu hình
            cropImageLauncher.launch(options);
        });
    }

    private void loadBasicData() {
        Toast.makeText(getContext(), "dau loadbasic", Toast.LENGTH_SHORT).show();

        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Tag_0", error.getMessage());
                    return;
                }
                assert value != null;
                if (value.exists()) {
                    String name = value.getString("name");
                    String status = value.getString("status");

                    String profileURL = value.getString("profileImage");

                    nameTv.setText(name);
                    toolbarNameTv.setText(name);
                    statusTv.setText(status);

                    followersList = (List<Object>) value.get("followers");
                    followingList = (List<Object>) value.get("following");

                    followersCountTv.setText("" + followersList.size());
                    followingCountTv.setText("" + followingList.size());

                    //test
                    CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                            .document(userUID)
                            .collection("Post Images");

                    reference.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            LIST_SIZE = task.getResult().size(); // Lấy số lượng ảnh từ kết quả
                            Toast.makeText(getContext(), "size: " + LIST_SIZE, Toast.LENGTH_SHORT).show();
                            postCountTv.setText("" + LIST_SIZE);
                        } else {
                            Log.e("Firestore Error", "Error getting documents: ", task.getException());
                        }
                    });


                    try {
                        Glide.with(getContext().getApplicationContext())
                                .load(profileURL)
                                .placeholder(R.drawable.ic_person)
                                .timeout(6500)
                                .into(profileImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(!isMyProfile){
                        if(followersList.contains(user.getUid())){
                            followBtn.setText("Unfollow");
                            isFollowed = true;
                        }
                        else{
                            isFollowed = false;
                            followBtn.setText("Follow");
                        }
                    }
//                    if(followersList.contains(userUID)){
//                        followBtn.setText("Unfollow");
//                        isFollowed = true;
//                    }
//                    else {
//                        isFollowed = false;
//                        followBtn.setText("Follow");
//                    }





                }


            };
        });

//        postCountTv.setText("" + LIST_SIZE);
        Toast.makeText(getContext(), "cuoi loadbasic", Toast.LENGTH_SHORT).show();
    }


    private void init(View view){
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        assert getActivity() != null;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        nameTv = view.findViewById(R.id.nameTv);
        statusTv = view.findViewById(R.id.statusTV);
        toolbarNameTv = view.findViewById(R.id.toolbarNameTV);
        followersCountTv = view.findViewById(R.id.followersCountTv);
        followingCountTv = view.findViewById(R.id.followingCountTv);
        postCountTv = view.findViewById(R.id.postCountTv);
        profileImage = view.findViewById(R.id.profileImage);
        followBtn = view.findViewById(R.id.followBtn);
        recyclerView = view.findViewById(R.id.recyclerView);
        countLayout = view.findViewById(R.id.countLayout);
        editProfileBtn = view.findViewById(R.id.edit_profileImage);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        buttonLayout = view.findViewById(R.id.buttonLayout);
    }

    private void loadPostImages() {
        Toast.makeText(getContext(), "dau loadpost", Toast.LENGTH_SHORT).show();
        if(userUID == null)
            return;
        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(userUID);

        Query query = reference.collection("Post Images");
        FirestoreRecyclerOptions<PostImageModel> options = new FirestoreRecyclerOptions.Builder<PostImageModel>()
                .setQuery(query, PostImageModel.class)
                .build();

         adapter = new FirestoreRecyclerAdapter<PostImageModel, PostImageHolder>(options) {
            @NonNull
            @Override
            public PostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_items, parent, false);
                return new PostImageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostImageHolder holder, int position, @NonNull PostImageModel model) {
                Glide.with(holder.imageView.getContext())
                        .load(model.getImageUrl())
                        .timeout(6500)
                        .into(holder.imageView);
            }

        };

        Toast.makeText(getContext(), "cuoi loadPost", Toast.LENGTH_SHORT).show();
    }




    private static class PostImageHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        public PostImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
//
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if (result == null)
//                return;
//
//            Uri uri = result.getUri();
//
//            uploadImage(uri);
//
//        }
//
//    }

    private void uploadImage(Uri uri) {
         StorageReference reference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        reference.putFile(uri)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        reference.getDownloadUrl()
                                .addOnSuccessListener(uri1 -> {
                                    String imageURL = uri1.toString();

                                    UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                                    request.setPhotoUri(uri1);

                                    user.updateProfile(request.build());

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("profileImage", imageURL);

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(user.getUid())
                                            .update(map).addOnCompleteListener(task1 -> {

                                                if (task1.isSuccessful())
                                                    Toast.makeText(getContext(),
                                                            "Updated Successful", Toast.LENGTH_SHORT).show();
                                                else {
                                                    assert task1.getException() != null;
                                                    Toast.makeText(getContext(),
                                                            "Error: " + task1.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                });


                    } else {
                        assert task.getException() != null;
                        Toast.makeText(getContext(), "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }





}