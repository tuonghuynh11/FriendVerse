package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.friendverse.Adapter.ImageSentAdapter;
import com.example.friendverse.Model.ChatMessage;
import com.example.friendverse.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SentImagesGroupActivity extends AppCompatActivity {
    private RecyclerView imageSentRecyclerViews;
    private List<String> imagesList;
    private ImageSentAdapter imageSentAdapter;
    private String groupId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_group);
        imageSentRecyclerViews = findViewById(R.id.imageSentRecyclerView);
        imagesList = new ArrayList<>();
        if (getIntent().getExtras() != null) {
            if (getIntent().getStringExtra("groupId") != null) {
                groupId = getIntent().getStringExtra("groupId").toString();
            }
        }
        ImageButton imageButton= findViewById(R.id.backButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        init();
    }

    private void init() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if ( chatMessage.getReceiverId().equals(groupId)) {
                        if (chatMessage.getMessageType().equals("image")) {
                            imagesList.add(chatMessage.getMessage());
                        }
                    }

                }
                imageSentAdapter = new ImageSentAdapter(SentImagesGroupActivity.this, imagesList);
                imageSentRecyclerViews.setHasFixedSize(true);
                GridLayoutManager manager = new GridLayoutManager(SentImagesGroupActivity.this, 4);
                imageSentRecyclerViews.setLayoutManager(manager);
                imageSentRecyclerViews.setAdapter(imageSentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}