package com.example.friendverse.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.friendverse.Login.ForgetPassPhoneActivity;
import com.example.friendverse.R;


public class PhoneForgetFragment extends Fragment {

    private Button buttonPhone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_phone_forget, container, false);

        buttonPhone = v.findViewById(R.id.buttonNextPhoneForget);
        buttonPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ForgetPassPhoneActivity.class);
                startActivity(intent);
            }
        });
        return v;
    }
}