package com.example.friendverse.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ItemContainerUserBinding;
import com.example.friendverse.listeners.UserListener;
import com.squareup.picasso.Picasso;
import java.util.List;

public class UsersChatAdapter extends RecyclerView.Adapter<UsersChatAdapter.UsersViewHoler> {
    private List<User> users;
    private final UserListener userListener;
    public UsersChatAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UsersViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding= ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UsersViewHoler(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHoler holder, int position) {
        User user= users.get(position);
        holder.binding.textName.setText(user.getUsername());
        holder.binding.textEmail.setText(user.getEmail());
        Picasso.get().load(user.getImageurl()).placeholder(R.drawable.default_avatar).into(holder.binding.imageProfile);
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userListener.onUserClicked(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class UsersViewHoler extends RecyclerView.ViewHolder {
        public ItemContainerUserBinding binding;

        public UsersViewHoler(ItemContainerUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

}
