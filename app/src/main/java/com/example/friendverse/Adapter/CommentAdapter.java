package com.example.friendverse.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.Model.Comment;
import com.example.friendverse.Model.User;
import com.example.friendverse.Profile.FollowActivity;
import com.example.friendverse.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private List<Comment> comments;
    private String PostID;
    FirebaseUser currentUser;
    public CommentAdapter(Context mmm, List<Comment> list, String id){
        context = mmm;
        comments = list;
        PostID = id;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int ViewType){
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }
    @Override
    public int getItemCount(){
        return comments.size();
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position){
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Comment comment = comments.get(position);
        viewHolder.comment.setText(comment.getComment());
        getUserInfo(viewHolder.image_profile, viewHolder.username, comment.getPublisher());
        isLikes(comment.getCommentID(), viewHolder.likeStat);
        getLikes(viewHolder.likes, comment.getCommentID());
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(comment.getPublisher().equals(currentUser.getUid())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(true);
                    builder.setTitle("Delete?");
                    builder.setMessage("Do you want to delete?");
                    builder.setNegativeButton("No way", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.setPositiveButton("Just delete it", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseDatabase.getInstance().getReference("Comments")
                                    .child(PostID).child(comment.getCommentID())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                return true;
            }
        });
        viewHolder.likeStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.likeStat.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(comment.getCommentID())
                            .child(currentUser.getUid()).setValue(true);
                    //addNotifications(post.getPublisher() , post.getPostid());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes")
                            .child(comment.getCommentID())
                            .child(currentUser.getUid()).removeValue();
                }
            }
        });
        viewHolder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context , FollowActivity.class);
                intent.putExtra("id" , comment.getCommentID());
                intent.putExtra("title" , "Likes");
                context.startActivity(intent);
            }
        });
        viewHolder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", comment.getPublisher());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

            }
        });
        viewHolder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", comment.getPublisher());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();

            }
        });



    }
    public int getLikes(TextView likes, String commentID){
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("Likes").child(commentID);

        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()){
                    likes.setText(getNum(snapshot.getChildrenCount()));
                }
                else
                {
                    likes.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return 0;
    }
    private String getNum(long x){
        if(x < 1000){
            return x + "";
        }
        if(x >= 1000 && x <= 1000000)
            return x/1000 + "K";
        return x/1000000 + "M";
    }
    private void isLikes (String commentID , ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(commentID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.happy);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.stoic);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView image_profile;
        public TextView username;
        public TextView comment;
        public TextView likes;
        public ImageView likeStat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.cmtIcon);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.YourCmt);
            likes = itemView.findViewById(R.id.likes);
            likeStat = itemView.findViewById(R.id.likeIcon);
        }
    }
    private void getUserInfo(ImageView imageView, TextView username, String userID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Picasso.get().load(user.getImageurl()).placeholder(R.drawable.default_avatar).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
