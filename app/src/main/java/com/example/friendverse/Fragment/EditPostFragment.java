package com.example.friendverse.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.friendverse.Model.Post;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class EditPostFragment extends Fragment {

    private ImageView back, post_image, image_profile;
    private TextView username, description;
    private EditText field_editdesc;
    private Button edit_btn;
    private FirebaseUser firebaseUser;
    private String postid;
    private Post post;
    private User user;
    public EditPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_post, container, false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        postid = getArguments().getString("postid");

        username = view.findViewById(R.id.username);
        description = view.findViewById(R.id.description);
        post_image = view.findViewById(R.id.post_image);
        field_editdesc = view.findViewById(R.id.field_editdesc);
        image_profile = view.findViewById(R.id.image_profile);

        getInfoPost();
        getInfoUser();

        edit_btn = view.findViewById(R.id.edit_btn);
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts").child(postid);
                if (reference == null) {
                    Toast.makeText(view.getContext(), "Post is not available", Toast.LENGTH_SHORT).show();
                }
                else {
                    reference.child("description").setValue(field_editdesc.getText().toString());
                    Toast.makeText(view.getContext(), "Successfull", Toast.LENGTH_SHORT).show();
                    field_editdesc.setText("");
                }
            }
        });
        back = view.findViewById(R.id.close);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        return view;
    }

    private void getInfoPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                post = new Post();
                post = snapshot.getValue(Post.class);
                description.setText(post.getDescription());
                if (post.getDescription() == null || post.getDescription().equals("")) {
                    field_editdesc.setText("");
                }
                else {
                    field_editdesc.setText(post.getDescription());
                }
                Picasso.get().load(post.getPostimage()).placeholder(R.mipmap.ic_launcher).into(post_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getInfoUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = new User();
                user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                Picasso.get().load(user.getImageurl()).placeholder(R.mipmap.ic_launcher).into(image_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}