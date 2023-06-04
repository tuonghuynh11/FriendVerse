package com.example.friendverse.Fragment;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.VideoView;

import com.example.friendverse.ChatApp.ChatActivity;
import com.example.friendverse.R;
import com.example.friendverse.Adapter.PostAdapter;
import com.example.friendverse.Model.Post;
import com.example.friendverse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

    public class HomeFragment extends Fragment {

        private RecyclerView recyclerView;
        private PostAdapter postAdapter;
        private List<Post> postLists;

        private int currentPosition;
        private List<String> followingList;
        private ImageView chatBtn;
        public  static  int position = 0;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_home, container, false);


            recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(false);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(false);
            recyclerView.setLayoutManager(linearLayoutManager);
            postLists = new ArrayList<>();
            postAdapter = new PostAdapter(getContext(), postLists);
            recyclerView.setAdapter(postAdapter);
            chatBtn = view.findViewById(R.id.chatButton);


            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getContext(), ChatActivity.class);
                    startActivity(i);
                }
            });
            checkFollowing();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.scrollToPosition(5);
                }
            }, 5000);

            return view;
        }


        private void checkFollowing() {
            followingList = new ArrayList<>();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                        .child(uid)
                        .child("following");

                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        followingList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            followingList.add(snapshot.getKey());
                        }

                        readPosts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {

            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if(position <= postAdapter.getItemCount()){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RecyclerView.LayoutManager tf = new LinearLayoutManager(getContext());
                        tf.scrollToPosition(5);
                        recyclerView.scrollToPosition(5);
                    }
                }, 5000);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            checkFollowing();
        }

        private void readPosts() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    postLists.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        for (String id : followingList) {
                            if (post.getPublisher() != null && post.getPublisher().equals(id)) {
                                postLists.add(post);
                            }
                        }
                    }

                    postAdapter.notifyDataSetChanged();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(5);
                        }
                    }, 5000);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
//
            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Reposts");

            reference2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        for (String id : followingList) {
                            if (post.getPublisher() != null && post.getPublisher().equals(id)) {
                                postLists.add(post);
                            }
                        }
                    }
                    postAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }



        }
