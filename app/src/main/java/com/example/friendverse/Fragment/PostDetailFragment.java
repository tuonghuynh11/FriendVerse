package com.example.friendverse.Fragment;
import android.content.Context;

import android.content.Intent;
import android.net.Uri;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.MediaController;
import android.widget.VideoView;


import com.example.friendverse.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.friendverse.Adapter.PostAdapter;
import com.example.friendverse.Model.Post;
import com.example.friendverse.R;

import java.util.ArrayList;
import java.util.List;

public class PostDetailFragment extends Fragment {

    private MediaController mediaController;

    private String postId;

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private VideoView videoView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        Bundle bundle = this.getArguments();
        if (bundle == null) {
            return null;
        }
        postid = bundle.getString("postid");
        back = view.findViewById(R.id.back);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext() , postList);
        recyclerView.setAdapter(postAdapter);

        ImageView backButton = view.findViewById(R.id.back);
//        VideoView videoView = view.findViewById(R.id.videoView);
//        MediaController mediaController = new MediaController(getActivity());
//        mediaController.setAnchorView(videoView);
//        videoView.setMediaController(mediaController);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        FirebaseDatabase.getInstance().getReference().child("Posts").child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                Post post = dataSnapshot.getValue(Post.class);
                postList.add(post);

                postAdapter.notifyDataSetChanged();
//                String videoUri = dataSnapshot.child("postvid").getValue(String.class);
//                if (videoUri != null) {
//                    videoView.setVideoURI(Uri.parse(videoUri));
//                    videoView.start();
//                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}