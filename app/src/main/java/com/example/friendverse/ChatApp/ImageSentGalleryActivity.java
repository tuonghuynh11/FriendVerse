package com.example.friendverse.ChatApp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.friendverse.Adapter.ImageSentAdapter;
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
    private ImageButton back;
    private RoundedImageView goToProfileUserBtn;
    private RecyclerView imageSentRecyclerViews;
    private List<String> imagesList;
    private ImageSentAdapter imageSentAdapter;

    //Firebase
    private FirebaseAuth auth;
    private DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_sent_gallery);
        currentUser= new User();
        currentUser.setImageurl("https://thuthuatnhanh.com/wp-content/uploads/2022/08/hinh-nen-may-tinh-4k.jpg");
        currentUser.setUsername("Nguyen Van M");


        auth = FirebaseAuth.getInstance();
        back=findViewById(R.id.backButton);
        goToProfileUserBtn=findViewById(R.id.imageProfileBtn);
        imageSentRecyclerViews=findViewById(R.id.imageSentRecyclerView);
        imagesList=new ArrayList<>();
        //Lấy thông tin người nhận tin nhắn
        Intent i = getIntent();
        if (i.getExtras()!=null){
            if (i.getStringExtra("interacter")!=null){
                receiverId= i.getStringExtra("interacter");
                if (i.getStringExtra("interacterImage")!=null){
                    receiverImage=i.getStringExtra("interacterImage");
                }
                Glide.with(this).load(receiverImage).into(goToProfileUserBtn);
                reference = FirebaseDatabase.getInstance().getReference().child("Chats");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                            if ((chatMessage.getSenderId().equals(auth.getUid())&&chatMessage.getReceiverId().equals(receiverId))||(chatMessage.getReceiverId().equals(auth.getUid())&&chatMessage.getSenderId().equals(receiverId))){
                                if (chatMessage.getMessageType().equals("image")){
                                    imagesList.add(chatMessage.getMessage());
                                }
                            }

                        }
                        imageSentAdapter= new ImageSentAdapter(ImageSentGalleryActivity.this, imagesList);
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
                Toast.makeText(ImageSentGalleryActivity.this, "Click the image", Toast.LENGTH_SHORT).show();
            }
        });

    }
}