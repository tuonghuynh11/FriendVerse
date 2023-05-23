package com.example.friendverse.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.Login.ForgetPassEmailActivity;
import com.example.friendverse.R;


public class EmailForgetFragment extends Fragment {

    private Button buttonEmail;
    private EditText forgetEmail;
    //private LoadingDialog loadingDialog = new LoadingDialog();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_email_forget, container, false);

        buttonEmail = v.findViewById(R.id.buttonNextEmailForget);
        forgetEmail = v.findViewById(R.id.emailForgetEditText);

        buttonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //loadingDialog.showDialog();
                String email = forgetEmail.getText().toString();
                if(TextUtils.isEmpty(email)){
                    forgetEmail.setError("Email can't be empty");
                    forgetEmail.requestFocus();
                    //loadingDialog.hideDialog();
                }
            }
        });
        return v;
    }
}