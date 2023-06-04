package com.example.friendverse.Adapter;

import android.app.AlertDialog;
import android.content.Context;

import android.os.Bundle;
import android.util.Log;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;


import com.bumptech.glide.Glide;
import com.example.friendverse.CommentActivity;
import com.example.friendverse.Fragment.FollowFragment;
import com.example.friendverse.Fragment.HomeFragment;
import com.example.friendverse.Fragment.PostDetailFragment;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.Fragment.ReportUserFragment;
import com.example.friendverse.MainActivity;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.Profile.SettingActivity;
import com.example.friendverse.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Profile.FollowActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.example.friendverse.CommentActivity;
//import com.example.friendverse.FollowersActivity;
import java.util.ArrayList;
import java.util.List;
import com.squareup.picasso.Picasso;
import java.util.HashMap;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Context mContext;
    private List<Post> mPosts;
    private  User currentuser;
    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        getCurrentUser();

    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);

        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPosts.get(position);
        if (post.getPostType().equals("image")) {
            holder.post_image.setVisibility(View.VISIBLE);
            holder.videoView.setVisibility(View.GONE);
            Picasso.get().load(post.getPostimage()).placeholder(R.mipmap.ic_launcher).into(holder.post_image);
        } else if (post.getPostType().equals("video")) {
//            holder.post_image.setVisibility(View.GONE);
//            holder.videoView.setVisibility(View.VISIBLE);
//            Uri videoUri = Uri.parse(post.getPostvid());
//            ((AppCompatActivity) mContext).runOnUiThread(()->{holder.videoView.setVideoURI(    Uri.parse(  post.getPostvid()));});
//            holder.videoView.setOnPreparedListener(mediaPlayer -> holder.videoView.start());
//            holder.videoView.setVideoURI(videoUri);
//            MediaController vidControl = new MediaController(mContext);
//            vidControl.setAnchorView(holder.videoView);
//            holder.videoView.setMediaController(vidControl);
//            holder.videoView.start();
            holder.post_image.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);
            Uri videoUri = Uri.parse(post.getPostvid());
            holder.videoView.setVideoURI(videoUri);
            holder.videoView.setOnPreparedListener(mediaPlayer -> holder.videoView.start());
//            MediaController vidControl = new MediaController(mContext);
//            vidControl.setAnchorView(holder.videoView);
//            holder.videoView.setMediaController(vidControl);
            holder.videoView.start();

        }
        if (post.getDescription() != null && post.getDescription().equals("")) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription());
        }

        publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPublisher());
        isLiked(post.getPostid(), holder.like);
        noLikes(holder.likes, post.getPostid());
        getComments(post.getPostid(), holder.comments);
        isSaved(post.getPostid(), holder.save);
        holder.videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostDetailFragment()).commit();
            }
        });
            holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                Fragment profileFragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putString("profileid", post.getPublisher());
                profileFragment.setArguments(bundle);

                FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, profileFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                Fragment profileFragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putString("profileid", post.getPublisher());
                profileFragment.setArguments(bundle);

                FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, profileFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });


        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                final EditText editText = new EditText(mContext);
                builder.setMessage("Do you want to share this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                repostPost(post);
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


        holder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                Fragment profileFragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putString("profileid", post.getPublisher());
                profileFragment.setArguments(bundle);

                FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, profileFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });


        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostDetailFragment()).addToBackStack(null).commit();
                int a = holder.getPosition();
                HomeFragment.position = a;


            }
        });

        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.save.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue();
                }
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(post.getPostid())
                            .child(firebaseUser.getUid()).setValue(true);
                    addNotifications(post.getPublisher(), post.getPostid());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(post.getPostid())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        Intent intent = new Intent(mContext, FollowActivity.class);
                        intent.putExtra("id", post.getPostid());
                        intent.putExtra("title", "Likes");
                        mContext.startActivity(intent);
                    }
                });
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View root) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        root.getContext(), R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(root.getContext()).inflate
                        (
                                R.layout.layout_bottom_post_menu_sheet,
                                (LinearLayout)root.findViewById(R.id.bottomSheetContainer)
                        );
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(post.getPostid()).child("publisher").getValue().equals(firebaseUser.getUid())) {
                            bottomSheetView.findViewById(R.id.linear_edit).setVisibility(View.VISIBLE);
                            bottomSheetView.findViewById(R.id.linear_report).setVisibility(View.GONE);
                            bottomSheetView.findViewById(R.id.linear_delete).setVisibility(View.VISIBLE);
                        }
                        else {
                            bottomSheetView.findViewById(R.id.linear_edit).setVisibility(View.GONE);
                            bottomSheetView.findViewById(R.id.linear_report).setVisibility(View.VISIBLE);
                            bottomSheetView.findViewById(R.id.linear_delete).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("TAG", error.getMessage());
                    }
                });
                bottomSheetView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
                bottomSheetView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        FirebaseDatabase.getInstance().getReference().child("Posts").child(post.getPostid()).removeValue();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
                });
                bottomSheetView.findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.cancel();
                        Bundle bundle = new Bundle();
                        bundle.putString("postid", post.getPostid());
                        Fragment reportFragment = new ReportUserFragment();
                        reportFragment.setArguments(bundle);
                        FragmentManager fragmentManager = ((MainActivity) mContext).getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, reportFragment).addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile;
        public ImageView post_image;
        public ImageView like;
        public ImageView comment;
        public ImageView save;
        public ImageView more;
        public ImageView share;
        public TextView username;
        public TextView likes;
        public VideoView videoView;
        public TextView publisher;
        public SocialTextView description;
        public TextView comments;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            share = itemView.findViewById(R.id.share);
            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            more = itemView.findViewById(R.id.more);

            videoView = itemView.findViewById(R.id.videoView);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.no_of_likes);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
        }
    }

    private void getComments(String postid , final TextView comments){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.setText("View All " + dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void isLiked(String postid, final ImageView imageView) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
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


    private void noOfLikes (String postId, final TextView text) {
        FirebaseDatabase.getInstance().getReference().child("Likes").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                text.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void addNotifications(String userid , String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("userid" , firebaseUser.getUid());
        hashMap.put("text" , "liked your post");
        hashMap.put("postid" , postid);
        hashMap.put("ispost" , true);

        reference.push().setValue(hashMap);
    }

    private void noLikes (final TextView likes , String postid) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void publisherInfo (final ImageView image_profile , final TextView username , final TextView publisher , String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.get().load(user.getImageurl()).placeholder(R.drawable.ic_profile).into(image_profile);
                username.setText(user.getUsername());
                publisher.setText(user.getFullname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        mPosts.add(0, newPost);

        mDatabaseRef.child(postId).setValue(newPost)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext, "Share Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Fail To Share", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getText(String postid , final EditText editText) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
