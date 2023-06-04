package com.example.friendverse.Adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.hendraanggrian.appcompat.widget.SocialTextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.friendverse.Fragment.PostDetailFragment;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.Model.Notification;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{

    private Context mContext;
    private List<Notification> mNotifications;

    public NotificationAdapter(Context mContext, List<Notification> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item , parent , false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Notification notification = mNotifications.get(position);
        holder.text.setText(notification.getText());

        getUserInfo(holder.image_profile , holder.username , notification.getUserid());

        if (notification.isIspost()){
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image , notification.getPostid());
        } else {
            holder.post_image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notification.isIspost()){
                    Bundle passData = new Bundle();
                    passData.putString("postid", notification.getPostid());
                    Fragment profileFragment  = new PostDetailFragment();
                    profileFragment .setArguments(passData);
                    FragmentManager fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, profileFragment ).addToBackStack(null);
                    fragmentTransaction.commit();
                } else {

                    Bundle passData = new Bundle();
                    passData.putString("profileid", notification.getUserid());
                    Fragment profileFragment  = new ProfileFragment();
                    profileFragment .setArguments(passData);
                    FragmentManager fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, profileFragment ).addToBackStack(null);
                    fragmentTransaction.commit();
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView image_profile;
        public ImageView post_image;
        public TextView username;
        public TextView text;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            username = itemView.findViewById(R.id.username);
            text = itemView.findViewById(R.id.comment);
        }
    }

    private void getUserInfo(final ImageView imageView , final TextView username , String publisherid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.get().load(user.getImageurl()).placeholder(R.drawable.ic_profile).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getPostImage (final ImageView imageView , String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                assert post != null;
                Picasso.get().load(post.getPostimage()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}