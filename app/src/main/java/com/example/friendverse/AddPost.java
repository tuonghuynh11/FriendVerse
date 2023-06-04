package com.example.friendverse;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AddPost extends AppCompatActivity {
    ImageView postImage;
    VideoView video;
    Uri imageUri;
    String myUrl;
    Button videoButton;
    Button logout;
    Button post;
    EditText description;
    FirebaseUser user;
    StorageReference storageReference;
    UploadTask uploadTask;
    AlertDialog.Builder alertBuilder;
    ActivityResultLauncher mediaLauncher;
    Uri uri;
    @SuppressLint("ClickableViewAccessibility")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        postImage = findViewById(R.id.imageView);
        postImage.setClipToOutline(true);
        logout = findViewById(R.id.logoutBtn);
        post = findViewById(R.id.postBTN);
        description = findViewById(R.id.description);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Posts");
        alertBuilder = new AlertDialog.Builder(this);
        videoButton = findViewById(R.id.VideoButton);
        video = findViewById(R.id.videoView);
        mediaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if(data != null){
                        uri = data.getData();
                        if(uri != null){

                            if(uri.toString().contains("video")){
                                video.setVisibility(View.VISIBLE);
                                postImage.setImageURI(null);
                                postImage.setVisibility(View.GONE);
                                video.setVideoURI(uri);
                                video.start();
                            }

                        }
                    }
                }

            }
        });

        user = auth.getCurrentUser();
        if(user == null){
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();

            }
        });
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertBuilder.setTitle("Change image");
                alertBuilder.setMessage("Do you want to change the image?");
                alertBuilder.setCancelable(true);
                alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CropImage.activity().start(AddPost.this);
                    }
                });
                alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog alertDialog = alertBuilder.create();
                alertDialog.show();

            }
        });
        video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                alertBuilder.setTitle("Change to video");
                alertBuilder.setMessage("Do you want to change to video?");
                alertBuilder.setCancelable(true);
                alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("video/*");
                        mediaLauncher.launch(intent);
                    }
                });
                alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
                return false;
            }

        });
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(videoButton.getText().toString()){
                    case "Video":
                    {
                        alertBuilder.setTitle("Change to video");
                        alertBuilder.setMessage("Do you want to change to video?");
                        alertBuilder.setCancelable(true);
                        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                postImage.setVisibility(View.GONE);
                                video.setVisibility(View.VISIBLE);
                                videoButton.setText("Image");


                            }
                        });
                        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog alertDialog = alertBuilder.create();
                        alertDialog.show();
                        break;
                    }
                    case "Image":{
                        alertBuilder.setTitle("Change image");
                        alertBuilder.setMessage("Do you want to change to image?");
                        alertBuilder.setCancelable(true);
                        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                video.setVisibility(View.GONE);
                                postImage.setVisibility(View.VISIBLE);
                                videoButton.setText("Video");
                            }
                        });
                        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        AlertDialog alertDialog = alertBuilder.create();
                        alertDialog.show();

                    }
                }


            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoButton.getText().toString() == "Video")
                    uploadImage();
                else
                    uploadVideo();

            }
        });
        CropImage.activity().start(AddPost.this);


    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            postImage.setImageURI(imageUri);
        } else if(resultCode != RESULT_OK) {
            Toast.makeText(this, "Something went wrong , try again!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddPost.this , MainActivity.class));
            finish();
        }

    }
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    void uploadVideo(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting video");
        progressDialog.show();

        if (uri != null){
            final StorageReference filereference = storageReference.child(System.currentTimeMillis()+ "." + getFileExtension(uri));

            uploadTask = filereference.putFile(uri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        Toast.makeText(AddPost.this, "Money Trees", Toast.LENGTH_SHORT).show();

                        throw task.getException();
                    }

                    return  filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = reference.push().getKey();

                        HashMap<String , Object> hashMap = new HashMap<>();
                        hashMap.put("createdTime", System.currentTimeMillis());
                        hashMap.put("postType","video");
                        hashMap.put("postid" , postid);
                        hashMap.put("postvid" , myUrl);
                        hashMap.put("description" , description.getText().toString());
                        hashMap.put("publisher" , FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference.child(postid).setValue(hashMap);



                        progressDialog.dismiss();

                        startActivity(new Intent(AddPost.this , MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(AddPost.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            });
        } else {
            Toast.makeText(this, "No videos selected!", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadImage() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if (imageUri != null){
            final StorageReference filereference = storageReference.child(System.currentTimeMillis()+ "." + getFileExtension(imageUri));

            uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        Toast.makeText(AddPost.this, "Money Trees", Toast.LENGTH_SHORT).show();

                        throw task.getException();
                    }

                    return  filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = reference.push().getKey();

                        HashMap<String , Object> hashMap = new HashMap<>();
                        hashMap.put("postType","image");
                        hashMap.put("postid" , postid);
                        hashMap.put("postimage" , myUrl);
                        hashMap.put("description" , description.getText().toString());
                        hashMap.put("publisher" , FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("createdTime", System.currentTimeMillis());
                        reference.child(postid).setValue(hashMap);



                        progressDialog.dismiss();

                        startActivity(new Intent(AddPost.this , MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(AddPost.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPost.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            });
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }

    }
}