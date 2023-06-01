package com.example.friendverse.Fragment;

import android.os.Bundle;
import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.friendverse.Adapter.PostAdapter;
import com.example.friendverse.Adapter.UserAdapter;
import com.example.friendverse.Model.Post;
import com.example.friendverse.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YourActivityFragment extends Fragment {
    ImageView close;
    String id;
    private List<String> idList;
    RecyclerView recyclerView;
    PostAdapter postAdapter;
    List<Post> postList;
    Context context;
    public YourActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_activity, container, false);
        context = container.getContext();
        Bundle bundle = this.getArguments();
        if (bundle == null) {
            return null;
        }
        id = bundle.getString("id");
        close = view.findViewById(R.id.close);
        recyclerView = view.findViewById(R.id.recycler_view_youractivity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(context, postList);
        recyclerView.setAdapter(postAdapter);
        idList = new ArrayList<>();

        getLikedAndSavedPost();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        return view;
    }
    private void getLikedAndSavedPost() {
        DatabaseReference referenceSaves = FirebaseDatabase.getInstance().getReference("Saves").child(id);
        DatabaseReference referenceLikes = FirebaseDatabase.getInstance().getReference("Likes").child(id);
        referenceSaves.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot item : snapshot.getChildren()){
                    idList.add(item.getKey().toString());
                }
                showPosts();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        referenceSaves.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot item : snapshot.getChildren()){
                    idList.add(item.getKey().toString());
                }
                showPosts();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showPosts() {

    }
}