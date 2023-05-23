package com.example.friendverse.Fragment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        private List<String> followingList;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_home, container, false);

            recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            postLists = new ArrayList<>();
            postAdapter= new PostAdapter(getContext(), postLists);
            recyclerView.setAdapter(postAdapter);

            checkFollowing();

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

        private void readPosts() {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    postLists.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);
                        for (String id : followingList) {
                            if (post.getPublisher().equals(id)) {
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