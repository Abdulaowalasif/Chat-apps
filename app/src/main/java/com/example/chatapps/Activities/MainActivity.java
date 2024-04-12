package com.example.chatapps.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapps.Adapters.RecycleViewHomeAdapter;
import com.example.chatapps.Auth.FirebaseInstance;
import com.example.chatapps.Models.Users;
import com.example.chatapps.R;
import com.example.chatapps.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<Users> users;
    RecycleViewHomeAdapter adapter;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        users = new ArrayList<>();
        adapter = new RecycleViewHomeAdapter(this, users);
        binding.rcHome.setAdapter(adapter);

        FirebaseInstance.databaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot sp : snapshot.getChildren()) {
                    Users users1 = sp.getValue(Users.class);
                    if (!FirebaseInstance.auth.getCurrentUser().getEmail().equals(users1.getEmail())) {
                        users.add(users1);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.logout){
            FirebaseInstance.auth.signOut();
            Intent intent=new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseInstance.databaseReference.child("Presence").child(FirebaseInstance.auth.getUid())
                .setValue("Online");
    }

    @Override
    protected void onPause() {
        FirebaseInstance.databaseReference.child("Presence").child(FirebaseInstance.auth.getUid())
                .setValue("Offline");
        super.onPause();
    }
}