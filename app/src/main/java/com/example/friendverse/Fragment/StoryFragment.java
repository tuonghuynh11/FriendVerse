package com.example.friendverse.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.friendverse.Model.Story;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.StoryActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StoryFragment extends Fragment implements StoriesProgressView.StoriesListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    LinearLayout seen;
    ImageView delete;
    ImageView image;
    //ProgressBar pro;
    StoriesProgressView progressView;
    CountDownTimer timer;
    TextView username;
    ImageView user_pic;
    TextView seen_number;
    ImageView save;

    String userid;
    List<String> images;
    List<String> storyIDs;
    int i = 0;
    long presstime = 0L;
    long limit = 500L;
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN :
                    presstime = System.currentTimeMillis();
                    progressView.pause();
                    return false;

                case MotionEvent.ACTION_UP :
                    long now = System.currentTimeMillis();
                    progressView.resume();
                    return limit < now - presstime;
            }

            return false;
        }
    };
    public StoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StoryFragment newInstance(String param1, String param2) {
        StoryFragment fragment = new StoryFragment();
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_story, container, false);
        seen = v.findViewById(R.id.r_seen);
        delete = v.findViewById(R.id.story_delete);
        seen.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        username = v.findViewById(R.id.story_username);
        user_pic = v.findViewById(R.id.story_photo);
        image = v.findViewById(R.id.image);
        save = v.findViewById(R.id.save);
        seen_number = v.findViewById(R.id.seen_number);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userid = user.getUid();
        userid = getArguments().getString("userid");

        //pro = v.v.findViewById(R.id.stories);
        progressView = v.findViewById(R.id.storiesProgress);
        progressView.setStoriesListener(this);
        View reverse = v.findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);
        View skip = v.findViewById(R.id.skip);
        getStories(userid);
        getUserInfo(user_pic, username, userid);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressView.skip();
            }
        });
        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", userid);
                editor.apply();

               getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

            }
        });
        skip.setOnTouchListener(onTouchListener);
        seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("Delete?");
                builder.setMessage("Do you want to delete?");
                builder.setNegativeButton("No way", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Stories")
                                .child(userid).child(storyIDs.get(i));
                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(getActivity(), "Deleted!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });


            }
        });


        return v;
    }

    @Override
    public void onNext() {
        Glide.with(getActivity()).load(images.get(++i)).into(image);
        addView(storyIDs.get(i));
        seenNumber(storyIDs.get(i));


    }

    public void onPrev() {
        if ((i - 1) < 0) return;

        Glide.with(getActivity()).load(images.get(--i)).into(image);
        seenNumber(storyIDs.get(i));



    }
    @Override
    public void onPause(){
        progressView.pause();
        super.onPause();

    }

    @Override
    public void onComplete() {
        getActivity().finish();

    }
    private void addView(String storyid){
        FirebaseDatabase.getInstance().getReference("Stories").child(userid)
                .child(storyid).child("view").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(true);


    }
    private void seenNumber(String storyid){
        DatabaseReference seenRef = FirebaseDatabase.getInstance().getReference("Stories").child(userid)
                .child(storyid).child("view");
        seenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seen_number.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getUserInfo(ImageView imageView, TextView username, String userID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(getActivity()).load(user.getImageurl()).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void getStories(String userid){
        images = new ArrayList<>();
        storyIDs = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Stories").child(userid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                images.clear();
                storyIDs.clear();
                for(DataSnapshot sn : snapshot.getChildren()){
                    Story myStory = sn.getValue(Story.class);
                    long currentTime = System.currentTimeMillis();
                    if(currentTime >= myStory.getTimeCreated() && currentTime <= myStory.getAfter1day()){
                        images.add(myStory.getImageURL());
                        storyIDs.add(myStory.getStoryID());
                    }
                }
                progressView.setStoriesCount(images.size());
                progressView.setStoriesListener(StoryFragment.this);
                progressView.setStoryDuration(5000);
                progressView.startStories(i);
                Glide.with(getActivity()).load(images.get(i)).into(image);
                addView(storyIDs.get(i));
                seenNumber(storyIDs.get(i));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}