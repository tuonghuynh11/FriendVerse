package com.example.friendverse.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.friendverse.Fragment.EmailForgetFragment;
import com.example.friendverse.Fragment.EmailFragment;
import com.example.friendverse.Fragment.PhoneForgetFragment;
import com.example.friendverse.Fragment.PhoneFragment;

public class ViewPagerAdapterForget extends FragmentStateAdapter {
    public ViewPagerAdapterForget(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 1){
            return new PhoneForgetFragment();
        }
        return new EmailForgetFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
