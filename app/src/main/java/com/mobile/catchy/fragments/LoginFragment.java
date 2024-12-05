package com.mobile.catchy.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.catchy.MainActivity;
import com.mobile.catchy.R;
import com.mobile.catchy.ReplacerActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class LoginFragment extends Fragment {
    //almost done
    private EditText emaiET, passwordET;
    private TextView signUpTV, forgotPasswordTV;
    private Button loginBtn, googleSignInBtn;
    private ProgressBar progressBar;
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";
    private FirebaseAuth auth;
    public static final int RC_SIGN_IN = 1;
    GoogleSignInClient mGoogleSignInClient;

    public LoginFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        clickListeners();

    }

    private void init(View view) {
        emaiET = view.findViewById(R.id.emailET);
        passwordET = view.findViewById(R.id.passwordET);
        signUpTV = view.findViewById(R.id.signUpTV);
        forgotPasswordTV = view.findViewById(R.id.forgotTV);
        loginBtn = view.findViewById(R.id.loginBtn);
        googleSignInBtn = view.findViewById(R.id.googleSignInBtn);
        progressBar = view.findViewById(R.id.progressBar);


        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }


    private void clickListeners() {

        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplacerActivity) getActivity()).setFragment(new ForgotPassword());
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emaiET.getText().toString();
                String password = passwordET.getText().toString();

                if (email.isEmpty() || !email.matches(EMAIL_REGEX)) {
                    emaiET.setError("Please input valid email");
                    return;
                }

                if(password.isEmpty() || password.length() < 6) {
                    passwordET.setError("Please input valid password");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                       FirebaseUser user = auth.getCurrentUser();
                          if(!user.isEmailVerified()) {
                              Toast.makeText(getActivity(), "Please verify your email", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                          }
                          FirebaseFirestore db = FirebaseFirestore.getInstance();
                          db.collection("Users").document(user.getUid()).update("status", "Online");

                          sendUserToMainActivity();

                    } else {
                        Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signUpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplacerActivity) getActivity()).setFragment(new CreateAccountFragment());
            }
        });
    }

    private void sendUserToMainActivity() {
        if(getActivity() == null) return;
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getContext().getApplicationContext(), MainActivity.class));
        getActivity().finish();
    }



    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kiểm tra kết quả từ Intent Google Sign-In
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign-In thành công, lấy tài khoản và tiếp tục xác thực với Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                // Gọi hàm xác thực Firebase với token của Google
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign-In thất bại, xử lý UI
                e.printStackTrace();
            }
        }
    }

    // Hàm xác thực với Firebase bằng Google ID Token
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            FirebaseUser user = auth.getCurrentUser();
                            // Thực hiện các hành động tiếp theo
                            updateUI(user);
                        } else {
                            // Đăng nhập thất bại
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            // Thực hiện xử lý UI cho trường hợp thất bại
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());

        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        reference.get().addOnSuccessListener(documentSnapshot -> {
            List<Object> followersList;
            List<Object> followingList;
            String profileImage = "";
            Map<String, Object> map = new HashMap<>();
            // Kiểm tra nếu followers và following đã tồn tại thì lấy giá trị hiện tại
            if (documentSnapshot.exists()) {
                followersList = (List<Object>) documentSnapshot.get("followers");
                followingList = (List<Object>) documentSnapshot.get("following");
                if(documentSnapshot.get("profileImage") != null)
                    profileImage = (String) documentSnapshot.get("profileImage");
            } else {
                followersList = new ArrayList<>(); // Nếu không tồn tại, tạo danh sách rỗng
                followingList = new ArrayList<>();
                profileImage = String.valueOf(account.getPhotoUrl());
            }

            // Tạo map với dữ liệu mới, không thay đổi followers và following nếu đã có
            map.put("name", account.getDisplayName());
            map.put("email", account.getEmail());
            map.put("profileImage", profileImage);
            map.put("uid", user.getUid());
            map.put("followers", followersList); // Giữ nguyên danh sách hiện tại nếu có
            map.put("following", followingList);
            map.put("status", " ");
            map.put("search", account.getDisplayName().toLowerCase());

            // Cập nhật Firestore
            reference.set(map)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            assert getActivity() != null;
                            progressBar.setVisibility(View.GONE);
                            sendUserToMainActivity();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Failed to retrieve data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

}