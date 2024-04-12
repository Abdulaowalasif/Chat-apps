package com.example.chatapps.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatapps.Adapters.MessageAdapter;
import com.example.chatapps.Auth.FirebaseInstance;
import com.example.chatapps.Models.Message;
import com.example.chatapps.R;
import com.example.chatapps.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                HashMap<String , Object> map=new HashMap<>();
                map.put("token",s);
                FirebaseInstance.databaseReference.child("Users")
                        .child(FirebaseInstance.auth.getUid())
                        .updateChildren(map);
            }
        });

        binding.msgBox.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.msgBox.getWindowToken(), 0);
                    binding.send.performClick();
                    return true;
                }
                return false;
            }
        });


        String name = getIntent().getStringExtra("name");
        String token = getIntent().getStringExtra("token");
        String image = getIntent().getStringExtra("image");
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseInstance.auth.getUid();
        binding.toolbarUsername.setText(name);

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;


        FirebaseInstance.databaseReference.child("Chats")
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
                        binding.chatsRc.scrollToPosition(messages.size() - 1);
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
                    String randomKey = FirebaseInstance.databaseReference.push().getKey();
                    binding.msgBox.setText("");
                    Date date = new Date();
                    Message message = new Message(senderUid, msg, date.getTime());

                    FirebaseInstance.databaseReference.child("Chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    FirebaseInstance.databaseReference.child("Chats")
                                            .child(receiverRoom)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    sendNotification(name, message.getMessage(),token);
                                                }
                                            });
                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                                    lastMsgObj.put("lastMsg", message.getMessage());
                                    lastMsgObj.put("lastMsgTime", date.getTime());
                                    FirebaseInstance.databaseReference.child("Chats").child(senderRoom).updateChildren(lastMsgObj);
                                    FirebaseInstance.databaseReference.child("Chats").child(receiverRoom).updateChildren(lastMsgObj);
                                }
                            });
                }
            }
        });
        final Handler handler=new Handler();
        binding.msgBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                FirebaseInstance.databaseReference.child("Presence").child(senderUid).setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStopped,1000);
                }
            final Runnable userStopped=new Runnable() {
                @Override
                public void run() {
                    FirebaseInstance.databaseReference.child("Presence").child(senderUid).setValue("Online");
                }
            };
        });
        FirebaseInstance.databaseReference.child("Presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String presence = snapshot.getValue(String.class);
                    binding.onlineStatus.setText(presence);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    public void sendNotification(String title,String body,String token){
        try{

            RequestQueue queue= Volley.newRequestQueue(this);
            String url ="https://fcm.googleapis.com/fcm/send";

            JSONObject object=new JSONObject();
            object.put("title",title);
            object.put("body",body);

            JSONObject notificationData=new JSONObject();
            notificationData.put("notification",object);
            notificationData.put("to",token);

            JsonObjectRequest request=new JsonObjectRequest(url, notificationData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String,String> map=new HashMap<>();
                    String key="key=AAAAddsQsWw:APA91bGN-NaO2Er8Dte_gQGJbHhpZFCvFt0F7-vrMSM1duMItezkBJgTph6Rlm2nKw0pkK1yPa_gONf0pphwOgSS_37XYi5eQGQpnlEx78ukZf9w111nABcQJwMk6FDeFnnN_8Zt0d-C";
                    map.put("Authorization",key);
                    map.put("Content-Type","application/json");

                    return map;
                }
            };

            queue.add(request);

        }catch (Exception e){

        }

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
                FirebaseInstance.storageReference.child("chats").child(filename).putFile(selectedImageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // If upload is successful, get the download URL of the uploaded image
                                    FirebaseInstance.storageReference.child("chats").child(filename).getDownloadUrl()
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
                                                    String randomKey = FirebaseInstance.databaseReference.push().getKey();

                                                    // Update last message and time for sender and receiver rooms
                                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                                                    lastMsgObj.put("lastMsg", message.getMessage());
                                                    lastMsgObj.put("lastMsgTime", date.getTime());
                                                    FirebaseInstance.databaseReference.child("Chats").child(senderRoom).updateChildren(lastMsgObj);
                                                    FirebaseInstance.databaseReference.child("Chats").child(receiverRoom).updateChildren(lastMsgObj);

                                                    // Set the message in sender's and receiver's message nodes
                                                    FirebaseInstance.databaseReference.child("Chats").child(senderRoom).child("messages").child(randomKey).setValue(message)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    FirebaseInstance.databaseReference.child("Chats").child(receiverRoom).child("messages").child(randomKey).setValue(message)
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