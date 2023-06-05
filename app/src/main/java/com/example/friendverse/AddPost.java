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
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
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

import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
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
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class AddPost extends AppCompatActivity {
    ImageView postImage;
    VideoView video;
    Uri imageUri;
    String myUrl;
    Button videoButton;
    Button logout;
    Button post;
    SocialAutoCompleteTextView description;
    FirebaseUser user;
    StorageReference storageReference;
    UploadTask uploadTask;
    AlertDialog.Builder alertBuilder;
    ProgressDialog progressDialog;
    ActivityResultLauncher mediaLauncher;
    ActivityResultLauncher<Intent> resultLauncher;
    Uri uri;
    @SuppressLint("ClickableViewAccessibility")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        postImage = findViewById(R.id.imageView);
        postImage.setClipToOutline(true);
        //logout = findViewById(R.id.logoutBtn);
        post = findViewById(R.id.postBTN);
        description = findViewById(R.id.description);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Posts");
        alertBuilder = new AlertDialog.Builder(this);
        videoButton = findViewById(R.id.VideoButton);
        video = findViewById(R.id.videoView);
        resultLauncher=
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                    if(result.getResultCode()==RESULT_OK){
                        imageUri=result.getData().getData();
                        postImage.setImageURI(imageUri);
                    }else if(result.getResultCode()== ImagePicker.RESULT_ERROR){
                        // Use ImagePicker.Companion.getError(result.getData()) to show an error
                        Toast.makeText(getApplicationContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                    }
                });
        mediaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if(data != null){
                        uri = data.getData();
                        if(uri != null){
                            ContentResolver contentResolver = getApplicationContext().getContentResolver();
                            String type = contentResolver.getType(uri);

                            if(type.startsWith("video/")){
                                video.setVisibility(View.VISIBLE);
                                postImage.setImageURI(null);
                                postImage.setVisibility(View.GONE);
                                try {
                                    long x = getVideoDuration(uri);
                                    if(x < 15000){
                                        video.setVideoURI(uri);
                                        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mediaPlayer) {
                                                mediaPlayer.setLooping(true);
                                                mediaPlayer.start();
                                                float videoRatio = mediaPlayer.getVideoWidth()/(float)mediaPlayer.getVideoHeight();
                                                float screenRatio = video.getWidth()/(float)video.getHeight();
                                                float scale = videoRatio/screenRatio;
                                                if(scale >= 1f){
                                                    video.setScaleX(scale);
                                                }
                                                else {
                                                    video.setScaleY(1f/scale);
                                                }
                                            }
                                        });
                                        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mediaPlayer) {
                                                mediaPlayer.start();
                                            }
                                        });
                                    }
                                    else
                                    {
                                        uri = null;
                                        Toast.makeText(getApplicationContext(), "The video limit is 15 seconds", Toast.LENGTH_LONG).show();
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                video.setVideoURI(uri);

                                //post.setEnabled(true);

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
        /*logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();

            }
        });*/
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertBuilder.setTitle("Change image");
                alertBuilder.setMessage("Do you want to change the image?");
                alertBuilder.setCancelable(true);
                alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //CropImage.activity().start(AddPost.this);
                        pickFromGallery();
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
                alertBuilder.setTitle("Change the video");
                alertBuilder.setMessage("Do you want to change the video?");
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
                                postImage.setImageURI(null);
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
                        alertBuilder.setTitle("Change to image");
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
                        break;

                    }
                }
                //post.setEnabled(false);



            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(videoButton.getText().toString().equals("Video"))
                {
                    uploadImage();
                }
                else
                    uploadVideo();

            }
        });
        //CropImage.activity().start(AddPost.this);
        pickFromGallery();


    }
    private void pickFromGallery() {
        ImagePicker.Companion.with(this)
                .crop()
                .cropSquare()
                .provider(ImageProvider.BOTH) //Or bothCameraGallery()
                .createIntentFromDialog((Function1)(new Function1(){
                    public Object invoke(Object var1){
                        this.invoke((Intent)var1);
                        return Unit.INSTANCE;
                    }

                    public final void invoke(@NotNull Intent it){
                        Intrinsics.checkNotNullParameter(it,"it");
                        resultLauncher.launch(it);
                    }
                }));
    }
    private long getVideoDuration(Uri URI) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(getApplicationContext(), URI);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);

        retriever.release();
        return timeInMillisec;
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            postImage.setImageURI(imageUri);
            //post.setEnabled(true);
        } else if(resultCode != RESULT_OK) {
            Toast.makeText(this, "Something went wrong , try again!", Toast.LENGTH_SHORT).show();
            //startActivity(new Intent(AddPost.this , MainActivity.class));
            finish();
        }

    }
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    void uploadVideo(){
        progressDialog = new ProgressDialog(this);
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
                        hashMap.put("createdTime", System.currentTimeMillis());
                        hashMap.put("postType", "video");

                        reference.child(postid).setValue(hashMap);
                        DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                        List<String> hashtags = description.getHashtags();
                        if(!hashtags.isEmpty()){
                            for(String hashtag : hashtags){
                                hashMap.clear();
                                hashMap.put("tag" , hashtag.toLowerCase());
                                hashMap.put("postid" , postid);

                                mHashTagRef.child(hashtag.toLowerCase()).child(postid).setValue(hashMap);

                            }
                        }



                        progressDialog.dismiss();

                       // startActivity(new Intent(AddPost.this , MainActivity.class));
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
                        hashMap.put("postType", "image");

                        reference.child(postid).setValue(hashMap);
                        DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                        List<String> hashtags = description.getHashtags();
                        if(!hashtags.isEmpty()){
                            for(String hashtag : hashtags){
                                hashMap.clear();
                                hashMap.put("tag" , hashtag.toLowerCase());
                                hashMap.put("postid" , postid);

                                mHashTagRef.child(hashtag.toLowerCase()).child(postid).setValue(hashMap);

                            }
                        }



                        progressDialog.dismiss();

                        //startActivity(new Intent(AddPost.this , MainActivity.class));
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