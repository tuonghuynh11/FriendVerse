package com.example.friendverse.Fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendverse.Adapter.MyPhotoAdapter;
import com.example.friendverse.Profile.FollowActivity;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.Profile.SettingActivity;
import com.example.friendverse.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    ImageView options;
    CircleImageView image_profile;
    TextView posts, followers, following, fullname, bio, username;
    Button edit_profile, share_profile;
    RecyclerView recyclerView;
    MyPhotoAdapter myPhotoAdapter;
    ArrayList<Post> postList;
    FirebaseUser firebaseUser;
    String profileid;
    private ArrayList<String> mySaved;
    private ArrayList<Post> postList_savedPosts;
    RecyclerView recyclerView_savedPosts;
    MyPhotoAdapter myPhotoAdapter_savedPosts;

    ImageButton my_photos, saved_photos;
    View root;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profileid = getArguments().getString("profileid");

        image_profile = root.findViewById(R.id.image_profile);
        options = root.findViewById(R.id.options);
        posts = root.findViewById(R.id.posts);
        followers = root.findViewById(R.id.followers);
        following = root.findViewById(R.id.following);
        fullname = root.findViewById(R.id.fullname);
        bio = root.findViewById(R.id.bio);
        username = root.findViewById(R.id.username);
        edit_profile = root.findViewById(R.id.edit_profile);
        share_profile = root.findViewById(R.id.share_profile);
        my_photos = root.findViewById(R.id.my_photos);
        saved_photos = root.findViewById(R.id.save_photos);
        // posts
        recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(root.getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        myPhotoAdapter = new MyPhotoAdapter(root.getContext(), postList);
        recyclerView.setAdapter(myPhotoAdapter);
        // save posts
        recyclerView_savedPosts = root.findViewById(R.id.recycler_view_save);
        recyclerView_savedPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager_savedPosts = new GridLayoutManager(root.getContext(), 3);
        recyclerView_savedPosts.setLayoutManager(linearLayoutManager_savedPosts);
        postList_savedPosts = new ArrayList<>();
        myPhotoAdapter_savedPosts = new MyPhotoAdapter(root.getContext(), postList_savedPosts);
        recyclerView_savedPosts.setAdapter(myPhotoAdapter_savedPosts);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_savedPosts.setVisibility(View.GONE);

        userInfo();
        getFollowers();
        myPhotosAndCountPosts();
        mySaved();

        if (profileid.equals(firebaseUser.getUid())) {
            edit_profile.setText("Edit Profile");
            share_profile.setText("Share Profile");
        }
        else {
            share_profile.setText("Message");
            checkFollow();
            saved_photos.setVisibility(View.GONE);
        }

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String button = edit_profile.getText().toString();
                if (button.equals("Edit Profile")) {
//                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                }
                else if (button.equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid).child("followers").child(firebaseUser.getUid()).setValue(true);
                }
                else if (button.equals("Following")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid).child("followers").child(firebaseUser.getUid()).setValue(true);
                }
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getContext(), SettingActivity.class));
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        root.getContext(), R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(root.getContext()).inflate
                        (
                        R.layout.layout_bottom_sheet,
                                (LinearLayout)root.findViewById(R.id.bottomSheetContainer)
                        );
                bottomSheetView.findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getContext(), SettingActivity.class);
                        startActivity(intent);
                    }
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();

            }
        });

        my_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_savedPosts.setVisibility(View.GONE);
            }
        });

        saved_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.GONE);
                recyclerView_savedPosts.setVisibility(View.VISIBLE);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        return root;
    }
    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (root.getContext() == null) {
                    return;
                }
                User user = new User();
                user = snapshot.getValue(User.class);
                Glide.with(root.getContext()).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
                fullname.setText(user.getFullname());
                bio.setText(user.getBio());

                share_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String button = share_profile.getText().toString();
                        if (button.equals("Share Profile")) {
                            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("CopyUserName", username.getText());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getContext(), "Copied Username to Clipboard", Toast.LENGTH_SHORT).show();
                        }
                        else if (button.equals("Message")) {

                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", error.getMessage());
            }
        });
    }
    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileid).exists()) {
                    edit_profile.setText("Following");
                }
                else {
                    edit_profile.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("follower");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference newReference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following");
        newReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myPhotosAndCountPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                postList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                        Post post = new Post();
                        post = item.getValue(Post.class);
                    if (post.getPublisher() != null && post.getPublisher().equals(profileid)) {
                        postList.add(post);
                        count++;
                    }
                }
                posts.setText("" + count);
                Collections.reverse(postList);
                myPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mySaved() {
        mySaved = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saved").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()){
                    mySaved.add(item.getKey());
                }
                readSaved();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readSaved() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                postList_savedPosts.clear();
                for (DataSnapshot item : snapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);

                    for (String id : mySaved) {
                        if (post.getPostid().equals(id)) {
                            postList_savedPosts.add(post);
                        }
                    }
                }
                myPhotoAdapter_savedPosts.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
