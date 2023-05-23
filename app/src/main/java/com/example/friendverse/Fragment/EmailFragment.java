package com.example.friendverse.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.friendverse.DialogLoadingBar.LoadingDialog;
import com.example.friendverse.MailService.GMailSender;
import com.example.friendverse.Login.EmailConfirmActivity;
import com.example.friendverse.R;

import java.util.Objects;


public class EmailFragment extends Fragment {

    private Button buttonEmail;
    private EditText etEmail;
    //private LoadingDialog loadingDialog = new LoadingDialog();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_email, container, false);
        buttonEmail = v.findViewById(R.id.buttonNextEmail);
        etEmail = v.findViewById(R.id.emailEditText);

        buttonEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //.showDialog();
                String emailSender = "friendverse123@gmail.com";
                String passEmail = "jqnzillisugvmied";
                String emailReceive = etEmail.getText().toString();

                int rand_num = (int)Math.floor(Math.random() * (1000000 - 100000 + 1) + 100000);
//                sendEmailOTP(emailReceive, emailSender, passEmail, rand_num);

                try{
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                    StrictMode.setThreadPolicy(policy);

                    //Chinh sua lai de gui mail ve edittext duoc nhap
                    GMailSender sender = new GMailSender(emailSender, passEmail);
                    sender.sendMail("OTP to verify email", "This is OTP code for verification your email: " + Integer.toString(rand_num), emailSender, emailReceive);
                    Toast.makeText(getActivity(),"OTP sent!",Toast.LENGTH_SHORT).show();

                }
                catch (Exception e){
                    Log.e("SendMail: ", e.getMessage(), e);
                }
                //loadingDialog.hideDialog();
                Intent intent = new Intent(getActivity(), EmailConfirmActivity.class);
                intent.putExtra("Email", emailReceive);
                intent.putExtra("OTPCode", rand_num);
                startActivity(intent);
            }
        });
        return v;
    }

//    public void sendEmailOTP(String to_email, String from_email, String password, int OTP){
//        try {
//            String stringHost = "smtp.gmail.com";
//            Properties props = System.getProperties();
//            props.setProperty("mail.transport.protocol", "smtp");
//            props.setProperty("mail.host", stringHost);
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.port", "465");
//            props.put("mail.smtp.socketFactory.port", "465");
//            props.put("mail.smtp.socketFactory.class",
//                    "javax.net.ssl.SSLSocketFactory");
//            props.put("mail.smtp.socketFactory.fallback", "false");
//            props.setProperty("mail.smtp.quitwait", "false");
//
//
//            javax.mail.Session session = Session.getInstance(props, new Authenticator() {
//                @Override
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication(from_email, password);
//                }
//            });
//
//            MimeMessage mimeMessage = new MimeMessage(session);
//
//            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to_email));
//
//            mimeMessage.setSubject("OTP for email verification");
//
//            mimeMessage.setText("This is an OTP for your verification: " + Integer.toString(OTP));
//
//            Thread thread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Transport.send(mimeMessage);
//                    } catch (MessagingException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            });
//            thread.start();
//
//        } catch (AddressException e) {
//            throw new RuntimeException(e);
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
//    }
}