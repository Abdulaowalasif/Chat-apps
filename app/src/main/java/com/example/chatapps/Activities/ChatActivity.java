package com.example.chatapps.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.chatapps.Adapters.MessageAdapter;
import com.example.chatapps.Auth.Utils;
import com.example.chatapps.Models.Message;
import com.example.chatapps.R;
import com.example.chatapps.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessageAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    String senderUid;
    String receiverUid;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messages = new ArrayList<>();
        adapter = new MessageAdapter(this, messages);
        binding.chatsRc.setAdapter(adapter);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");
        binding.goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String name = getIntent().getStringExtra("name");
        binding.toolbarUsername.setText(name);
        String image = getIntent().getStringExtra("image");
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = Utils.auth.getUid();

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;


        Utils.databaseReference.child("Chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Message message = snap.getValue(Message.class);
                            messages.add(message);
                        }
                        binding.chatsRc.scrollToPosition(messages.size()-1);
                        adapter.notifyDataSetChanged();
                       }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.msgBox.getText().toString().isEmpty()) {
                    String msg = binding.msgBox.getText().toString();
                    String randomKey = Utils.databaseReference.push().getKey();

                    Date date = new Date();
                    Message message = new Message(senderUid, msg, date.getTime());

                    Utils.databaseReference.child("Chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Utils.databaseReference.child("Chats")
                                            .child(receiverRoom)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    binding.msgBox.setText("");

                                                }
                                            });
                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                                    lastMsgObj.put("lastMsg", message.getMessage());
                                    lastMsgObj.put("lastMsgTime", date.getTime());
                                    Utils.databaseReference.child("Chats").child(senderRoom).updateChildren(lastMsgObj);
                                    Utils.databaseReference.child("Chats").child(receiverRoom).updateChildren(lastMsgObj);
                                }
                            });
                }
            }
        });

        Glide.with(this).load(image)
                .placeholder(R.drawable.ic_launcher_background)
                .into(binding.toolbarUserImage);


        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1000);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from selecting an image
        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            // Check if data contains the selected image URI
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Generate a unique filename using current time
                String filename = Calendar.getInstance().getTimeInMillis() + "";

                // Upload the selected image to Firebase Storage
                Utils.storageReference.child("chats").child(filename).putFile(selectedImageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // If upload is successful, get the download URL of the uploaded image
                                    Utils.storageReference.child("chats").child(filename).getDownloadUrl()
                                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    // Once download URL is obtained, create a Message object
                                                    String imageUri = uri.toString();
                                                    String messageText = binding.msgBox.getText().toString();
                                                    Date date = new Date();
                                                    Message message = new Message(senderUid, messageText, date.getTime());
                                                    message.setMessage("photo");
                                                    message.setImageUri(imageUri);

                                                    // Clear the message box
                                                    binding.msgBox.setText("");

                                                    // Generate a random key for the message
                                                    String randomKey = Utils.databaseReference.push().getKey();

                                                    // Update last message and time for sender and receiver rooms
                                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                                                    lastMsgObj.put("lastMsg", message.getMessage());
                                                    lastMsgObj.put("lastMsgTime", date.getTime());
                                                    Utils.databaseReference.child("Chats").child(senderRoom).updateChildren(lastMsgObj);
                                                    Utils.databaseReference.child("Chats").child(receiverRoom).updateChildren(lastMsgObj);

                                                    // Set the message in sender's and receiver's message nodes
                                                    Utils.databaseReference.child("Chats").child(senderRoom).child("messages").child(randomKey).setValue(message)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Utils.databaseReference.child("Chats").child(receiverRoom).child("messages").child(randomKey).setValue(message)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    // Both sender's and receiver's messages are set successfully
                                                                                }
                                                                            });
                                                                }
                                                            });
                                                }
                                            });
                                }
                            }
                        });
            }
        }
    }

}