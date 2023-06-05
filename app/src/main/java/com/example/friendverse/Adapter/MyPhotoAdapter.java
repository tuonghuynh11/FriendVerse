package com.example.friendverse.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.friendverse.Fragment.EditProfileFragment;
import com.example.friendverse.Fragment.FollowFragment;
import com.example.friendverse.Fragment.PostDetailFragment;
import com.example.friendverse.Model.Post;
import com.example.friendverse.R;
import com.example.friendverse.databinding.FragmentEditProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyPhotoAdapter extends RecyclerView.Adapter<MyPhotoAdapter.ViewHolder>{
    private Context context;
    private ArrayList<Post> posts;

    public MyPhotoAdapter(Context context, ArrayList<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_item_profile, parent, false);
        return new MyPhotoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);

//        Glide.with(context).load(post.getPostimage()).into(holder.post_image);
        Picasso.get().load(post.getPostimage()).placeholder(R.drawable.app_icon_one).into(holder.post_image);
        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle passData = new Bundle();
                passData.putString("postid", post.getPostid());
                Fragment fragment = new PostDetailFragment();
                fragment.setArguments(passData);
                FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView post_image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            post_image = itemView.findViewById(R.id.post_image);
        }
    }
}
