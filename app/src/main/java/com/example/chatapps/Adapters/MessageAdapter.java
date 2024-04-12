package com.example.chatapps.Adapters;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapps.Auth.FirebaseInstance;
import com.example.chatapps.Models.Message;
import com.example.chatapps.R;
import com.example.chatapps.databinding.MsgReceiveLayoutBinding;
import com.example.chatapps.databinding.MsgSentLayoutBinding;

import java.io.File;
import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final ArrayList<Message> messages;
    private final int SENDER_VIEW = 1;
    private final int RECEIVER_VIEW = 2;

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (viewType == SENDER_VIEW) {
            View view = layoutInflater.inflate(R.layout.msg_sent_layout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.msg_receive_layout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);

        if (holder instanceof SenderViewHolder) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            bindMessageData(viewHolder, msg);


        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            bindMessageData(viewHolder, msg);
            viewHolder.binding.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri imageUri = Uri.parse(msg.getImageUri());

                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(imageUri);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setTitle("Downloading image");

                    // Choose a destination folder for the downloaded file
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Downloaded_images");
                    request.setDestinationUri(Uri.fromFile(file));

                    // Enqueue the download
                    downloadManager.enqueue(request);
                }
            });
        }
    }

    private void bindMessageData(RecyclerView.ViewHolder holder, Message msg) {
        if (msg.getMessage().equals("photo")) {
            // If the message is a photo, show the image and hide the text message
            if (holder instanceof SenderViewHolder) {
                SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                senderViewHolder.binding.image.setVisibility(View.VISIBLE);
                senderViewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(msg.getImageUri())
                        .placeholder(R.drawable.user)
                        .into(senderViewHolder.binding.image);
            } else if (holder instanceof ReceiverViewHolder) {
                ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
                receiverViewHolder.binding.image.setVisibility(View.VISIBLE);
                receiverViewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(msg.getImageUri())
                        .placeholder(R.drawable.user)
                        .into(receiverViewHolder.binding.image);
            }
        } else {
            // If it's a text message, show the text and hide the image
            if (holder instanceof SenderViewHolder) {
                SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                senderViewHolder.binding.image.setVisibility(View.GONE);
                senderViewHolder.binding.message.setVisibility(View.VISIBLE);
                senderViewHolder.binding.message.setText(msg.getMessage());
            } else if (holder instanceof ReceiverViewHolder) {
                ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
                receiverViewHolder.binding.image.setVisibility(View.GONE);
                receiverViewHolder.binding.message.setVisibility(View.VISIBLE);
                receiverViewHolder.binding.message.setText(msg.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (FirebaseInstance.auth.getUid().equals(message.getSenderId())) {
            return SENDER_VIEW;
        } else {
            return RECEIVER_VIEW;
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        MsgReceiveLayoutBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = MsgReceiveLayoutBinding.bind(itemView);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        MsgSentLayoutBinding binding;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = MsgSentLayoutBinding.bind(itemView);
        }
    }
}
