package com.example.friendverse.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.friendverse.Adapter.ReelAdapter;
import com.example.friendverse.AddReelActivity;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReelFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    List<Post> reels;
    ViewPager2 reelView;
    ImageView addReel;
    ReelAdapter adapter;
    public ReelFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ReelFragment newInstance(String param1, String param2) {
        ReelFragment fragment = new ReelFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reel, container, false);
        reelView = view.findViewById(R.id.viewPager);
        reels = new ArrayList<>();
        adapter = new ReelAdapter(getContext(), reels);
        reelView.setAdapter(adapter);

        takeVideo();

        // Inflate the layout for this fragment
        return view;
    }
    public void takeVideo(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reels.clear();

                for(DataSnapshot child: snapshot.getChildren()){

                    if(child.hasChild("postvid")){
                       // Toast.makeText(getContext(), child.getKey(), Toast.LENGTH_SHORT).show();

                        Post post1 = child.getValue(Post.class);
                        reels.add(post1);
                    }
                }
                if(adapter != null)
                    adapter.notifyDataSetChanged();
                else {
                    adapter = new ReelAdapter(getContext(), reels);
                    reelView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}