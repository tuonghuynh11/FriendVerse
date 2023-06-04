package com.example.friendverse.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.friendverse.Adapter.PostAdapter;
import com.example.friendverse.Model.Hashtag;
import com.example.friendverse.Model.Post;
import com.example.friendverse.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
public class TagListFragment extends Fragment {

    private String jsonString;
    private ImageView back;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<Hashtag> tagList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("TagPrefs" , Context.MODE_PRIVATE);
        jsonString = sharedPreferences.getString("tagList" , "none");

        Gson gson = new Gson();
        Type type = new TypeToken<List<Hashtag>>() {}.getType();
        tagList = gson.fromJson(jsonString , type);

        readMultiplePosts();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext() , postList);
        recyclerView.setAdapter(postAdapter);
        back = view.findViewById(R.id.back);
//        VideoView videoView = view.findViewById(R.id.videoView);
//        MediaController mediaController = new MediaController(getActivity());
//        mediaController.setAnchorView(videoView);
//        videoView.setMediaController(mediaController);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
//                FragmentManager fm = requireActivity().getSupportFragmentManager();
//                fm.popBackStack();

            }
        });
        return view;
    }

    private void readMultiplePosts() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    for (Hashtag tag : tagList){
                        if (snapshot.getKey().equals(tag.getPostid())){
                            Post post = snapshot.getValue(Post.class);
                            postList.add(post);
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

