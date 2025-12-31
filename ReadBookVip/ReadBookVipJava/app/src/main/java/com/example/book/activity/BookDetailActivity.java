package com.example.book.activity;

import android.os.Bundle;

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityBookDetailBinding;
import com.example.book.model.Book;
import com.example.book.model.UserInfo;
import com.example.book.prefs.DataStoreManager;
import com.example.book.utils.AdvertisementHelper;
import com.example.book.utils.AsyncTaskExecutor;
import com.example.book.utils.StringUtil;
import com.github.pdfviewer.util.FitPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends BaseActivity {

    private ActivityBookDetailBinding mBinding;
    private Book mBook;
    private UserInfo mUserInfo;
    private int pagesReadSinceLastAd = 0;
    private int lastAdPage = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        loadDataIntent();
        initToolbar();
        if (!StringUtil.isEmpty(mBook.getUrl())) {
            new PdfDownloader().execute(mBook.getUrl());
        }
    }

    private void initToolbar() {
        mBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        if (mBook != null) {
            mBinding.layoutToolbar.tvToolbarTitle.setText(mBook.getTitle());
        }
    }

    private void loadDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBook = (Book) bundle.get(Constant.OBJECT_BOOK);
            if (bundle.containsKey(Constant.OBJECT_USER_INFO)) {
                mUserInfo = (UserInfo) bundle.get(Constant.OBJECT_USER_INFO);
            }
        }
    }

    private void handleReadBookEpub(InputStream inputStream) {
        int currentPage = (null == mUserInfo ? 0 : mUserInfo.getCurrentPage());
        mBinding.pdfView.fromStream(inputStream)
                .defaultPage(currentPage)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .enableAnnotationRendering(true)
                .pageFitPolicy(FitPolicy.BOTH)
                .spacing(0)
                .pageFling(true)
                .onLoad(nbPages -> {
                    showProgressDialog(false);
                    addHistory();
                })
                .onError(t -> {
                    showProgressDialog(false);
                    GlobalFunction.showToastMessage(BookDetailActivity.this,
                            getString(R.string.msg_file_error));
                })
                .onPageChange((page, pageCount) -> {
                    saveCurrentPageHistory(page);
                    checkAndShowAdvertisement(page);
                })
                .load();
    }

    private void saveCurrentPageHistory(int page) {
        if (mUserInfo == null) return;
        mUserInfo.setCurrentPage(page);
        MyApplication.get(this).bookDatabaseReference()
                .child(String.valueOf(mBook.getId()))
                .child("history")
                .child(String.valueOf(mUserInfo.getId()))
                .setValue(mUserInfo);
    }

    private void checkAndShowAdvertisement(int currentPage) {
        // Đếm số trang đã đọc (tránh đếm lại khi quay lại)
        if (currentPage > lastAdPage) {
            pagesReadSinceLastAd += (currentPage - lastAdPage);
            lastAdPage = currentPage;
        }

        // Hiển thị quảng cáo sau mỗi 10 trang
        if (pagesReadSinceLastAd >= Constant.PAGES_PER_AD) {
            showAdvertisement();
            pagesReadSinceLastAd = 0;
        }
    }

    private void showAdvertisement() {
        AdvertisementHelper.loadActiveAdvertisement(this, new AdvertisementHelper.OnAdvertisementLoadedListener() {
            @Override
            public void onAdvertisementLoaded(com.example.book.model.Advertisement advertisement) {
                AdvertisementHelper.showAdvertisement(BookDetailActivity.this, advertisement);
            }

            @Override
            public void onNoAdvertisement() {
                // Không có quảng cáo active, không làm gì
            }

            @Override
            public void onError(String error) {
                // Lỗi khi load quảng cáo, không làm gì để không ảnh hưởng đến trải nghiệm đọc sách
            }
        });
    }

    private void addHistory() {
        if (mBook == null || isHistory(mBook)) return;
        String userEmail = DataStoreManager.getUser().getEmail();
        mUserInfo = new UserInfo(System.currentTimeMillis(), userEmail, 0);
        MyApplication.get(this).bookDatabaseReference()
                .child(String.valueOf(mBook.getId()))
                .child("history")
                .child(String.valueOf(mUserInfo.getId()))
                .setValue(mUserInfo);
    }

    private boolean isHistory(Book book) {
        if (book.getHistory() == null || book.getHistory().isEmpty()) {
            return false;
        }
        List<UserInfo> listHistory = new ArrayList<>(book.getHistory().values());
        if (listHistory.isEmpty()) {
            return false;
        }
        for (UserInfo userInfo : listHistory) {
            if (DataStoreManager.getUser().getEmail().equals(userInfo.getEmailUser())) {
                return true;
            }
        }
        return false;
    }

    class PdfDownloader extends AsyncTaskExecutor<String, InputStream> {

        protected void onPreExecute() {
            showProgressDialog(true);
        }

        protected InputStream doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                int responseCode = httpConn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return httpConn.getInputStream();
                }
                httpConn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(InputStream inputStream) {
            if (inputStream != null) {
                handleReadBookEpub(inputStream);
            }
        }
    }
}
