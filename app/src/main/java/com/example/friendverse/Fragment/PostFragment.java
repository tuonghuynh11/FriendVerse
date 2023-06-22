package com.example.friendverse.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.friendverse.AddPost;
import com.example.friendverse.MainActivity;
import com.example.friendverse.R;
import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
//import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
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
    ActivityResultLauncher mediaLauncher;
    private ActivityResultLauncher<Intent> resultLauncher;
    Uri uri;

    public PostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostFragment newInstance(String param1, String param2) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_post, container, false);
        postImage = v.findViewById(R.id.imageView);
        video = v.findViewById(R.id.videoView);
        //logout = v.findViewById(R.id.logoutBtn);
        post = v.findViewById(R.id.postBTN);
        description = v.findViewById(R.id.description);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Posts");
        alertBuilder = new AlertDialog.Builder(getActivity());
        videoButton = v.findViewById(R.id.VideoButton);
        resultLauncher=
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                    if(result.getResultCode()==RESULT_OK){
                        imageUri=result.getData().getData();
                        postImage.setImageURI(imageUri);
                    }else if(result.getResultCode()== ImagePicker.RESULT_ERROR){
                        // Use ImagePicker.Companion.getError(result.getData()) to show an error
                        Toast.makeText(getActivity(), "No image selected!", Toast.LENGTH_SHORT).show();
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
                            ContentResolver contentResolver = getActivity().getContentResolver();
                            String type = contentResolver.getType(uri);

                            if(type.contains("video/")){
                                video.setVisibility(View.VISIBLE);
                                postImage.setImageURI(null);
                                postImage.setVisibility(View.GONE);
                                video.setVideoURI(uri);
                                video.start();
                                try {
                                    long x = getVideoDuration(uri);
                                    if(x < 15000){
                                        video.setVideoURI(uri);
                                        video.start();
                                    }
                                    else
                                    {
                                        Toast.makeText(getActivity(), "The video limit is 15 seconds", Toast.LENGTH_LONG);
                                    }

                                    description.setText(x + "");

                                } catch (IOException e) {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                    }
                }

            }
        });
        user = auth.getCurrentUser();
        if(user == null){
            Intent i = new Intent(getContext(), MainActivity.class);
            startActivity(i);
        }
        /*logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
                getActivity().finish();

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
        //Click to the videoView will sent this
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
                if(videoButton.getText().toString().equals("Video"))
                    uploadImage();
                else
                    uploadVideo();

            }
        });
        pickFromGallery();
        return v;
    }
    private long getVideoDuration(Uri URI) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(getContext(), URI);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);

        retriever.release();
        return timeInMillisec;
    }

    private void pickFromGallery() {
        ImagePicker.Companion.with(this.requireActivity())
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
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    void uploadVideo(){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Posting video");
        progressDialog.show();

        if (uri != null){
            final StorageReference filereference = storageReference.child(System.currentTimeMillis()+ "." + getFileExtension(uri));

            uploadTask = filereference.putFile(uri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        Toast.makeText(getActivity(), "Money Trees", Toast.LENGTH_SHORT).show();

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

                        startActivity(new Intent(getActivity() , MainActivity.class));
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            });
        } else {
            Toast.makeText(getActivity(), "No videos selected!", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadImage() {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if (imageUri != null){
            final StorageReference filereference = storageReference.child(System.currentTimeMillis()+ "." + getFileExtension(imageUri));

            uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        Toast.makeText(getActivity(), "Money Trees", Toast.LENGTH_SHORT).show();

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
                        hashMap.put("postimage" , myUrl);
                        hashMap.put("description" , description.getText().toString());
                        hashMap.put("publisher" , FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("createdTime", System.currentTimeMillis());
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

                        startActivity(new Intent(getActivity() , MainActivity.class));
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            });
        } else {
            Toast.makeText(getActivity(), "No image selected!", Toast.LENGTH_SHORT).show();
        }

    }
}
