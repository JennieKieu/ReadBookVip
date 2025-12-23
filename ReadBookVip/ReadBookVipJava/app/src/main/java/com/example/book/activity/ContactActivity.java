package com.example.book.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.book.R;
import com.example.book.adapter.ContactAdapter;
import com.example.book.constant.AboutUsConfig;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityContactBinding;
import com.example.book.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends BaseActivity {

    private ActivityContactBinding mBinding;
    private ContactAdapter mContactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initToolbar();
        initData();
        initListener();
    }

    private void initToolbar() {
        mBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        mBinding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_contact));
    }

    private void initData() {
        mBinding.tvAboutUsTitle.setText(AboutUsConfig.ABOUT_US_TITLE);
        mBinding.tvAboutUsContent.setText(AboutUsConfig.ABOUT_US_CONTENT);
        mBinding.tvAboutUsWebsite.setText(AboutUsConfig.ABOUT_US_WEBSITE_TITLE);

        mContactAdapter = new ContactAdapter(this, loadListContact(),
                () -> GlobalFunction.callPhoneNumber(this));
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mBinding.rcvData.setNestedScrollingEnabled(false);
        mBinding.rcvData.setFocusable(false);
        mBinding.rcvData.setLayoutManager(layoutManager);
        mBinding.rcvData.setAdapter(mContactAdapter);
    }

    private void initListener() {
        mBinding.layoutWebsite.setOnClickListener(view
                -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.WEBSITE))));
    }

    public List<Contact> loadListContact() {
        List<Contact> contactArrayList = new ArrayList<>();
        contactArrayList.add(new Contact(Contact.FACEBOOK, R.drawable.ic_facebook));
        contactArrayList.add(new Contact(Contact.HOTLINE, R.drawable.ic_hotline));
        contactArrayList.add(new Contact(Contact.GMAIL, R.drawable.ic_gmail));
        contactArrayList.add(new Contact(Contact.SKYPE, R.drawable.ic_skype));
        contactArrayList.add(new Contact(Contact.YOUTUBE, R.drawable.ic_youtube));
        contactArrayList.add(new Contact(Contact.ZALO, R.drawable.ic_zalo));

        return contactArrayList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactAdapter.release();
    }
}
