package com.example.book.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import com.example.book.R;
import com.example.book.databinding.ActivityForgotPasswordBinding;
import com.example.book.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends BaseActivity {

    private ActivityForgotPasswordBinding mActivityForgotPasswordBinding;
    private boolean isEnableButtonResetPassword;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityForgotPasswordBinding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(mActivityForgotPasswordBinding.getRoot());

        initToolbar();
        initListener();
    }

    private void initToolbar() {
        mActivityForgotPasswordBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        mActivityForgotPasswordBinding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_reset_password));
    }

    private void initListener() {
        mActivityForgotPasswordBinding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    mActivityForgotPasswordBinding.edtEmail.setBackgroundResource(R.drawable.bg_white_corner_30_border_main);
                } else {
                    mActivityForgotPasswordBinding.edtEmail.setBackgroundResource(R.drawable.bg_white_corner_30_border_gray);
                }

                if (!StringUtil.isEmpty(s.toString())) {
                    isEnableButtonResetPassword = true;
                    mActivityForgotPasswordBinding.btnResetPassword.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonResetPassword = false;
                    mActivityForgotPasswordBinding.btnResetPassword.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        mActivityForgotPasswordBinding.btnResetPassword.setOnClickListener(v -> onClickValidateResetPassword());
    }

    private void onClickValidateResetPassword() {
        if (!isEnableButtonResetPassword) return;
        String strEmail = mActivityForgotPasswordBinding.edtEmail.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            Toast.makeText(ForgotPasswordActivity.this,
                    getString(R.string.msg_email_require), Toast.LENGTH_SHORT).show();
        } else if (!StringUtil.isValidEmail(strEmail)) {
            Toast.makeText(ForgotPasswordActivity.this,
                    getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT).show();
        } else {
            resetPassword(strEmail);
        }
    }

    private void resetPassword(String email) {
        showProgressDialog(true);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                getString(R.string.msg_reset_password_successfully),
                                Toast.LENGTH_SHORT).show();
                        mActivityForgotPasswordBinding.edtEmail.setText("");
                    }
                });
    }
}