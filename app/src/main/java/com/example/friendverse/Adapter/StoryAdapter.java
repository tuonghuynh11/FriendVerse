package com.example.friendverse.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.AddStoryActivity;
import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.Story;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.viewHolder> {
    Context thisContext;
    List<Story> storyList;
    FirebaseUser firebaseUser;
    public StoryAdapter(Context mContext, List<Story> list){
        thisContext = mContext;
        storyList = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0){
            View view = LayoutInflater.from(thisContext).inflate(R.layout.add_story_item , parent , false);
            return new StoryAdapter.viewHolder(view);
        }
        View view = LayoutInflater.from(thisContext).inflate(R.layout.story_item , parent , false);
        return new StoryAdapter.viewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        final Story story = storyList.get(position);
        userInfo(holder , story.getUserID() , position);

        if (holder.getAbsoluteAdapterPosition() != 0){
            seenStory(holder , story.getUserID());
        }

        if (holder.getAbsoluteAdapterPosition() == 0){
            myStory(holder.add_story_text , holder.story_plus , false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getAbsoluteAdapterPosition() == 0){
                    myStory(holder.add_story_text , holder.story_plus , true);
                } else {
                    //Toast.makeText(thisContext, "La la la", Toast.LENGTH_SHORT);
                    Intent intent = new Intent(thisContext , StoryActivity.class);
                    intent.putExtra("userid" , story.getUserID());
                    thisContext.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        public ImageView story_photo;
        public ImageView story_plus;
        public ImageView story_photo_seen;
        public TextView story_username;
        public TextView add_story_text;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            story_photo = itemView.findViewById(R.id.story_photo);
            story_plus = itemView.findViewById(R.id.story_plus);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
            story_username = itemView.findViewById(R.id.story_username);
            add_story_text = itemView.findViewById(R.id.add_story_text);
        }
    }
    private void userInfo(final viewHolder viewHolder , String userid , final int pos) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.get().load(user.getImageurl()).placeholder(R.drawable.default_user_avatar).into(viewHolder.story_photo);
                if (pos != 0){
                    Picasso.get().load(user.getImageurl()).placeholder(R.drawable.default_user_avatar).into(viewHolder.story_photo_seen);
                    viewHolder.story_username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return 0;

        return 1;
    }
    private void myStory (final TextView textView , final ImageView imageView , final boolean click){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Stories")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent = System.currentTimeMillis();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Story story = snapshot.getValue(Story.class);
                    if (timecurrent > story.getTimeCreated() && timecurrent < story.getAfter1day()){
                        count++;
                    }
                }

                if (click){
                    if (count > 0){
                        AlertDialog alertDialog = new AlertDialog.Builder(thisContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(thisContext , StoryActivity.class);
                                        intent.putExtra("userid" , FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        thisContext.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(thisContext , AddStoryActivity.class);
                                        thisContext.startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.show();
                    } else {
                        Intent intent = new Intent(thisContext , AddStoryActivity.class);
                        thisContext.startActivity(intent);
                    }
                } else {
                    if (count > 0){
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    } else {
                        textView.setText("Add Story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenStory (final viewHolder viewHolder , String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Stories")
                .child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!snapshot.child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()
                            && System.currentTimeMillis() < snapshot.getValue(Story.class).getAfter1day()){
                        i++;
                    }
                }

                if (i > 0){
                    viewHolder.story_photo.setVisibility(View.VISIBLE);
                    viewHolder.story_photo_seen.setVisibility(View.GONE);
                } else {
                    viewHolder.story_photo.setVisibility(View.GONE);
                    viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
