package com.example.friendverse.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Fragment.DetailReportUserFragment;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserReportAllPostAdapter extends  RecyclerView.Adapter<UserReportAllPostAdapter.ViewHolder>{

    private Context sContext;
    private List<Post> sPost;
    private FirebaseUser firebaseUser;
    private OnItemClickListener onItemClickListener;
    private List<String> id_ban;
    private String username;
    private TextView tv_ban;

    View view;
    BottomSheetDialog bottomSheetDialog;
    public UserReportAllPostAdapter(Context sContext, List<Post> sPost) {
        this.sContext = sContext;
        this.sPost = sPost;

    }
    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(sContext).inflate(R.layout.user_item_alluser,parent,false);
        return  new UserReportAllPostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = sPost.get(position);
        id_ban = new ArrayList<>();
        username = "Hello";

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    User user = item.getValue(User.class);
                    if (user.getId().trim().equals(post.getPublisher().trim())) {
                        username = user.getUsername();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.username.setText(username);
        holder.fullname.setText(post.getPostid());

        Picasso.get().load(post.getPostimage()).placeholder(R.mipmap.ic_launcher).into(holder.imageProfile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle passData = new Bundle();
                passData.putString("postid", post.getPostid());
                Fragment detailReportUserFragment = new DetailReportUserFragment();
                detailReportUserFragment.setArguments(passData);
                FragmentManager fragmentManager = ((AppCompatActivity)sContext).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, detailReportUserFragment).addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
    }


    @Override
    public int getItemCount() {
        return sPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imageProfile;
        public TextView username;
        public TextView fullname;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Post post = sPost.get(position);
                            onItemClickListener.onItemClick(post);
                        }
                    }
                }
            });
        }
    }
}
