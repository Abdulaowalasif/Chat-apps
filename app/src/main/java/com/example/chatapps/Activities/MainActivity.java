package com.example.chatapps.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapps.Adapters.RecycleViewHomeAdapter;
import com.example.chatapps.Auth.Utils;
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
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

        users=new ArrayList<>();
        adapter=new RecycleViewHomeAdapter(this,users);
        binding.rcHome.setAdapter(adapter);

        Utils.databaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               users.clear();
                for (DataSnapshot sp:snapshot.getChildren()){
                    Users users1=sp.getValue(Users.class);
                    if (!Utils.auth.getCurrentUser().getEmail().equals(users1.getEmail())){
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
        getMenuInflater().inflate(R.menu.top_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Utils.auth.signOut();
        startActivity(new Intent(MainActivity.this, SiginIn.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}