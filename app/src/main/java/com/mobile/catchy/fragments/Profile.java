package com.mobile.catchy.fragments;

import static com.mobile.catchy.MainActivity.IS_SEARCHED_USER;
import static com.mobile.catchy.MainActivity.USER_ID;
import static com.mobile.catchy.utils.Constants.PREF_DIRECTORY;
import static com.mobile.catchy.utils.Constants.PREF_NAME;
import static com.mobile.catchy.utils.Constants.PREF_STORED;
import static com.mobile.catchy.utils.Constants.PREF_URL;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marsad.stylishdialogs.StylishAlertDialog;
import com.mobile.catchy.MainActivity;
import com.mobile.catchy.R;
import com.mobile.catchy.chat.ChatActivity;
import com.mobile.catchy.model.PostImageModel;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
    boolean isMyProfile = true;
    boolean isFollowed;
    List<String> followersList, followingList_2, followingList;
    DocumentReference userRef, myRef;
    int count;
    private ImageButton logoutBtn;
    String oppositeUID;
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

        if (userUID == null) {
            userUID = user.getUid();
            isMyProfile = true;
        }

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

            //Hide chat btn
            startChatBtn.setVisibility(View.GONE);
        }
        else {
            editProfileBtn.setVisibility(View.GONE);
            followBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);

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
        myRef.addSnapshotListener((value, error) -> {
            if(error != null){
                Log.e("Tag_b"," " + error.getMessage());
                return;
            }

            if(value == null || !value.exists()){
               return;
            }
            followingList_2 = (List<String>) value.get("following");
        });
    }

    private void clickListeners() {

        followBtn.setOnClickListener(view -> {

            if(isFollowed){

                followersList.remove(user.getUid());  //opposite user

                followingList_2.remove(userUID);   //us

                final Map<String,Object> map_2 = new HashMap<>();
                map_2.put("following", followingList_2);

                Map<String,Object> map = new HashMap<>();
                map.put("followers", followersList);


                userRef.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followBtn.setText("Follow");
                        //isFollowed = false;

                        myRef.update(map_2).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getContext(), "Unfollowed", Toast.LENGTH_SHORT).show();
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
                createNotification();

                followersList.add(user.getUid()); //opposite user

                followingList_2.add(userUID);   //us

                final Map<String,Object> map_2 = new HashMap<>();
                map_2.put("following", followingList_2);

                Map<String,Object> map = new HashMap<>();
                map.put("followers", followersList);

                userRef.update(map).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followBtn.setText("Unfollow");
                        //isFollowed = true;
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

        startChatBtn.setOnClickListener(v -> {
            queryChat();
        });


        logoutBtn.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String uid = currentUser.getUid();
                // Cập nhật trạng thái trước khi đăng xuất
                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(uid)
                        .update("status", "Offline")
                        .addOnCompleteListener(task -> {
                            // Đăng xuất sau khi cập nhật trạng thái
                            FirebaseAuth.getInstance().signOut();
                            requireActivity().finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Logout", "Error updating status: " + e.getMessage());
                            // Vẫn đăng xuất nếu cập nhật thất bại
                            FirebaseAuth.getInstance().signOut();
                            requireActivity().finish();
                        });
            } else {
                // Nếu user đã null, chỉ cần đăng xuất
                FirebaseAuth.getInstance().signOut();
                requireActivity().finish();
            }
        });


    }

    void StartChat(StylishAlertDialog  alertDialog) {

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");

        List<String> list = new ArrayList<>();

        list.add(0, user.getUid());
        list.add(1, userUID);

        String pushID = reference.document().getId();

        Map<String, Object> map = new HashMap<>();
        map.put("id", pushID);
        map.put("lastMessage", "Hi");
        map.put("time", FieldValue.serverTimestamp());
        map.put("uid", list);

        reference.document(pushID).update(map).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                reference.document(pushID).set(map);
            }
        });

        //todo - ---- - -- - -
        //Message

        CollectionReference messageRef = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(pushID)
                .collection("Messages");

        String messageID = messageRef.document().getId();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("id", messageID);
        messageMap.put("message", "Hi");
        messageMap.put("senderID", user.getUid());
        messageMap.put("time", FieldValue.serverTimestamp());

        messageRef.document(messageID).set(messageMap);

        new Handler().postDelayed(() -> {

            alertDialog.dismissWithAnimation();

            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("uid", userUID);
            intent.putExtra("id", pushID);
            startActivity(intent);

        }, 3000);
    }

    private void queryChat() {
        assert getContext() != null;
        StylishAlertDialog alertDialog = new StylishAlertDialog(getContext(), StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Starting Chat...");
        alertDialog.setCancellable(false);
        alertDialog.show();

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        Log.d("USERID_userID: ", userUID);
        Log.d("USERID_user.getUID: ", user.getUid());
        Log.d("USERID_reference: ", reference.document().getId());
        reference.whereArrayContains("uid", userUID)
                .get().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        QuerySnapshot snapshot = task.getResult();

                        // Danh sách kết quả sau khi lọc cả 2 UID
                        List<DocumentSnapshot> validChats = new ArrayList<>();

                        // Lọc kết quả đảm bảo cả userUID và user.getUid() đều nằm trong mảng uid
                        for (DocumentSnapshot doc : snapshot) {
                            List<String> uidList = (List<String>) doc.get("uid");
                            if (uidList != null && uidList.contains(user.getUid())) {
                                validChats.add(doc);
                            }
                        }

                        if (validChats.isEmpty()) {
                            // Nếu không tìm thấy cuộc trò chuyện nào, tạo cuộc trò chuyện mới
                            StartChat(alertDialog);
                        } else {
                            // Nếu đã tìm thấy, lấy chatId và chuyển sang màn hình ChatActivity
                            alertDialog.dismissWithAnimation();
                            for (DocumentSnapshot validChat : validChats) {
                                Intent intent = new Intent(getActivity(), ChatActivity.class);
                                intent.putExtra("uid", userUID);
                                intent.putExtra("id", validChat.getId()); // Đặt chatId
                                startActivity(intent);
                            }
                        }

                    } else {
                        // Xử lý lỗi nếu truy vấn thất bại
                        alertDialog.dismissWithAnimation();
                    }

                });
    }

    private void loadBasicData() {

        userRef.addSnapshotListener((value, error) -> {
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

                followersList = (List<String>) value.get("followers");
                followingList = (List<String>) value.get("following");

                followersCountTv.setText("" + followersList.size());
                followingCountTv.setText("" + followingList.size());


                try {


                    Glide.with(requireContext().getApplicationContext())
                            .load(profileURL)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .timeout(6500)
                            .into(profileImage);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (followersList.contains(user.getUid())) {
                    followBtn.setText("Unfollow");
                    isFollowed = true;
                    startChatBtn.setVisibility(View.VISIBLE);


                } else {
                    isFollowed = false;
                    followBtn.setText("Follow");
                    startChatBtn.setVisibility(View.GONE);
                }
            }


        });

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
        startChatBtn = view.findViewById(R.id.startChatBtn);
        logoutBtn = view.findViewById(R.id.logoutBtn);
    }

    private void loadPostImages() {
        if(userUID == null)
        {
            Log.e("Profile", "userUID is null in loadPostImages");
            return;
        }
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
                Glide.with(holder.imageView.getContext().getApplicationContext())
                        .load(model.getImageUrl())
                        .timeout(6500)
                        .into(holder.imageView);
                count = getItemCount();
                postCountTv.setText("" + count);
            }

            @Override
             public int getItemCount() {
                return super.getItemCount();
            }
        };
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
        if (adapter != null) {
            adapter.startListening();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }



    private void uploadImage(Uri uri) {
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("Profile Images")
                .child(user.getUid())  // Tạo thư mục riêng cho mỗi người dùng
                .child("profile_image.jpg");  // Tên tệp cố định cho ảnh đại diện

        // Tải ảnh lên Firebase Storage
        reference.putFile(uri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Lấy URL của ảnh đã tải lên
                        reference.getDownloadUrl()
                                .addOnSuccessListener(uri1 -> {
                                    String newProfileImageUrl = uri1.toString();

                                    // Cập nhật ảnh đại diện trong Firestore (Users collection)
                                    updateUserProfileImage(newProfileImageUrl);
                                })
                                .addOnFailureListener(e -> {
                                    // Lỗi khi lấy URL tải về của ảnh
                                    Toast.makeText(getContext(),
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        // Lỗi khi tải ảnh lên Firebase Storage
                        assert task.getException() != null;
                        Toast.makeText(getContext(),
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserProfileImage(String newProfileImageUrl) {
        // Cập nhật ảnh đại diện trong Users collection
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("profileImage", newProfileImageUrl);

        FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid())
                .update(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sau khi cập nhật ảnh đại diện thành công, tiếp tục cập nhật các bài đăng
                        updatePostImagesProfileImage(newProfileImageUrl);
                    } else {
                        // Lỗi khi cập nhật ảnh trong Users collection
                        Toast.makeText(getContext(),
                                "Error updating user profile image: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePostImagesProfileImage(String newProfileImageUrl) {
        // Tạo map chứa dữ liệu sẽ cập nhật
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("profileImage", newProfileImageUrl);

        // Lấy reference đến subcollection "Post Images" của user
        FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid())  // Lấy tài liệu của người dùng dựa trên UID
                .collection("Post Images")  // Truy cập vào subcollection "Post Images"
                .get()  // Lấy tất cả tài liệu trong subcollection
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Duyệt qua từng bài đăng và cập nhật profileImage
                        for (DocumentSnapshot document : task.getResult()) {
                            // Cập nhật từng bài đăng với profileImage mới
                            document.getReference().update(postMap)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Log.d("ProfileImageUpdate", "Post profileImage updated successfully.");
                                        } else {
                                            Log.e("ProfileImageUpdate", "Error updating post profileImage: " + task1.getException().getMessage());
                                        }
                                    });
                        }


                    } else {
                        // Lỗi khi lấy bài đăng
                        Toast.makeText(getContext(),
                                "Error getting posts: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        updateCommentProfileImage(newProfileImageUrl);
    }

    private void updateCommentProfileImage(String newProfileImageUrl) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("profileImageUrl", newProfileImageUrl);

        FirebaseFirestore.getInstance().collection("Users")
                .whereNotEqualTo("uid", user.getUid())  // Lấy tất cả users khác user hiện tại
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Duyệt qua từng user
                        for (DocumentSnapshot userDoc : task.getResult()) {
                            // Truy cập collection Post Images của user đó
                            userDoc.getReference().collection("Post Images")
                                    .get()
                                    .addOnCompleteListener(postTask -> {
                                        if (postTask.isSuccessful()) {
                                            // Duyệt qua từng post
                                            for (DocumentSnapshot postDoc : postTask.getResult()) {
                                                // Truy cập collection Comments của post đó
                                                postDoc.getReference().collection("Comments")
                                                        .whereEqualTo("uid", user.getUid())  // Lọc comments của user hiện tại
                                                        .get()
                                                        .addOnCompleteListener(commentTask -> {
                                                            if (commentTask.isSuccessful()) {
                                                                // Cập nhật từng comment
                                                                for (DocumentSnapshot commentDoc : commentTask.getResult()) {
                                                                    commentDoc.getReference().update(updateMap)
                                                                            .addOnSuccessListener(aVoid ->
                                                                                    Log.d("ProfileUpdate", "Comment updated in post: " + postDoc.getId()))
                                                                            .addOnFailureListener(e ->
                                                                                    Log.e("ProfileUpdate", "Error updating comment: " + e.getMessage()));
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Error getting users: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }





    void createNotification() {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Notifications");
        String id = reference.document().getId();
        Map<String, Object> map = new HashMap<>();
        map.put("time", FieldValue.serverTimestamp());
        map.put("notification", user.getDisplayName() + " followed you.");
        map.put("id", id);
        map.put("uid", userUID);
        map.put("followerId", user.getUid());
        reference.document(id).set(map);
    }
}






