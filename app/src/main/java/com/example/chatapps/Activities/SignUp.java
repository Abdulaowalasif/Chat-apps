package com.example.chatapps.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapps.Auth.Utils;
import com.example.chatapps.Models.Users;
import com.example.chatapps.R;
import com.example.chatapps.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.storage.UploadTask;

public class SignUp extends AppCompatActivity {
    ActivitySignUpBinding binding;
    Uri selectedImageUri;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dialog = new ProgressDialog(this);
        dialog.setMessage("Creating profile....");
        dialog.setCancelable(false);

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        binding.userRegisterBtn.setOnClickListener(v -> {
            dialog.show();
            String email = binding.userEmail.getText().toString();
            String pass = binding.userPassword.getText().toString();
            String name = binding.userName.getText().toString();
            if (email.isEmpty()) {
                binding.userEmail.setError("Enter your email");
            } else if (pass.isEmpty()) {
                binding.userPassword.setError("Enter your password");
            } else if (name.isEmpty()) {
                binding.userName.setError("Enter your name");
            } else {
                Utils.auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(SignUp.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    dialog.dismiss();
                                    if (selectedImageUri != null) {
                                        Utils.storageReference.child("Profile pictures").child(Utils.auth.getUid())
                                                .putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            Utils.storageReference.child("Profile pictures").child(Utils.auth.getUid())
                                                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                        @Override
                                                                        public void onSuccess(Uri uri) {
                                                                            Users users = new Users(name, email, String.valueOf(uri), String.valueOf(Utils.auth.getUid()));
                                                                            Utils.databaseReference.child("Users").child(Utils.auth.getUid())
                                                                                    .setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    } else {
                                        Users users = new Users(name, email, "No images", String.valueOf(Utils.auth.getUid()));
                                        Utils.databaseReference.child("Users").child(Utils.auth.getUid())
                                                .setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                    }
                                                });
                                    }

                                }else dialog.dismiss();
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getData() != null) {
                binding.profileImage.setImageURI(data.getData());
                selectedImageUri = data.getData();
            }
        }
    }
}