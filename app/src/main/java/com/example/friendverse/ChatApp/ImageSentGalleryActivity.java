package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.friendverse.Adapter.ImageSentAdapter;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.MainActivity;
import com.example.friendverse.Model.ChatMessage;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageSentGalleryActivity extends AppCompatActivity {

    private User currentUser;
    private String receiverId;
    private String receiverImage;
    private String receiverFullName;
    private String conservationId;
    private ImageButton back;
    private RoundedImageView goToProfileUserBtn;
    private RecyclerView imageSentRecyclerViews;
    private List<String> imagesList;
    private ImageSentAdapter imageSentAdapter;

    private ConstraintLayout leaveConversation;

    //Firebase
    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_sent_gallery);
        currentUser = new User();
        currentUser.setImageurl("https://thuthuatnhanh.com/wp-content/uploads/2022/08/hinh-nen-may-tinh-4k.jpg");
        currentUser.setUsername("Nguyen Van M");

        leaveConversation = findViewById(R.id.LeaveTheGroup);
        auth = FirebaseAuth.getInstance();
        back = findViewById(R.id.backButton);
        goToProfileUserBtn = findViewById(R.id.imageProfileBtn);
        imageSentRecyclerViews = findViewById(R.id.imageSentRecyclerView);
        imagesList = new ArrayList<>();
        //Lấy thông tin người nhận tin nhắn
        Intent i = getIntent();
        if (i.getExtras() != null) {
            if (i.getStringExtra("interacter") != null) {
                receiverId = i.getStringExtra("interacter");
                if (i.getStringExtra("interacterImage") != null) {
                    receiverImage = i.getStringExtra("interacterImage");
                }
                if (i.getStringExtra("interacterFullName") != null) {
                    receiverFullName = i.getStringExtra("interacterFullName");
                }
                if (i.getStringExtra("conversationId") != null) {
                    conservationId = i.getStringExtra("conversationId");
                }

                TextView name = findViewById(R.id.userNameTextView);
                name.setText(receiverFullName);
                Glide.with(this).load(receiverImage).into(goToProfileUserBtn);
                reference = FirebaseDatabase.getInstance().getReference().child("Chats");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                            if ((chatMessage.getSenderId().equals(auth.getUid()) && chatMessage.getReceiverId().equals(receiverId)) || (chatMessage.getReceiverId().equals(auth.getUid()) && chatMessage.getSenderId().equals(receiverId))) {
                                if (chatMessage.getMessageType().equals("image")) {
                                    imagesList.add(chatMessage.getMessage());
                                }
                            }

                        }
                        imageSentAdapter = new ImageSentAdapter(ImageSentGalleryActivity.this, imagesList);
                        imageSentRecyclerViews.setHasFixedSize(true);
                        GridLayoutManager manager = new GridLayoutManager(ImageSentGalleryActivity.this, 4);
                        imageSentRecyclerViews.setLayoutManager(manager);
                        imageSentRecyclerViews.setAdapter(imageSentAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
        leaveConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference = FirebaseDatabase.getInstance().getReference().child(ChatMessage.KEY_COLLECTION_CONVERSATION);
                reference.child(conservationId).removeValue();
                Intent i= new Intent(getApplicationContext(),ChatActivity.class);
                finishAffinity();
                startActivity(i);

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        goToProfileUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Chuyen den profile cua user dang chat

                Bundle bundle = new Bundle();
                bundle.putString("profileid", receiverId);


                MainActivity mainActivity = new MainActivity();
                mainActivity.selectedFragment = new ProfileFragment();
                mainActivity.selectedFragment.setArguments(bundle);
                Intent i = new Intent(getApplicationContext(), mainActivity.getClass());
                i.putExtra("profileid", receiverId);
                startActivity(i);

                //  Toast.makeText(ImageSentGalleryActivity.this, "Click the image", Toast.LENGTH_SHORT).show();
            }

        });

    }
}