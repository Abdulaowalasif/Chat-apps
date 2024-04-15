package com.example.chatapps.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapps.Auth.FirebaseInstance;
import com.example.chatapps.R;
import com.example.chatapps.databinding.ActivitySiginInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SiginIn extends AppCompatActivity {
    ActivitySiginInBinding binding;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySiginInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dialog = new ProgressDialog(this);
        dialog.setMessage("Logging in..");
        dialog.setCancelable(false);

        if (FirebaseInstance.auth.getCurrentUser()!=null){
            startActivity(new Intent(SiginIn.this, MainActivity.class));
            finish();
        }

        binding.goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(SiginIn.this, SignUp.class));
        });
        binding.userLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                String email = binding.userEmail.getText().toString();
                String pass = binding.userPassword.getText().toString();
                if (email.isEmpty()){
                    binding.userEmail.setError("enter your email");
                } else if (pass.isEmpty()) {
                    binding.userPassword.setError("enter your password");
                }else {
                    FirebaseInstance.auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                startActivity(new Intent(SiginIn.this, MainActivity.class));
                                finishAffinity();
                            }else dialog.dismiss();
                        }
                    });
                }
            }
        });
    }
}