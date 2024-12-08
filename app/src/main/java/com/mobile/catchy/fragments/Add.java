package com.mobile.catchy.fragments;





import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;

import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mobile.catchy.R;
import com.mobile.catchy.adapter.GalleryAdapter;
import com.mobile.catchy.model.GalleryImages;


import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Add extends Fragment {

    private EditText descET;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ImageButton backBtn, nextBtn;
    private List<GalleryImages> list;
    private GalleryAdapter adapter;
    private FirebaseUser user;
    private Dialog dialog;
    Uri imageUri;
    String profileImageUrl;
    private boolean permissionsChecked = false;
    private DocumentReference documentReference;
    public Add() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
        descET.setText("");
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);

        list = new ArrayList<>();
        adapter = new GalleryAdapter(list);

        recyclerView.setAdapter(adapter);

        clickListeners();


    }


    private void clickListeners(){
        adapter.SendImage(picUri ->

                {
                    CropImageOptions options = new CropImageOptions();

                    options.guidelines = CropImageView.Guidelines.ON;
                    options.aspectRatioX = 4;
                    options.aspectRatioY = 3;


                    cropLauncher.launch(new CropImageContractOptions(picUri, options));


                }


        );

        nextBtn.setOnClickListener(view -> {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference().child("Post Images/"+System.currentTimeMillis());
            dialog.show();
            storageReference.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                uploadData(uri.toString(), profileImageUrl);
                            });
                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Failed to upload post", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void uploadData(String imageUrl, String profileImage) {
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("Post Images");

        String id = reference.document().getId();

        String description = descET.getText().toString();

        List<String> list = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("description", description);
        map.put("imageUrl", imageUrl);
        map.put("timestamp", FieldValue.serverTimestamp());

        map.put("name", user.getDisplayName());
        map.put("profileImage", profileImage);
        map.put("likes", list);
        //map.put("comments",list);
        map.put("uid", user.getUid());

        reference.document(id).set(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                descET.setText("");
                dialog.dismiss();
                //return home
            } else {
                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();


        });


    }


    private void init(View view) {

        descET = view.findViewById(R.id.descriptionET);
        imageView = view.findViewById(R.id.imageView);
        recyclerView = view.findViewById(R.id.recyclerView);
        backBtn = view.findViewById(R.id.backBtn);
        nextBtn = view.findViewById(R.id.nextBtn);

        user = FirebaseAuth.getInstance().getCurrentUser();
        documentReference = FirebaseFirestore.getInstance().collection("Users").document(user.getUid());
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                profileImageUrl = task.getResult().getString("profileImage");
            }
        });



        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dialog_bg, null));
        dialog.setCancelable(false);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!permissionsChecked) {


            requireActivity().runOnUiThread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
                @Override
                public void run() {
                    Dexter.withContext(getContext())
                            .withPermissions(
                                    // Thay thế quyền cũ bằng quyền mới tùy theo mục đích của bạn
                                    android.Manifest.permission.READ_MEDIA_IMAGES,
                                    android.Manifest.permission.READ_MEDIA_VIDEO,
                                    android.Manifest.permission.READ_MEDIA_AUDIO,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport report) {
                                    if (list.isEmpty()) {
                                        loadImagesFromMediaStore();
                                    }
                                    permissionsChecked = true;
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                                    // Hiển thị lý do cần thiết cho người dùng nếu họ từ chối quyền
                                    permissionToken.continuePermissionRequest();
                                }
                            }).check();
                }
            });
        }
    }


        ActivityResultLauncher<CropImageContractOptions> cropLauncher = registerForActivityResult(new CropImageContract(), result -> {


            if (result.isSuccessful()) {
                imageUri = result.getUriContent();

                Glide.with(Add.this).load(imageUri).into(imageView);

                imageView.setVisibility(View.VISIBLE);
                nextBtn.setVisibility(View.VISIBLE);

            }

        });

    private void loadImagesFromMediaStore() {
        // Truy vấn MediaStore để lấy ảnh
        ContentResolver contentResolver = requireContext().getContentResolver();
        Uri collection;

        // Sử dụng MediaStore để lấy URI của ảnh tùy vào phiên bản Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        // Cột mà chúng ta muốn truy xuất
        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
        };


        String selection = "(" + MediaStore.Images.Media.MIME_TYPE + "=? OR " +
                MediaStore.Images.Media.MIME_TYPE + "=?) AND " +
                MediaStore.Images.Media.RELATIVE_PATH + " LIKE ?";
        String[] selectionArgs = new String[]{"image/jpeg", "image/png"};//, "%AndroidNhat2%"};

        try (Cursor cursor = contentResolver.query(
                collection,
                projection,
//                selection,
//                selectionArgs,
                null,
                null,
                null)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                    // Thêm ảnh vào danh sách và thông báo adapter
                    list.add(new GalleryImages(contentUri));
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}