package com.example.friendverse.Adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.ShareActionProvider;
import android.widget.TextView;

import androidx.annotation.NonNull;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.MainActivity;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends  RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private Context sContext;
    private List<User> sUser;
    private boolean isFragment;
    private int limit = 5;
    private FirebaseUser firebaseUser;
    private OnItemClickListener onItemClickListener;
    public UserAdapter(Context sContext, List<User> sUser) {
        this.sContext = sContext;
        this.sUser = sUser;

    }
    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(sContext).inflate(R.layout.user_item,parent,false);
        return  new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        User user = sUser.get(position);
        holder.btnFollow.setVisibility(View.VISIBLE);

        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getFullname());

        Picasso.get().load(user.getImageurl()).placeholder(R.mipmap.ic_launcher).into(holder.imageProfile);

        isFollowed(user.getId() , holder.btnFollow);

        if (user.getId().equals(firebaseUser.getUid())){
            holder.btnFollow.setVisibility(View.GONE);
            holder.imageProfile.setVisibility(View.GONE);
            holder.username.setVisibility(View.GONE);
            holder.fullname.setVisibility(View.GONE);
        }

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btnFollow.getText().toString().equals(("follow"))){
                    FirebaseDatabase.getInstance().getReference().child("Follow").
                            child((firebaseUser.getUid())).child("following").child(user.getId()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow").
                            child(user.getId()).child("followers").child(firebaseUser.getUid()).setValue(true);

                  addNotifications(user.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").
                            child((firebaseUser.getUid())).child("following").child(user.getId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").
                            child(user.getId()).child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle passData = new Bundle();
                passData.putString("profileid", user.getId());
                Fragment editFragment = new ProfileFragment();
                editFragment.setArguments(passData);
                FragmentManager fragmentManager = ((AppCompatActivity)sContext).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, editFragment).addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

    }



    private void isFollowed(final String id, final Button btnFollow) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                    .child(firebaseUser.getUid()).child("following");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(id).exists())
                        btnFollow.setText("following");
                    else
                        btnFollow.setText("follow");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error gracefully
                }
            });
        } else {
            // Handle the case when the firebaseUser is null
        }
    }


    @Override
    public int getItemCount() {
        if (sUser.size() > limit) {
            return limit;
        } else {
            return sUser.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView imageProfile;
        public TextView username;
        public TextView fullname;
        public Button btnFollow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            btnFollow = itemView.findViewById(R.id.btn_follow);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            User user = sUser.get(position);
                            onItemClickListener.onItemClick(user);
                        }
                    }
                }
            });
        }
    }
    private void addNotifications(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("userid" , firebaseUser.getUid());
        hashMap.put("text" , "started following you");
        hashMap.put("postid" , "");
        hashMap.put("ispost" , false);

        reference.push().setValue(hashMap);
    }
}
