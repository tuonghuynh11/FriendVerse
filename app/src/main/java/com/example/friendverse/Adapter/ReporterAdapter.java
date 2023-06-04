package com.example.friendverse.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendverse.Fragment.DetailReportUserFragment;
import com.example.friendverse.Fragment.ProfileFragment;
import com.example.friendverse.Fragment.ReportUserFragment;
import com.example.friendverse.Model.Reporter;
import com.example.friendverse.Model.User;
import com.example.friendverse.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReporterAdapter extends  RecyclerView.Adapter<ReporterAdapter.ViewHolder>{
    private Context sContext;
    private List<User> sUser;
    private List<Reporter> sReporter;
    private List<String> idReport;
    private FirebaseUser firebaseUser;
    private OnItemClickListener onItemClickListener;

    View view;
    public ReporterAdapter(Context sContext, List<User> sUser, List<Reporter> sReporter) {
        this.sContext = sContext;
        this.sUser = sUser;
        this.sReporter = sReporter;
    }
    public interface OnItemClickListener {
        void onItemClick(Reporter reporter);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(sContext).inflate(R.layout.report_item,parent,false);
        filterListUser();
        return  new ReporterAdapter.ViewHolder(view);
    }

    private void filterListUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sUser.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    User user = item.getValue(User.class);
                    sUser.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (sUser.size() == 0 || sReporter.size() == 0) {
            return;
        }
        idReport.clear();
        for (Reporter reporter : sReporter) {
            idReport.add(reporter.getReporter());
        }
        sUser = sUser.stream().filter(u -> idReport.contains(u.getId())).collect(Collectors.toList());
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Reporter reporter = sReporter.get(position);

        holder.username.setText(reporter.getUsername());
        holder.report.setText(reporter.getReport());

        String img ="https://vapa.vn/wp-content/uploads/2022/12/anh-3d-thien-nhien.jpeg";
        for (User user : sUser) {
            if (user.getId().equals(reporter.getReporter())) {
                img = user.getImageurl();
                break;
            }
        }

        Picasso.get().load(img).placeholder(R.mipmap.ic_launcher).into(holder.imageProfile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Bundle passData = new Bundle();
//                passData.putString("profileid", user.getId());
//                Fragment detailReportUserFragment = new DetailReportUserFragment();
//                detailReportUserFragment.setArguments(passData);
//                FragmentManager fragmentManager = ((AppCompatActivity)sContext).getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.fragment_container, detailReportUserFragment).addToBackStack(null);
//                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return sReporter.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imageProfile;
        public TextView username;
        public TextView report;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            report = itemView.findViewById(R.id.report);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Reporter reporter = sReporter.get(position);
                            onItemClickListener.onItemClick(reporter);
                        }
                    }
                }
            });
        }
    }
}
