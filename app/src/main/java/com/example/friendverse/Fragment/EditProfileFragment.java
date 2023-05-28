package com.example.friendverse.Fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.example.friendverse.TestActivity;

import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import javax.activation.MimeType;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;


public class EditProfileFragment extends Fragment {


    public EditProfileFragment() {
        // Required empty public constructor
    }

    private ImageView imgClose;
    private ImageView imgCheck;
    private TextView changePhoto;
    private CircleImageView avtImage;
    private EditText etUsername;
    private EditText etFullname;
    private EditText etBio;
    private EditText etWebsite;
    private EditText etEmail;
    private EditText etPhone;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> resultLauncher;

    private DatabaseReference reference;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference("avatarImages/");

    private FirebaseAuth mAuth;
    Context applicationContext;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        applicationContext = container.getContext();

        LoadingDialog loadingDialog = new LoadingDialog(applicationContext);
        loadingDialog.showDialog();

        imgClose = v.findViewById(R.id.closeX);
        imgCheck = v.findViewById(R.id.check);
        changePhoto = v.findViewById(R.id.tvChangePhoto);
        avtImage = v.findViewById(R.id.profileAvatar);
        etUsername = v.findViewById(R.id.my_editTextUsername);
        etFullname = v.findViewById(R.id.my_editTextDisplayname);
        etBio = v.findViewById(R.id.my_editTextBio);
        etWebsite = v.findViewById(R.id.my_editTextWebsite);
        etEmail = v.findViewById(R.id.my_editTextEmail);
        etPhone = v.findViewById(R.id.my_editTextPhone);
        registerResult();
        //registerResult2();
        mAuth = FirebaseAuth.getInstance();


        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userID = firebaseUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.child(userID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){

                        DataSnapshot dataSnapshot = task.getResult();
                        if(dataSnapshot.child(User.IMAGEKEY).getValue() != null){
                            String image = String.valueOf(dataSnapshot.child(User.IMAGEKEY).getValue());
                            if(!image.equals("default"))
                                Picasso.get().load(image).into(avtImage);
                        }
                        if(dataSnapshot.child(User.USERNAMEKEY).getValue() != null){
                            String username = String.valueOf(dataSnapshot.child(User.USERNAMEKEY).getValue());
                            etUsername.setText(username);
                        }
                        if(dataSnapshot.child(User.FULLNAMEKEY).getValue() != null){
                            String username = String.valueOf(dataSnapshot.child(User.FULLNAMEKEY).getValue());
                            etFullname.setText(username);
                        }
                        if(dataSnapshot.child(User.BIOKEY).getValue() != null){
                            String username = String.valueOf(dataSnapshot.child(User.BIOKEY).getValue());
                            etBio.setText(username);
                        }
                        if(dataSnapshot.child(User.WEBSITEKEY).getValue() != null){
                            String username = String.valueOf(dataSnapshot.child(User.WEBSITEKEY).getValue());
                            etWebsite.setText(username);
                        }
                        if(dataSnapshot.child(User.EMAILOKEY).getValue() != null){
                            String username = String.valueOf(dataSnapshot.child(User.EMAILOKEY).getValue());
                            etEmail.setText(username);
                        }
                        if(dataSnapshot.child(User.PHONEKEY).getValue() != null){
                            String username = String.valueOf(dataSnapshot.child(User.PHONEKEY).getValue());
                            etPhone.setText(username);
                        }

                        loadingDialog.hideDialog();
                    }


                }
                else{

                }
            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        imgCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.showDialog();
                if(imageUri != null){
                    uploadToFirebase(imageUri);
                }
                else{
                    Toast.makeText(getActivity(), "No image", Toast.LENGTH_SHORT).show();
                }
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                String userID = firebaseUser.getUid();
                reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                reference.child(User.FULLNAMEKEY).setValue(etFullname.getText().toString());
                reference.child(User.USERNAMEKEY).setValue(etUsername.getText().toString());
                reference.child(User.BIOKEY).setValue(etBio.getText().toString());
                reference.child(User.WEBSITEKEY).setValue(etWebsite.getText().toString());
                reference.child(User.EMAILOKEY).setValue(etEmail.getText().toString());
                reference.child(User.PHONEKEY).setValue(etPhone.getText().toString());
                loadingDialog.hideDialog();
                Toast.makeText(getActivity(), "Changes done", Toast.LENGTH_SHORT).show();

            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                cameraIntent.setType("image/*");
//                resultLauncher.launch(cameraIntent);
                boolean pick = true;
                if (pick == true) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();

                    } else {
                        pickFromGallery();

                    }
                } else{
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });


        return v;
    }

    private void registerResult(){

        resultLauncher=
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                    if(result.getResultCode()==RESULT_OK){
                        imageUri=result.getData().getData();
                        avtImage.setImageURI(imageUri);
                    }else if(result.getResultCode()== ImagePicker.RESULT_ERROR){
                        // Use ImagePicker.Companion.getError(result.getData()) to show an error
                        Toast.makeText(getActivity(), "No image selected!", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void uploadToFirebase(Uri uri){
        final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        imageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userID = firebaseUser.getUid();
                        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
                        reference.child(User.IMAGEKEY).setValue(uri.toString());
                    }
                });
            }
        });
    }

    private String getFileExtension(Uri fileUri){
        ContentResolver contentResolver = applicationContext.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }

    private void pickFromGallery() {
        ImagePicker.Companion.with(this.requireActivity())
                .crop()
                .cropOval()
                .maxResultSize(512,512,true)
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

    private Boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return true;
    }

    private void requestStoragePermission() {
        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST);
    }

    private Boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result || result1;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST);
    }

}