package com.example.book.activity;

import android.os.Bundle;

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityFeedbackBinding;
import com.example.book.model.Feedback;
import com.example.book.prefs.DataStoreManager;
import com.example.book.utils.StringUtil;

public class FeedbackActivity extends BaseActivity {

    private ActivityFeedbackBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initToolbar();
        initData();
    }

    private void initToolbar() {
        mBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        mBinding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_feedback));
    }

    private void initData() {
        mBinding.edtEmail.setText(DataStoreManager.getUser().getEmail());
        mBinding.tvSendFeedback.setOnClickListener(v -> onClickSendFeedback());
    }

    private void onClickSendFeedback() {
        String strName = mBinding.edtName.getText().toString();
        String strPhone = mBinding.edtPhone.getText().toString();
        String strEmail = mBinding.edtEmail.getText().toString();
        String strComment = mBinding.edtComment.getText().toString();

        if (StringUtil.isEmpty(strName)) {
            GlobalFunction.showToastMessage(this, getString(R.string.msg_name_require));
        } else if (StringUtil.isEmpty(strComment)) {
            GlobalFunction.showToastMessage(this, getString(R.string.msg_comment_require));
        } else {
            showProgressDialog(true);
            Feedback feedback = new Feedback(strName, strPhone, strEmail, strComment);
            MyApplication.get(this).feedbackDatabaseReference()
                    .child(String.valueOf(System.currentTimeMillis()))
                    .setValue(feedback, (databaseError, databaseReference) -> {
                        showProgressDialog(false);
                        sendFeedbackSuccess();
                    });
        }
    }

    public void sendFeedbackSuccess() {
        GlobalFunction.hideSoftKeyboard(this);
        GlobalFunction.showToastMessage(this, getString(R.string.msg_send_feedback_success));
        mBinding.edtName.setText("");
        mBinding.edtPhone.setText("");
        mBinding.edtComment.setText("");
    }
}
