package com.example.friendverse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.friendverse.Adapter.CommentAdapter;
import com.example.friendverse.Model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    TextView post;
    FirebaseUser currentUser;
    EditText comment;
    String postID;
    String publisherID;
    RecyclerView commentLV;
    List<Comment> commentList;
    CommentAdapter cmtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        post = findViewById(R.id.post);
        comment = findViewById(R.id.comment_place);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        commentLV = findViewById(R.id.recycler_view);
        commentLV.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        commentLV.setLayoutManager(linearLayoutManager);
        //postID = "-NVeSq29BLnq5kz2LwRh";
        postID = getIntent().getStringExtra("postid");
        publisherID = getIntent().getStringExtra("publisherid");
        commentList = new ArrayList<>();
        cmtAdapter = new CommentAdapter(CommentActivity.this, commentList, postID);
        commentLV.setAdapter(cmtAdapter);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(comment.getText().toString().trim().isEmpty()){
                    Toast.makeText(CommentActivity.this, "You didn't comment", Toast.LENGTH_SHORT);
                }
                else{
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postID);
                    String commentID = reference.push().getKey();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("commentID", commentID);
                    hashMap.put("comment", comment.getText().toString());
                    hashMap.put("publisher", currentUser.getUid());
                    reference.child(commentID).setValue(hashMap);
                    comment.setText("");
                    addNotifications();

                }

            }
        });
        readComment();

    }

    private void addNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherID);

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("userid" , currentUser.getUid());
        hashMap.put("text" , "commented: " + comment.getText().toString());
        hashMap.put("postid" , postID);
        hashMap.put("ispost" , true);

        reference.push().setValue(hashMap);
    }
    void readComment(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for(DataSnapshot e : snapshot.getChildren()){
                    Comment comment1 = e.getValue(Comment.class);
                    commentList.add(comment1);
                }
                cmtAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}