package com.example.friendverse;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class AddReelActivity extends AppCompatActivity {
    VideoView reel;
    SocialAutoCompleteTextView content;
    ActivityResultLauncher mediaLauncher;
    StorageReference storageReference;
    StorageTask uploadTask;
    String myUrl;
    Uri uri;
    Button add;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reel);
        storageReference = FirebaseStorage.getInstance().getReference("Reels").child(FirebaseAuth.getInstance().getCurrentUser().getUid());;
        reel = findViewById(R.id.reel);
        add = findViewById(R.id.reelButton);
        content = findViewById(R.id.descriptionReel);
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
                                try {
                                    long x = getVideoDuration(uri);
                                    Toast.makeText(AddReelActivity.this, x + " milliseconds", Toast.LENGTH_SHORT).show();
                                    if(x < 15000){
                                        reel.setVideoURI(uri);
                                        reel.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mediaPlayer) {
                                                mediaPlayer.setLooping(true);
                                                mediaPlayer.start();
                                                float videoRatio = mediaPlayer.getVideoWidth()/(float)mediaPlayer.getVideoHeight();
                                                float screenRatio = reel.getWidth()/(float)reel.getHeight();
                                                float scale = videoRatio/screenRatio;
                                                if(scale >= 1f){
                                                    reel.setScaleX(scale);
                                                }
                                                else {
                                                    reel.setScaleY(1f/scale);
                                                }
                                            }
                                        });
                                        reel.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
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


                                    //content.setText(x + "");

                                } catch (IOException e) {
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }



                            }

                        }
                    }
                }

            }
        });
        reel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AddReelActivity.this);
                alertBuilder.setTitle("Change the video");
                alertBuilder.setMessage("Do you want to change the video?");
                alertBuilder.setCancelable(true);
                alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("video/*");
                        dialogInterface.dismiss();
                        mediaLauncher.launch(intent);
                    }
                });
                alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();


                    }
                });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
                return true;
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uri != null){
                    uploadVideo();

                }
            }
        });
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        mediaLauncher.launch(intent);



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

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    void uploadVideo(){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if (uri != null){
            final StorageReference filereference = storageReference.child(System.currentTimeMillis()+ "." + getFileExtension(uri));

            uploadTask = filereference.putFile(uri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "Money Trees", Toast.LENGTH_SHORT).show();

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
                        hashMap.put("postid" , postid);
                        hashMap.put("postvid" , myUrl);
                        hashMap.put("description" , content.getText().toString());
                        hashMap.put("publisher" , FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("createdTime", System.currentTimeMillis());
                        hashMap.put("postType", "video");

                        reference.child(postid).setValue(hashMap);
                        DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                        List<String> hashtags = content.getHashtags();
                        if(!hashtags.isEmpty()){
                            for(String hashtag : hashtags){
                                hashMap.clear();
                                hashMap.put("tag" , hashtag.toLowerCase());
                                hashMap.put("postid" , postid);

                                mHashTagRef.child(hashtag.toLowerCase()).child(postid).setValue(hashMap);

                            }
                        }



                        progressDialog.dismiss();

                        startActivity(new Intent(getApplicationContext() , MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No videos selected!", Toast.LENGTH_SHORT).show();
        }
    }
}
