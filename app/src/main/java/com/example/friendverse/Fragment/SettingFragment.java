package com.example.friendverse.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.friendverse.Login.LoginActivity;
import com.example.friendverse.Login.StartActivity;
import com.example.friendverse.MainActivity;
import com.example.friendverse.Profile.SettingActivity;
import com.example.friendverse.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingFragment extends Fragment {

    ImageView close;
    TextView logout, report;
    FirebaseUser firebaseUser;
    public SettingFragment() {
        // Required empty public constructor
    }
    private GoogleSignInClient googleSignInClient;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_setting, container, false);
        report = view.findViewById(R.id.report);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.getUid().equals("tRDwJaJRQNfghpKaFsPH6MtoBcW2")) {
            report.setVisibility(View.VISIBLE);
        }
        else {
            report.setVisibility(View.GONE);
        }
        close = view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new AdminReportFragment();
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            }
        });

        logout = view.findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileFragment.bottomSheetDialog.cancel();
                FirebaseAuth.getInstance().signOut();
                googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN);
                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                        }
                    }
                });
                startActivity(new Intent(view.getContext(), StartActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        return view;
    }
}