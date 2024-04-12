package com.example.chatapps.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapps.Auth.FirebaseInstance;
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
                FirebaseInstance.auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                dialog.show();
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(SignUp.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    dialog.dismiss();
                                    if (selectedImageUri != null) {
                                        FirebaseInstance.storageReference.child("Profile pictures").child(FirebaseInstance.auth.getUid())
                                                .putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            FirebaseInstance.storageReference.child("Profile pictures").child(FirebaseInstance.auth.getUid())
                                                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                        @Override
                                                                        public void onSuccess(Uri uri) {
                                                                            Users users = new Users(name, email, String.valueOf(uri), String.valueOf(FirebaseInstance.auth.getUid()));
                                                                            FirebaseInstance.databaseReference.child("Users").child(FirebaseInstance.auth.getUid())
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
                                        Users users = new Users(name, email, "No images", String.valueOf(FirebaseInstance.auth.getUid()));
                                        FirebaseInstance.databaseReference.child("Users").child(FirebaseInstance.auth.getUid())
                                                .setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                    }
                                                });
                                    }

                                } else dialog.dismiss();
                            }
                        });
            }
        });


        binding.userName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Hide the keyboard
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
        binding.userEmail.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Hide the keyboard
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
        binding.userPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // Hide the keyboard
                    hideKeyboard();
                    binding.userRegisterBtn.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode ==RESULT_OK && data != null) {
                if (data.getData() != null) {
                    binding.profileImage.setImageURI(data.getData());
                    selectedImageUri = data.getData();
                    binding.add.setVisibility(View.GONE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.userPassword.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(binding.userEmail.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(binding.userName.getWindowToken(), 0);
    }
}