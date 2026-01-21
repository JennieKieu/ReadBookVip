package com.example.book.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.book.R;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityBookDetailBinding;
import com.example.book.model.Book;
import com.example.book.model.BookText;
import com.example.book.model.Chapter;
import com.example.book.model.UserInfo;
import com.example.book.prefs.DataStoreManager;
import com.example.book.utils.AdvertisementHelper;
import com.example.book.utils.StringUtil;
// PDF code - commented for text/chapter migration
// import com.github.pdfviewer.util.FitPolicy;
// import com.example.book.utils.AsyncTaskExecutor;
// import java.io.IOException;
// import java.io.InputStream;
// import java.net.HttpURLConnection;
// import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailActivity extends BaseActivity {

    private ActivityBookDetailBinding mBinding;
    private Book mBook; // Keep for compatibility
    private BookText mBookText;
    private UserInfo mUserInfo;
    private List<Chapter> mListChapters;
    private int currentChapterIndex = 0;
    private BookApiService apiService;
    
    // PDF code - commented for text/chapter migration
    // private int pagesReadSinceLastAd = 0;
    // private int lastAdPage = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityBookDetailBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();
        
        // PDF code - commented for text/chapter migration
        // if (!StringUtil.isEmpty(mBook.getUrl())) {
        //     new PdfDownloader().execute(mBook.getUrl());
        // }
        
        // Load chapters from API
        if (mBook != null) {
            loadBookFromApi();
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

    private void initView() {
        apiService = ApiClient.getInstance().getBookApiService();
        mListChapters = new ArrayList<>();
        
        // Setup WebView
        WebView webView = mBinding.webViewChapter;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        
        // Chapter navigation buttons
        Button btnPrev = mBinding.btnPrevChapter;
        Button btnNext = mBinding.btnNextChapter;
        
        btnPrev.setOnClickListener(v -> loadPreviousChapter());
        btnNext.setOnClickListener(v -> loadNextChapter());
    }

    private void loadBookFromApi() {
        // Try to load BookText from API using Book ID
        showProgressDialog(true);
        apiService.getBookById(mBook.getId()).enqueue(new Callback<BookText>() {
            @Override
            public void onResponse(@NonNull Call<BookText> call, @NonNull Response<BookText> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mBookText = response.body();
                    loadChapters();
                } else {
                    showProgressDialog(false);
                    Toast.makeText(BookDetailActivity.this,
                            "Không tìm thấy sách", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookText> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(BookDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChapters() {
        if (mBookText == null) return;
        
        apiService.getChaptersByBook(mBookText.getId()).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(@NonNull Call<List<Chapter>> call, @NonNull Response<List<Chapter>> response) {
                showProgressDialog(false);
                if (response.isSuccessful() && response.body() != null) {
                    mListChapters = response.body();
                    if (mListChapters.isEmpty()) {
                        Toast.makeText(BookDetailActivity.this,
                                "Sách chưa có chương nào", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Load reading history to determine current chapter
                    loadReadingHistory();
                } else {
                    Toast.makeText(BookDetailActivity.this,
                            "Lỗi khi tải danh sách chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chapter>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(BookDetailActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadReadingHistory() {
        String userEmail = DataStoreManager.getUser().getEmail();
        apiService.getHistory(mBookText.getId(), userEmail).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> history = response.body();
                    Object chapterNumberObj = history.get("chapterNumber");
                    if (chapterNumberObj != null) {
                        int chapterNumber = ((Double) chapterNumberObj).intValue();
                        // Find chapter index
                        for (int i = 0; i < mListChapters.size(); i++) {
                            if (mListChapters.get(i).getChapterNumber() == chapterNumber) {
                                currentChapterIndex = i;
                                break;
                            }
                        }
                    }
                }
                displayCurrentChapter();
                addHistory();
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                displayCurrentChapter();
                addHistory();
            }
        });
    }

    private void displayCurrentChapter() {
        if (mListChapters == null || mListChapters.isEmpty()) return;
        if (currentChapterIndex < 0 || currentChapterIndex >= mListChapters.size()) {
            currentChapterIndex = 0;
        }
        
        Chapter chapter = mListChapters.get(currentChapterIndex);
        TextView tvChapterTitle = mBinding.tvChapterTitle;
        WebView webView = mBinding.webViewChapter;
        
        // Display chapter title
        String title = chapter.getTitle();
        if (StringUtil.isEmpty(title)) {
            title = "Chương " + chapter.getChapterNumber();
        }
        tvChapterTitle.setText(title);
        
        // Display HTML content
        String htmlContent = "<html><head><meta charset='UTF-8'>" +
                "<style>body { font-family: sans-serif; font-size: 18px; line-height: 1.6; " +
                "padding: 16px; color: #333; }</style></head><body>" +
                chapter.getContent() + "</body></html>";
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
        
        // Update navigation buttons
        updateNavigationButtons();
        
        // Save reading progress
        saveReadingProgress(chapter);
    }

    private void updateNavigationButtons() {
        Button btnPrev = mBinding.btnPrevChapter;
        Button btnNext = mBinding.btnNextChapter;
        
        btnPrev.setEnabled(currentChapterIndex > 0);
        btnNext.setEnabled(currentChapterIndex < mListChapters.size() - 1);
    }

    private void loadPreviousChapter() {
        if (currentChapterIndex > 0) {
            currentChapterIndex--;
            displayCurrentChapter();
        }
    }

    private void loadNextChapter() {
        if (currentChapterIndex < mListChapters.size() - 1) {
            currentChapterIndex++;
            displayCurrentChapter();
            // Check for advertisement after reading chapters
            checkAndShowAdvertisement();
        }
    }

    private void saveReadingProgress(Chapter chapter) {
        String userEmail = DataStoreManager.getUser().getEmail();
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("bookId", mBookText.getId());
        historyData.put("userEmail", userEmail);
        historyData.put("chapterId", chapter.getId());
        historyData.put("chapterNumber", chapter.getChapterNumber());
        
        apiService.saveHistory(mBookText.getId(), historyData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                // Progress saved
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                // Silent fail - don't interrupt reading
            }
        });
    }

    private void checkAndShowAdvertisement() {
        // Show advertisement after every 3 chapters (instead of 10 pages)
        if (currentChapterIndex > 0 && currentChapterIndex % 3 == 0) {
            showAdvertisement();
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
        if (mBookText == null) return;
        String userEmail = DataStoreManager.getUser().getEmail();
        
        // History is saved when displaying chapter, so just ensure it exists
        if (mListChapters != null && !mListChapters.isEmpty()) {
            Chapter firstChapter = mListChapters.get(0);
            saveReadingProgress(firstChapter);
        }
    }

    // PDF code - commented for text/chapter migration
    /*
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
