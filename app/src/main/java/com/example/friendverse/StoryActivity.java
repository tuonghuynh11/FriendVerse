package com.example.friendverse;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.Model.Story;
import com.example.friendverse.Model.User;
import com.example.friendverse.Profile.FollowActivity;
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

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        seen = findViewById(R.id.r_seen);
        delete = findViewById(R.id.story_delete);
        seen.setVisibility(View.VISIBLE);
        delete.setVisibility(View.GONE);
        username = findViewById(R.id.story_username);
        user_pic = findViewById(R.id.story_photo);
        image = findViewById(R.id.image);
        save = findViewById(R.id.save);
        seen_number = findViewById(R.id.seen_number);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userid = user.getUid();
        userid = getIntent().getStringExtra("userid");
        if(userid.equals(user.getUid())){
            delete.setVisibility(View.VISIBLE);
        }

        //pro = findViewById(R.id.stories);
        progressView = findViewById(R.id.storiesProgress);
        progressView.setStoriesListener(StoryActivity.this);
        View reverse = findViewById(R.id.reverse);

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", userid);
                editor.apply();

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

            }
        });
        user_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", userid);
                editor.apply();

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

            }
        });
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);
        View skip = findViewById(R.id.skip);
        getStories(userid);
        getUserInfo(user_pic, username, userid);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference saveRef = FirebaseDatabase.getInstance().getReference("Saves").child(currentUser.getUid()).child(storyIDs.get(i));
                if(save.getTag().equals("save")){
                    saveRef.setValue(true);
                }
                else {
                    saveRef.removeValue();
                }



            }
        });
        seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StoryActivity.this);
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
                                    Toast.makeText(StoryActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });


            }
        });

    }

    @Override
    public void onNext() {
        Glide.with(getApplicationContext()).load(images.get(++i)).into(image);
        addView(storyIDs.get(i));
        seenNumber(storyIDs.get(i));


    }

    @Override
    public void onPrev() {
        if ((i - 1) < 0) return;

        Glide.with(getApplicationContext()).load(images.get(--i)).into(image);
        seenNumber(storyIDs.get(i));



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
    private void isSaved (final String storyid , final ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(storyid).exists()){
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
                isSaved(storyIDs.get(i), save);

                progressView.setStoriesCount(images.size());
                progressView.setStoriesListener(StoryActivity.this);
                progressView.setStoryDuration(5000);
                progressView.startStories(i);
                Glide.with(StoryActivity.this).load(images.get(i)).into(image);
                isSaved(storyIDs.get(i), save);
                addView(storyIDs.get(i));
                seenNumber(storyIDs.get(i));

           }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}