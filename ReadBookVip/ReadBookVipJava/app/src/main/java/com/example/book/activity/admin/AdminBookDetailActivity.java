package com.example.book.activity.admin;

import android.os.Bundle;

import com.example.book.R;
import com.example.book.activity.BaseActivity;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityAdminBookDetailBinding;
import com.example.book.model.Book;
// PDF code - commented for text/chapter migration
// import com.example.book.utils.AsyncTaskExecutor;
import com.example.book.utils.StringUtil;
// import com.github.pdfviewer.util.FitPolicy;
// import java.io.IOException;
// import java.io.InputStream;
// import java.net.HttpURLConnection;
// import java.net.URL;

public class AdminBookDetailActivity extends BaseActivity {

    private ActivityAdminBookDetailBinding mBinding;
    private Book mBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAdminBookDetailBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        loadDataIntent();
        initToolbar();
        // PDF code - commented for text/chapter migration
        // if (!StringUtil.isEmpty(mBook.getUrl())) {
        //     new AdminBookDetailActivity.PdfDownloader().execute(mBook.getUrl());
        // }
        
        // TODO: Implement chapter viewing for admin (similar to BookDetailActivity)
        GlobalFunction.showToastMessage(this, "Xem chương sách - Tính năng đang phát triển");
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
        }
    }

    // PDF code - commented for text/chapter migration
    /*
    private void handleReadBookEpub(InputStream inputStream) {
        mBinding.pdfView.fromStream(inputStream)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .enableAnnotationRendering(true)
                .pageFitPolicy(FitPolicy.BOTH)
                .spacing(0)
                .pageFling(true)
                .onLoad(nbPages -> showProgressDialog(false))
                .onError(t -> {
                    showProgressDialog(false);
                    GlobalFunction.showToastMessage(AdminBookDetailActivity.this,
                            getString(R.string.msg_file_error));
                })
                .load();
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
    */
}
