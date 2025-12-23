package com.example.book.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.book.R;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityRegisterBinding;
import com.example.book.model.User;
import com.example.book.prefs.DataStoreManager;
import com.example.book.utils.StringUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.book.constant.GlobalFunction.showToastMessage;

public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding mActivityRegisterBinding;
    private boolean isEnableButtonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityRegisterBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(mActivityRegisterBinding.getRoot());

        initListener();
    }

    private void initListener() {
        mActivityRegisterBinding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    mActivityRegisterBinding.edtEmail.setBackgroundResource(R.drawable.bg_white_corner_30_border_main);
                } else {
                    mActivityRegisterBinding.edtEmail.setBackgroundResource(R.drawable.bg_white_corner_30_border_gray);
                }

                String strPassword = mActivityRegisterBinding.edtPassword.getText().toString().trim();
                if (!StringUtil.isEmpty(s.toString()) && !StringUtil.isEmpty(strPassword)) {
                    isEnableButtonRegister = true;
                    mActivityRegisterBinding.btnRegister.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonRegister = false;
                    mActivityRegisterBinding.btnRegister.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        mActivityRegisterBinding.edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!StringUtil.isEmpty(s.toString())) {
                    mActivityRegisterBinding.edtPassword.setBackgroundResource(R.drawable.bg_white_corner_30_border_main);
                } else {
                    mActivityRegisterBinding.edtPassword.setBackgroundResource(R.drawable.bg_white_corner_30_border_gray);
                }

                String strEmail = mActivityRegisterBinding.edtEmail.getText().toString().trim();
                if (!StringUtil.isEmpty(s.toString()) && !StringUtil.isEmpty(strEmail)) {
                    isEnableButtonRegister = true;
                    mActivityRegisterBinding.btnRegister.setBackgroundResource(R.drawable.bg_button_enable_corner_10);
                } else {
                    isEnableButtonRegister = false;
                    mActivityRegisterBinding.btnRegister.setBackgroundResource(R.drawable.bg_button_disable_corner_10);
                }
            }
        });

        mActivityRegisterBinding.layoutLogin.setOnClickListener(v -> finish());
        mActivityRegisterBinding.btnRegister.setOnClickListener(v -> onClickValidateRegister());
    }

    private void onClickValidateRegister() {
        if (!isEnableButtonRegister) return;

        String strEmail = mActivityRegisterBinding.edtEmail.getText().toString().trim();
        String strPassword = mActivityRegisterBinding.edtPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            showToastMessage(this, getString(R.string.msg_email_require));
        } else if (StringUtil.isEmpty(strPassword)) {
            showToastMessage(this, getString(R.string.msg_password_require));
        } else if (!StringUtil.isValidEmail(strEmail)) {
            showToastMessage(this, getString(R.string.msg_email_invalid));
        } else {
            registerUserFirebase(strEmail, strPassword);
        }
    }

    private void registerUserFirebase(String email, String password) {
        showProgressDialog(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            User userObject = new User(user.getEmail(), password);
                            if (user.getEmail() != null && user.getEmail().contains(Constant.ADMIN_EMAIL_FORMAT)) {
                                userObject.setAdmin(true);
                            }
                            DataStoreManager.setUser(userObject);
                            goToMainActivity();
                        }
                    } else {
                        showToastMessage(this, getString(R.string.msg_register_error));
                    }
                });
    }

    private void goToMainActivity() {
        GlobalFunction.startActivity(RegisterActivity.this, MainActivity.class);
        finishAffinity();
    }
}