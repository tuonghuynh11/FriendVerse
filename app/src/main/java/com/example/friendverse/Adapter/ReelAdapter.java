package com.example.friendverse.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendverse.AddReelActivity;
import com.example.friendverse.CommentActivity;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.Profile.FollowActivity;
import com.example.friendverse.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReelAdapter extends RecyclerView.Adapter<ReelAdapter.viewHolder> {
    Context thisContext;
    List<Post> reelList;
    FirebaseUser firebaseUser;
    User currentuser;
    public ReelAdapter(Context context, List<Post> reelList){
        thisContext = context;
        this.reelList = reelList;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getCurrentUser();

    }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reel_video, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        holder.setData(position);
        Post reel = reelList.get(position);
        holder.content.setText(reel.getDescription());
        holder.content.setMovementMethod(new ScrollingMovementMethod());
        isLikes(reel.getPostid(), holder.like);
        String id = reel.getPublisher();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(thisContext.getApplicationContext()).load(user.getImageurl()).placeholder(R.drawable.default_user_avatar).into(holder.user_Icon);
                holder.username.setText(user.getUsername());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(reel.getPostid())
                            .child(firebaseUser.getUid()).setValue(true);
                    //addNotifications(post.getPublisher() , post.getPostid());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(reel.getPostid())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(thisContext , CommentActivity.class);
                intent.putExtra("postid" , reel.getPostid());
                intent.putExtra("publisherid" , reel.getPublisher());
                thisContext.startActivity(intent);
                holder.progressBar.setVisibility(View.VISIBLE);
            }
        });
        holder.likes.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(thisContext, FollowActivity.class);
                                                intent.putExtra("id", reel.getPostid());
                                                intent.putExtra("title", "Likes");
                                                thisContext.startActivity(intent);
                                            }
                                        });

        holder.addReel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent x = new Intent(thisContext, AddReelActivity.class);
                thisContext.startActivity(x);
                holder.progressBar.setVisibility(View.VISIBLE);
            }
        });
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
                final EditText editText = new EditText(thisContext);
                builder.setMessage("Do you want to share this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                repostPost(reel);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }


        });
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle passData = new Bundle();
                passData.putString("profileid", reel.getPublisher());
                Fragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(passData);
                FragmentManager fragmentManager = ((AppCompatActivity)thisContext).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, profileFragment).addToBackStack(null);
                fragmentTransaction.commit();


            }
        });
        holder.user_Icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle passData = new Bundle();
                passData.putString("profileid", reel.getPublisher());
                Fragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(passData);
                FragmentManager fragmentManager = ((AppCompatActivity)thisContext).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, profileFragment).addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.save.getTag().equals("save")){
                    FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.getUid()).child(reel.getPostid())
                            .setValue(true);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.getUid()).child(reel.getPostid()).removeValue();
                }
            }
        });

        getLikes(holder.likes, reel.getPostid());
        getComments(holder.comments, reel.getPostid());
        isSaved(reel.getPostid(), holder.save);
    }

    @Override
    public int getItemCount() {
        return reelList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        VideoView reelVid;
        ImageView user_Icon, like, share, comment, save;
        ImageView addReel;
        TextView content;
        TextView username;
        TextView likes, shares, comments;
        ProgressBar progressBar;
        public viewHolder(@NonNull View itemView){
            super(itemView);
            reelVid = itemView.findViewById(R.id.ReelVideo);
            user_Icon = itemView.findViewById(R.id.reel_image_profile);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            share = itemView.findViewById(R.id.share);
            content = itemView.findViewById(R.id.contentReel);
            username = itemView.findViewById(R.id.reelUsername);
            likes = itemView.findViewById(R.id.likes);
            comments = itemView.findViewById(R.id.comments);
            shares = itemView.findViewById(R.id.shares);
            save = itemView.findViewById(R.id.save);
            addReel = itemView.findViewById(R.id.addReel);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        void setData(int position){
            if(reelList.get(position).getPostid() != null){
                reelVid.setVideoURI(Uri.parse(reelList.get(position).getPostvid()));
            }

            reelVid.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    progressBar.setVisibility(View.GONE);
                    mediaPlayer.start();
                    float videoRatio = mediaPlayer.getVideoWidth()/(float)mediaPlayer.getVideoHeight();
                    float screenRatio = reelVid.getWidth()/(float)reelVid.getHeight();
                    float scale = videoRatio/screenRatio;
                    if(scale >= 1f){
                        reelVid.setScaleX(scale);
                    }
                    else {
                        reelVid.setScaleY(1f/scale);
                    }
                }
            });
            reelVid.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Log.d("VideoError", "Can't load the video");
                    return true;
                }
            });
            reelVid.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

        }
    }
    private void isSaved (final String postid , final ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.ic_save);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.ic_save_black);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void getCurrentUser(){
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child(User.USERKEY).child(firebaseUser.getUid());
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentuser = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void repostPost(Post repostedPost) {
        DatabaseReference mDatabaseRef;
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Reposts");
        Post newPost = new Post();
        repostedPost.setDescription(repostedPost.getDescription() + "\n\nRepost");

        // Update the image_profile, username, and publisher fields with current user's information
//        newPost.setImagine(currentUser.getPhotoUrl().toString());
        newPost.setUsername(currentuser.getUsername());
        newPost.setPublisher(currentuser.getId());

        newPost.setPostType(repostedPost.getPostType());
        if (newPost.getPostType().equals("video")) {
            newPost.setPostvid(repostedPost.getPostvid());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

            String postid = reference.push().getKey();

            HashMap<String , Object> hashMap = new HashMap<>();
            hashMap.put("createdTime", System.currentTimeMillis());
            hashMap.put("postType","video");
            hashMap.put("postid" , postid);
            hashMap.put("postvid" , repostedPost.getPostvid());
            hashMap.put("description" , "*Repost*\n" +  repostedPost.getDescription().toString());
            hashMap.put("publisher" , FirebaseAuth.getInstance().getCurrentUser().getUid());

            reference.child(postid).setValue(hashMap);
        } else if (newPost.getPostType().equals("image")) {
            newPost.setPostimage(repostedPost.getPostimage());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

            String postid = reference.push().getKey();

            HashMap<String , Object> hashMap = new HashMap<>();
            hashMap.put("postType","image");
            hashMap.put("postid" , postid);
            hashMap.put("postimage" , repostedPost.getPostimage());
            hashMap.put("description" , "*Repost*\n" +  repostedPost.getDescription().toString());
            hashMap.put("publisher" , FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap.put("createdTime", System.currentTimeMillis());
            reference.child(postid).setValue(hashMap);
        }

        newPost.setPostid(repostedPost.getPostid());
        newPost.setRepostCount(repostedPost.getRepostCount());
        newPost.setShared(true);

        String postId = mDatabaseRef.push().getKey();
        mDatabaseRef.child(postId).setValue(newPost);
        reelList.add(0, newPost);

        mDatabaseRef.child(postId).setValue(newPost)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(thisContext, "Share Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(thisContext, "Fail To Share", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void isLikes (String reelID , ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(reelID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_favorite);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_favorited);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public int getLikes(TextView likes, String commentID){
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("Likes").child(commentID);

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    likes.setText(getNum(snapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return 0;
    }
    private String getNum(long x){
        if(x < 1000){
            return x + "";
        }
        if(x >= 1000 && x <= 1000000)
            return x/1000 + "K";
        return x/1000000 + "M";
    }
    public void getComments(TextView comments, String reelID){
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("Comments").child(reelID);
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    comments.setText(getNum(snapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void sharePostWithFollowers(final Post post) {
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference().child("Followers").child(firebaseUser.getUid()).child("followers");
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<String> followersList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    followersList.add(snapshot.getKey());
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
                builder.setTitle("Select followers to share with")
                        .setMultiChoiceItems(followersList.toArray(new String[0]), null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                // Handle follower selection
                            }
                        })
                        .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SparseBooleanArray checkedItems = ((AlertDialog) dialog).getListView().getCheckedItemPositions();
                                for (int i = 0; i < followersList.size(); i++) {
                                    if (checkedItems.get(i)) {
                                        String followerId = followersList.get(i);
                                        sharePostWithFollower(followerId, post);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sharePostWithFollower(String followerId, Post post) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Shares")
                .child(followerId)
                .child(post.getPostid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("postid", post.getPostid());

        reference.updateChildren(hashMap);
    }

}
