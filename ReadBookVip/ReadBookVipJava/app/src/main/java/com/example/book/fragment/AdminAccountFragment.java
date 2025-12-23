package com.example.book.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.book.activity.ChangePasswordActivity;
import com.example.book.activity.LoginActivity;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.FragmentAdminAccountBinding;
import com.example.book.prefs.DataStoreManager;
import com.google.firebase.auth.FirebaseAuth;

public class AdminAccountFragment extends Fragment {

    private FragmentAdminAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminAccountBinding.inflate(inflater, container, false);
        initUi();
        return binding.getRoot();
    }

    private void initUi() {
        binding.tvEmail.setText(DataStoreManager.getUser().getEmail());
        binding.tvChangePassword.setOnClickListener(v -> onClickChangePassword());
        binding.tvSignOut.setOnClickListener(v -> onClickSignOut());
    }

    private void onClickChangePassword() {
        GlobalFunction.startActivity(getActivity(), ChangePasswordActivity.class);
    }

    private void onClickSignOut() {
        if (getActivity() == null) return;
        FirebaseAuth.getInstance().signOut();
        DataStoreManager.setUser(null);
        GlobalFunction.startActivity(getActivity(), LoginActivity.class);
        getActivity().finishAffinity();
    }
}
