package com.example.book.adapter.admin;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.book.fragment.AdminAccountFragment;
import com.example.book.fragment.AdminAdvertisementFragment;
import com.example.book.fragment.AdminBookFragment;
import com.example.book.fragment.AdminCategoryFragment;
import com.example.book.fragment.AdminFeedbackFragment;

public class AdminViewPagerAdapter extends FragmentStateAdapter {

    public AdminViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new AdminBookFragment();

            case 2:
                return new AdminFeedbackFragment();

            case 3:
                return new AdminAdvertisementFragment();

            case 4:
                return new AdminAccountFragment();

            default:
                return new AdminCategoryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
