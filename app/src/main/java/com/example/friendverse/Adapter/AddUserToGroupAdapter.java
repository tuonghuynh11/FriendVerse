package com.example.friendverse.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.databinding.ItemContainerAddMemberBinding;
import com.example.friendverse.databinding.ItemContainerSendMessageBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AddUserToGroupAdapter extends RecyclerView.Adapter<AddUserToGroupAdapter.AddUserToGroupViewHolder> {

    private List<User> userList;
    private List<User> userListDefault;

    public AddUserToGroupAdapter(List<User> userList, List<User> userListDefault) {
        this.userList = userList;
        this.userListDefault = userListDefault;
    }

    @NonNull
    @Override
    public AddUserToGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AddUserToGroupViewHolder(ItemContainerAddMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false));
    }

    @Override
    public void onBindViewHolder(@NonNull AddUserToGroupViewHolder holder, int position) {
        User user = userList.get(position);
        holder.itemContainerAddMemberBinding.textName.setText(user.getUsername());
        Picasso.get().load(user.getImageurl()).placeholder(R.drawable.default_avatar).into(holder.itemContainerAddMemberBinding.imageProfile);
        holder.itemContainerAddMemberBinding.checkBox.setChecked(user.isSelected());
        holder.itemContainerAddMemberBinding.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.itemContainerAddMemberBinding.checkBox.isChecked()) {
                    user.setSelected(true);
                } else {
                    user.setSelected(false);
                }
                for (User user1 : userListDefault) {
                    if (user1.getId().equals(user.getId())) {
                        user1.setSelected(user.isSelected());
                        break;
                    }
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    public class AddUserToGroupViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerAddMemberBinding itemContainerAddMemberBinding;

        public AddUserToGroupViewHolder(ItemContainerAddMemberBinding binding) {
            super(binding.getRoot());
            this.itemContainerAddMemberBinding = binding;
        }
    }
}
