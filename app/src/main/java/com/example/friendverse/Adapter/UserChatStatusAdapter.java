package com.example.friendverse.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ItemUserChatStatusBinding;
import com.example.friendverse.listeners.ConversionListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserChatStatusAdapter extends RecyclerView.Adapter<UserChatStatusAdapter.UserChatStatusViewHolder> {

    private List<User> activityUserList;
    private ConversionListener conversionListener;

    public UserChatStatusAdapter(List<User> activityUserList, ConversionListener conversionListener) {
        this.activityUserList = activityUserList;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public UserChatStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserChatStatusBinding itemUserChatStatusBinding= ItemUserChatStatusBinding.inflate(
               LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserChatStatusViewHolder(itemUserChatStatusBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserChatStatusViewHolder holder, int position) {
        User user = activityUserList.get(position);
        holder.itemUserChatStatusBinding.chatUsername.setText(user.getUsername());
        Picasso.get().load(user.getImageurl()).placeholder(R.drawable.default_avatar).into(holder.itemUserChatStatusBinding.chatUserImage);
        holder.itemUserChatStatusBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversionListener.onConversionClicked(user);
            }
        });

    }

    @Override
    public int getItemCount() {
        return activityUserList.size();
    }

    public class UserChatStatusViewHolder extends RecyclerView.ViewHolder{
        private ItemUserChatStatusBinding itemUserChatStatusBinding;

        public UserChatStatusViewHolder(ItemUserChatStatusBinding itemUserChatStatusBinding) {
            super(itemUserChatStatusBinding.getRoot());
            this.itemUserChatStatusBinding = itemUserChatStatusBinding;
        }

    }

}
