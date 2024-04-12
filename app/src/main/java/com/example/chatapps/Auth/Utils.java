package com.example.chatapps.Auth;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Utils {
    public static final FirebaseAuth auth=FirebaseAuth.getInstance();
    private static final FirebaseDatabase database=FirebaseDatabase.getInstance();
    private static final FirebaseStorage storage=FirebaseStorage.getInstance();
    public static final DatabaseReference databaseReference=database.getReference();
    public static final StorageReference storageReference= storage.getReference();


}
