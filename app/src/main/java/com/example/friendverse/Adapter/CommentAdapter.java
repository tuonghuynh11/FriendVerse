package com.example.friendverse.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Model.Comment;
import com.example.friendverse.Model.User;
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
                }
                return true;
            }
        });


    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView image_profile;
        public TextView username;
        public TextView comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.cmtIcon);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.YourCmt);
        }
    }
    private void getUserInfo(ImageView imageView, TextView username, String userID){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Picasso.get().load(user.getImageurl()).into(imageView);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
