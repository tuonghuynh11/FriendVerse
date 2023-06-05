package com.example.friendverse;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.github.drjacky.imagepicker.listener.DismissListener;
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
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class AddStoryActivity extends AppCompatActivity {
    Uri imageUri;
    String myUrl;
    Button add;
    Button cancel;
    StorageReference storageReference;
    StorageTask uploadTask;
    private ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);
        storageReference = FirebaseStorage.getInstance().getReference("Stories");
        //CropImage.activity().setAspectRatio(9 , 16).start(AddStoryActivity.this);
        resultLauncher=
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
                    if(result.getResultCode()==RESULT_OK){
                        imageUri=result.getData().getData();
                        uploadImage();
                    }else if(result.getResultCode()== ImagePicker.RESULT_ERROR){
                        // Use ImagePicker.Companion.getError(result.getData()) to show an error
                        Toast.makeText(getApplicationContext(), "No image selected!", Toast.LENGTH_SHORT).show();
                    }
                    else if(result.getResultCode() == RESULT_CANCELED){
                        finish();
                    }
                });
        add = findViewById(R.id.add_story);
        cancel = findViewById(R.id.cancel);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        pickFromGallery();

    }
    private void pickFromGallery() {
        ImagePicker.Companion.with(this)
                .crop()
                .provider(ImageProvider.BOTH).setDismissListener(new DismissListener() {
                    @Override
                    public void onDismiss() {


                        Log.d("ImagePicker", "onDismiss");
                    }
                }) //Or bothCameraGallery()
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
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {



        if (imageUri != null){
            final StorageReference filereference = storageReference.child(System.currentTimeMillis()+ "." + getFileExtension(imageUri));

            uploadTask = filereference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        Toast.makeText(AddStoryActivity.this, "Money Trees", Toast.LENGTH_SHORT).show();

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

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Stories").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        String storyid = reference.push().getKey();

                        HashMap<String , Object> hashMap = new HashMap<>();
                        hashMap.put("storyID" , storyid);
                        hashMap.put("imageURL" , myUrl);
                        hashMap.put("userID" , FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap.put("timeCreated", System.currentTimeMillis());
                        hashMap.put("after1day", System.currentTimeMillis() + 86400000);
                        reference.child(storyid).setValue(hashMap);



                        Toast.makeText(AddStoryActivity.this, "Succeeded!", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(AddStoryActivity.this , MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(AddStoryActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }

    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            uploadImage();

        } else if(resultCode != RESULT_OK) {
            Toast.makeText(this, "Something went wrong , try again!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this , MainActivity.class));
            finish();
        }

    }
}