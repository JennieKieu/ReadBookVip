package com.example.book.activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.book.R;
import com.example.book.adapter.ChapterAdapter;
import com.example.book.api.AdvertisementApiService;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityChapterReadBinding;
import com.example.book.listener.IOnChapterClickListener;
import com.example.book.model.Advertisement;
import com.example.book.model.Chapter;
import com.example.book.widget.OverscrollScrollView;

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
    private AdvertisementApiService advertisementApiService;
    private long bookId;
    private String bookTitle;
    private int currentChapterIndex = 0;
    private int chapterChangeCount = 0; // Count chapter changes for ad display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChapterReadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance().getBookApiService();
        advertisementApiService = ApiClient.getInstance().getAdvertisementApiService();
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

        // Back button
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chapter_read, menu);
        
        // Tint menu icon to white
        MenuItem menuItem = menu.findItem(R.id.action_chapter_list);
        if (menuItem != null && menuItem.getIcon() != null) {
            menuItem.getIcon().setTint(getColor(R.color.white));
        }
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_chapter_list) {
            binding.drawerLayout.openDrawer(GravityCompat.END);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isChangingChapter = false;
    
    private void initWebView() {
        WebView webView = binding.webViewChapter;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        
        // Setup overscroll listener for Wattpad-like chapter navigation
        setupOverscrollListener();
    }
    
    private void setupOverscrollListener() {
        OverscrollScrollView overscrollScrollView = (OverscrollScrollView) binding.scrollView;
        
        // Setup progress listener for visual feedback
        overscrollScrollView.setOnOverscrollProgressListener(new OverscrollScrollView.OnOverscrollProgressListener() {
            @Override
            public void onOverscrollProgress(float progress, boolean isTop) {
                View indicator = isTop ? binding.overscrollIndicatorTop : binding.overscrollIndicatorBottom;
                if (indicator != null) {
                    if (indicator.getVisibility() == View.GONE) {
                        indicator.setVisibility(View.VISIBLE);
                    }
                    indicator.setAlpha(progress);
                }
            }
            
            @Override
            public void onOverscrollReset() {
                if (binding.overscrollIndicatorTop != null) {
                    animateIndicatorOut(binding.overscrollIndicatorTop);
                }
                if (binding.overscrollIndicatorBottom != null) {
                    animateIndicatorOut(binding.overscrollIndicatorBottom);
                }
            }
        });
        
        // Setup overscroll listener for chapter navigation
        overscrollScrollView.setOnOverscrollListener(new OverscrollScrollView.OnOverscrollListener() {
            @Override
            public void onOverscrollTop() {
                // User overscrolled at top - go to previous chapter (end of previous chapter)
                if (!isChangingChapter && currentChapterIndex > 0) {
                    isChangingChapter = true;
                    // Hide indicator
                    if (binding.overscrollIndicatorTop != null) {
                        animateIndicatorOut(binding.overscrollIndicatorTop);
                    }
                    // Smooth transition to previous chapter
                    if (currentChapterIndex > 0) {
                        currentChapterIndex--;
                        chapterAdapter.setCurrentChapterIndex(currentChapterIndex);
                        displayChapter();
                        chapterChangeCount++;
                        checkAndShowAdvertisement();
                        // Smooth scroll to bottom of previous chapter after it loads
                        binding.webViewChapter.postDelayed(() -> {
                            binding.scrollView.post(() -> {
                                binding.scrollView.smoothScrollTo(0, binding.scrollView.getChildAt(0).getHeight());
                                isChangingChapter = false;
                            });
                        }, 300);
                    } else {
                        isChangingChapter = false;
                    }
                }
            }
            
            @Override
            public void onOverscrollBottom() {
                // User overscrolled at bottom - go to next chapter (start of next chapter)
                if (!isChangingChapter && currentChapterIndex < mListChapters.size() - 1) {
                    isChangingChapter = true;
                    // Hide indicator
                    if (binding.overscrollIndicatorBottom != null) {
                        animateIndicatorOut(binding.overscrollIndicatorBottom);
                    }
                    // Smooth transition to next chapter
                    if (currentChapterIndex < mListChapters.size() - 1) {
                        currentChapterIndex++;
                        chapterAdapter.setCurrentChapterIndex(currentChapterIndex);
                        displayChapter();
                        chapterChangeCount++;
                        checkAndShowAdvertisement();
                        // Smooth scroll to top is already handled in displayChapter()
                        binding.scrollView.postDelayed(() -> {
                            isChangingChapter = false;
                        }, 300);
                    } else {
                        isChangingChapter = false;
                    }
                }
            }
        });
    }
    
    private void animateIndicatorOut(View indicator) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(indicator, "alpha", indicator.getAlpha(), 0f);
        animator.setDuration(200);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
        animator.addUpdateListener(animation -> {
            if (indicator.getAlpha() == 0) {
                indicator.setVisibility(View.GONE);
            }
        });
    }

    private void initDrawer() {
        binding.tvBookTitleDrawer.setText(bookTitle);

        // Setup RecyclerView in drawer
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rcvChaptersDrawer.setLayoutManager(layoutManager);

        chapterAdapter = new ChapterAdapter(mListChapters, (chapter, position) -> {
            currentChapterIndex = position;
            chapterAdapter.setCurrentChapterIndex(currentChapterIndex);
            displayChapter();
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        });
        binding.rcvChaptersDrawer.setAdapter(chapterAdapter);

        // Navigation buttons
        binding.btnPrevChapter.setOnClickListener(v -> {
            goToPrevChapter();
            chapterChangeCount++;
            checkAndShowAdvertisement();
        });
        binding.btnNextChapter.setOnClickListener(v -> {
            goToNextChapter();
            chapterChangeCount++;
            checkAndShowAdvertisement();
        });
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
                    
                    // Set current chapter index in adapter
                    if (currentChapterIndex >= 0 && currentChapterIndex < mListChapters.size()) {
                        chapterAdapter.setCurrentChapterIndex(currentChapterIndex);
                    }
                    
                    if (!mListChapters.isEmpty()) {
                        displayChapter();
                    } else {
                        Toast.makeText(ChapterReadActivity.this, "No chapters available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChapterReadActivity.this, "Failed to load chapters", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chapter>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(ChapterReadActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayChapter() {
        if (mListChapters == null || mListChapters.isEmpty()) return;
        if (currentChapterIndex < 0 || currentChapterIndex >= mListChapters.size()) return;

        Chapter currentChapter = mListChapters.get(currentChapterIndex);
        
        // Update title
        binding.tvChapterTitle.setText(String.format("Chapter %d: %s", 
            currentChapter.getChapterNumber(), 
            currentChapter.getTitle()));

        // Load content into WebView
        String htmlContent = buildHtmlContent(currentChapter.getContent());
        binding.webViewChapter.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

        // Update navigation buttons
        binding.btnPrevChapter.setEnabled(currentChapterIndex > 0);
        binding.btnNextChapter.setEnabled(currentChapterIndex < mListChapters.size() - 1);
        
        // Update adapter to highlight current chapter
        chapterAdapter.setCurrentChapterIndex(currentChapterIndex);

        // Smooth scroll to top when new chapter is loaded
        binding.scrollView.post(() -> {
            binding.scrollView.smoothScrollTo(0, 0);
            // Wait for WebView to load content, then ensure scroll to top
            binding.webViewChapter.postDelayed(() -> {
                binding.scrollView.smoothScrollTo(0, 0);
            }, 150);
        });
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
            chapterAdapter.setCurrentChapterIndex(currentChapterIndex);
            displayChapter();
        }
    }

    private void goToNextChapter() {
        if (currentChapterIndex < mListChapters.size() - 1) {
            currentChapterIndex++;
            chapterAdapter.setCurrentChapterIndex(currentChapterIndex);
            displayChapter();
        }
    }
    
    private void checkAndShowAdvertisement() {
        // Show advertisement every 5 chapter changes
        if (chapterChangeCount > 0 && chapterChangeCount % 5 == 0) {
            loadAndShowAdvertisement();
        }
    }
    
    private void loadAndShowAdvertisement() {
        advertisementApiService.getActiveAdvertisements().enqueue(new Callback<List<Advertisement>>() {
            @Override
            public void onResponse(@NonNull Call<List<Advertisement>> call, @NonNull Response<List<Advertisement>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Select random advertisement
                    List<Advertisement> activeAds = response.body();
                    int randomIndex = (int) (Math.random() * activeAds.size());
                    Advertisement selectedAd = activeAds.get(randomIndex);
                    
                    // Show advertisement
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constant.OBJECT_ADVERTISEMENT, selectedAd);
                    GlobalFunction.startActivity(ChapterReadActivity.this, AdvertisementActivity.class, bundle);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Advertisement>> call, @NonNull Throwable t) {
                // Silent fail - don't interrupt reading
            }
        });
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


