package com.example.chatapps.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapps.Activities.ChatActivity;
import com.example.chatapps.Auth.Utils;
import com.example.chatapps.Models.Users;
import com.example.chatapps.R;
import com.example.chatapps.databinding.AvailableUsersBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecycleViewHomeAdapter extends RecyclerView.Adapter<RecycleViewHomeAdapter.ViewHolder> {
    Context context;
    ArrayList<Users> usersList;

    public RecycleViewHomeAdapter(Context context, ArrayList<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.available_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = usersList.get(position);

        String senderUid = Utils.auth.getUid();
        String senderRoom = senderUid + users.getUid();

        Utils.databaseReference.child("Chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                    Long lastMsgTime = snapshot.child("lastMsgTime").getValue(Long.class);

                    SimpleDateFormat sdf = new SimpleDateFormat(" hh:mm a");
                    // Convert the timestamp to a Date object
                    Date currentDate = new Date(lastMsgTime);

                    // Format the Date object to the desired string representation
                    String formattedDate = sdf.format(currentDate);

                    holder.binding.lastMsg.setText(lastMsg);
                    holder.binding.time.setText(formattedDate);
                    holder.binding.time.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.binding.userName.setText(users.getUsername());
        if (users.getImage().equals("No images")) {
            holder.binding.profile.setImageResource(R.drawable.ic_launcher_background);
        } else {
            Glide.with(context).load(users.getImage())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.binding.profile);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", users.getUsername());
                intent.putExtra("image", users.getImage());
                intent.putExtra("uid", users.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        AvailableUsersBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = AvailableUsersBinding.bind(itemView);
        }
    }

    ;
}
