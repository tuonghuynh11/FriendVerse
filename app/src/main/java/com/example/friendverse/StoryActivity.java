package com.example.friendverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.friendverse.Model.Story;
import com.example.friendverse.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    View seen;
    ImageView delete;
    ImageView image;
    //ProgressBar pro;
    StoriesProgressView progressView;
    CountDownTimer timer;
    TextView username;
    ImageView user_pic;
    String userid;
    List<String> images;
    List<String> storyIDs;
    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        seen = findViewById(R.id.r_seen);
        delete = findViewById(R.id.story_delete);
        seen.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
        username = findViewById(R.id.story_username);
        user_pic = findViewById(R.id.story_photo);
        image = findViewById(R.id.image);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userid = user.getUid();

        //pro = findViewById(R.id.stories);
        progressView = findViewById(R.id.storiesProgress);
        progressView.setStoriesListener(StoryActivity.this);
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressView.reverse();
            }
        });
        View skip = findViewById(R.id.skip);
        getStories(userid);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressView.skip();
            }
        });


    }

    @Override
    public void onNext() {
        Glide.with(getApplicationContext()).load(images.get(++i)).into(image);

    }

    @Override
    public void onPrev() {
        if ((i - 1) < 0) return;

        Glide.with(getApplicationContext()).load(images.get(--i)).into(image);


    }
    @Override
    public void onPause(){
        progressView.pause();
        super.onPause();

    }

    @Override
    public void onComplete() {
        finish();

    }
    private void getUserInfo(ImageView imageView, TextView username, String userID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(imageView);
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
                progressView.setStoriesListener(StoryActivity.this);
                progressView.setStoryDuration(5000);
                progressView.startStories(i);
                Glide.with(StoryActivity.this).load(images.get(i)).into(image);


           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

}