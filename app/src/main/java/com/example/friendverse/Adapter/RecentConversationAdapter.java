package com.example.friendverse.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Model.ChatMessage;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ItemContainerRecentConversionBinding;
import com.example.friendverse.listeners.ConversionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.RecentMessageViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;
    private final List<User> users;

    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener, List<User> users) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
        this.users=users;
    }

    @NonNull
    @Override
    public RecentMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecentMessageViewHolder(ItemContainerRecentConversionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecentMessageViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    public class RecentMessageViewHolder extends RecyclerView.ViewHolder {
        ItemContainerRecentConversionBinding binding;

        public RecentMessageViewHolder(ItemContainerRecentConversionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(ChatMessage chatMessage) {
            FirebaseUser firebaseUser  = FirebaseAuth.getInstance().getCurrentUser();
            String currentId= firebaseUser.getUid();
            binding.textName.setText(chatMessage.getConversionName());
            Picasso.get().load(chatMessage.getConversionImage()).placeholder(R.drawable.default_avatar).into(binding.imageProfile);
            binding.textRecentMessage.setText(chatMessage.getMessage());
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user =new User();

                    for (User u:users){
                        if (u.getId().equals(chatMessage.getReceiverId())&& currentId.equals(chatMessage.getSenderId())){
                            user.setUsername(u.getUsername());
                            user.setUsername(u.getUsername());
                            user=u;
                            break;
                        }
                        if (u.getId().equals(chatMessage.getSenderId())&&currentId.equals(chatMessage.getReceiverId())){
                            user.setUsername(u.getUsername());
                            user.setUsername(u.getUsername());
                            user=u;
                            break;
                        }

                    }

                    user.setId(chatMessage.getConversionId());
                    if (user.getId().contains("group")){
                        user.setUsername(chatMessage.getConversionName());
                        user.setEmail(chatMessage.getConversionIdentify());
                    }
                    user.setFullname(chatMessage.getConversionName());
                    user.setImageurl(chatMessage.getConversionImage());
                    conversionListener.onConversionClicked(user);
                }
            });
        }
    }
}
