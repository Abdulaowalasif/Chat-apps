package com.example.chatapps.Activities;

import static com.example.chatapps.Auth.FirebaseInstance.*;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapps.R;
import com.example.chatapps.databinding.ActivitySiginInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SignIn extends AppCompatActivity {
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

        binding.goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(SignIn.this, SignUp.class));
        });
        binding.userLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.userEmail.getText().toString();
                String pass = binding.userPassword.getText().toString();
                if (email.isEmpty()) {
                    binding.userEmail.setError("enter your email");
                } else if (pass.isEmpty()) {
                    binding.userPassword.setError("enter your password");
                } else {
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.show();
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                startActivity(new Intent(SignIn.this, MainActivity.class));
                                finish();
                            } else {
                                dialog.dismiss();
                                Toast.makeText(SignIn.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
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
                    binding.userLoginBtn.performClick();
                    return true;
                }
                return false;
            }
        });
    }
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.userPassword.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(binding.userEmail.getWindowToken(), 0);
    }
}