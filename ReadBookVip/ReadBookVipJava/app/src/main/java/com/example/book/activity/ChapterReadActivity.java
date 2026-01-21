package com.example.book.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.book.R;
import com.example.book.adapter.ChapterAdapter;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.databinding.ActivityChapterReadBinding;
import com.example.book.listener.IOnChapterClickListener;
import com.example.book.model.Chapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChapterReadActivity extends BaseActivity {

    private ActivityChapterReadBinding binding;
    private List<Chapter> mListChapters;
    private ChapterAdapter chapterAdapter;
    private BookApiService apiService;
    private long bookId;
    private String bookTitle;
    private int currentChapterIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChapterReadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance().getBookApiService();
        mListChapters = new ArrayList<>();

        loadDataIntent();
        initToolbar();
        initWebView();
        initDrawer();
        loadChapters();
    }

    private void loadDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            bookId = bundle.getLong(Constant.BOOK_ID, 0);
            bookTitle = bundle.getString(Constant.BOOK_TITLE, "");
            currentChapterIndex = bundle.getInt(Constant.CHAPTER_INDEX, 0);
        }
    }

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(bookTitle);
        }

        // Add hamburger menu icon on the right
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_chapter_read);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_chapter_list) {
                binding.drawerLayout.openDrawer(GravityCompat.END);
                return true;
            }
            return false;
        });
    }

    private void initWebView() {
        WebView webView = binding.webViewChapter;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
    }

    private void initDrawer() {
        binding.tvBookTitleDrawer.setText(bookTitle);

        // Setup RecyclerView in drawer
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rcvChaptersDrawer.setLayoutManager(layoutManager);

        chapterAdapter = new ChapterAdapter(mListChapters, (chapter, position) -> {
            currentChapterIndex = position;
            displayChapter();
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        });
        binding.rcvChaptersDrawer.setAdapter(chapterAdapter);

        // Navigation buttons
        binding.btnPrevChapter.setOnClickListener(v -> goToPrevChapter());
        binding.btnNextChapter.setOnClickListener(v -> goToNextChapter());
    }

    private void loadChapters() {
        showProgressDialog(true);

        apiService.getChaptersByBook(bookId).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(@NonNull Call<List<Chapter>> call, @NonNull Response<List<Chapter>> response) {
                showProgressDialog(false);
                if (response.isSuccessful() && response.body() != null) {
                    mListChapters.clear();
                    mListChapters.addAll(response.body());
                    chapterAdapter.notifyDataSetChanged();
                    
                    if (!mListChapters.isEmpty()) {
                        displayChapter();
                    } else {
                        Toast.makeText(ChapterReadActivity.this, "Chưa có chương nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChapterReadActivity.this, "Không thể tải danh sách chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chapter>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(ChapterReadActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayChapter() {
        if (mListChapters == null || mListChapters.isEmpty()) return;
        if (currentChapterIndex < 0 || currentChapterIndex >= mListChapters.size()) return;

        Chapter currentChapter = mListChapters.get(currentChapterIndex);
        
        // Update title
        binding.tvChapterTitle.setText(String.format("Chương %d: %s", 
            currentChapter.getChapterNumber(), 
            currentChapter.getTitle()));

        // Load content into WebView
        String htmlContent = buildHtmlContent(currentChapter.getContent());
        binding.webViewChapter.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

        // Update navigation buttons
        binding.btnPrevChapter.setEnabled(currentChapterIndex > 0);
        binding.btnNextChapter.setEnabled(currentChapterIndex < mListChapters.size() - 1);

        // Scroll to top
        binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_UP));
    }

    private String buildHtmlContent(String content) {
        return "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-size: 16px; line-height: 1.6; padding: 0; margin: 0; color: #333; }" +
                "p { margin-bottom: 1em; }" +
                "img { max-width: 100%; height: auto; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                content +
                "</body>" +
                "</html>";
    }

    private void goToPrevChapter() {
        if (currentChapterIndex > 0) {
            currentChapterIndex--;
            displayChapter();
        }
    }

    private void goToNextChapter() {
        if (currentChapterIndex < mListChapters.size() - 1) {
            currentChapterIndex++;
            displayChapter();
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}


