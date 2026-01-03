package com.example.book.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.book.databinding.FragmentAdminAdvertisementBinding;
import com.google.android.material.tabs.TabLayout;

public class AdminAdvertisementFragment extends Fragment {

    private FragmentAdminAdvertisementBinding binding;
    private AdminAdvertisementListFragment listFragment;
    private AdminAdvertisementStatsFragment statsFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminAdvertisementBinding.inflate(inflater, container, false);

        initView();
        initListener();

        return binding.getRoot();
    }

    private void initView() {
        listFragment = new AdminAdvertisementListFragment();
        statsFragment = new AdminAdvertisementStatsFragment();

        // Load default fragment (List)
        loadFragment(listFragment);
        
        // Select first tab by default
        if (binding.tabLayout.getTabCount() > 0) {
            binding.tabLayout.getTabAt(0).select();
        }
    }

    private void initListener() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadFragment(listFragment);
                } else {
                    loadFragment(statsFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        if (getActivity() == null) return;
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment);
        fragmentTransaction.commit();
    }
}




