package com.example.book.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.book.R;
import com.example.book.adapter.ChapterAdapter;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.databinding.ActivityBookInfoBinding;
import com.example.book.listener.IOnChapterClickListener;
import com.example.book.model.Book;
import com.example.book.model.BookText;
import com.example.book.model.Chapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookInfoActivity extends BaseActivity {

    private ActivityBookInfoBinding binding;
    private Book mBook;
    private BookText mBookDetails;
    private List<Chapter> mListChapters;
    private ChapterAdapter chapterAdapter;
    private BookApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance().getBookApiService();
        mListChapters = new ArrayList<>();

        loadDataIntent();
        initToolbar();
        initView();
        initAppBarListener();
        loadBookDetails();
        loadChapters();
    }

    private void loadDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBook = (Book) bundle.get(Constant.OBJECT_BOOK);
        }
    }

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Set title in toolbar header
            if (mBook != null && mBook.getTitle() != null) {
                getSupportActionBar().setTitle(mBook.getTitle());
            } else {
                getSupportActionBar().setTitle("");
            }
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        // Set navigation icon color to black
        Drawable navigationIcon = binding.toolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                PorterDuff.Mode.SRC_ATOP
            );
        }
        
        // Set toolbar title text color - will change based on scroll
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
    }
    
    private void initAppBarListener() {
        binding.appbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            // Calculate scroll percentage
            int totalScrollRange = appBarLayout.getTotalScrollRange();
            float scrollPercentage = Math.abs(verticalOffset) / (float) totalScrollRange;
            
            // Change title color based on scroll position
            if (scrollPercentage > 0.5f) {
                // More than 50% scrolled - show black text
                binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.black));
            } else {
                // Less than 50% scrolled - show white text
                binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
            }
        });
    }

    private void initView() {
        if (mBook == null) return;

        // Load banner image
        if (mBook.getBanner() != null && !mBook.getBanner().isEmpty()) {
            Glide.with(this)
                    .load(mBook.getBanner())
                    .placeholder(R.drawable.img_no_image)
                    .error(R.drawable.img_no_image)
                    .into(binding.imgBanner);
        }

        // Load cover image
        if (mBook.getImage() != null && !mBook.getImage().isEmpty()) {
            Glide.with(this)
                    .load(mBook.getImage())
                    .placeholder(R.drawable.img_no_image)
                    .error(R.drawable.img_no_image)
                    .into(binding.imgBookCover);
        }

        // Set book info
        binding.tvBookTitle.setText(mBook.getTitle());
        binding.tvCategory.setText(mBook.getCategoryName());
        
        // Status - dummy for now since Book doesn't have status field
        binding.tvStatus.setText("Updating");
        binding.tvStatus.setTextColor(getColor(R.color.blue));

        // Chapter count - will be updated after loading chapters
        binding.tvChapterCount.setText("Loading...");

        // Description - will be loaded from API
        binding.tvDescription.setText("Loading description...");

        // Setup RecyclerView for chapters
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rcvChapters.setLayoutManager(layoutManager);

        chapterAdapter = new ChapterAdapter(mListChapters, (chapter, position) -> {
            // Go to ChapterReadActivity
            goToChapterRead(position);
        });
        binding.rcvChapters.setAdapter(chapterAdapter);

        // Start Reading button
        binding.btnStartReading.setOnClickListener(v -> goToChapterRead(0));
    }

    private void loadBookDetails() {
        if (mBook == null) return;

        apiService.getBookById(mBook.getId()).enqueue(new Callback<BookText>() {
            @Override
            public void onResponse(@NonNull Call<BookText> call, @NonNull Response<BookText> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mBookDetails = response.body();
                    updateBookDetails();
                } else {
                    binding.tvDescription.setText("No description available");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookText> call, @NonNull Throwable t) {
                binding.tvDescription.setText("Failed to load description");
            }
        });
    }

    private void updateBookDetails() {
        if (mBookDetails == null) return;

        // Update description - check for null, empty, or whitespace only
        String description = mBookDetails.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            // Remove surrounding quotes if present (e.g., "description" -> description)
            String cleanDescription = description.trim();
            if (cleanDescription.startsWith("\"") && cleanDescription.endsWith("\"")) {
                cleanDescription = cleanDescription.substring(1, cleanDescription.length() - 1);
            }
            binding.tvDescription.setText(cleanDescription);
        } else {
            binding.tvDescription.setText("No description available");
        }

        // Update status
        if (mBookDetails.getStatus() != null) {
            String status = mBookDetails.getStatus();
            if ("completed".equalsIgnoreCase(status)) {
                binding.tvStatus.setText("Completed");
                binding.tvStatus.setTextColor(getColor(R.color.green));
            } else {
                binding.tvStatus.setText("Ongoing");
                binding.tvStatus.setTextColor(getColor(R.color.blue));
            }
        }

        // Update tags if available
        if (mBookDetails.getTags() != null && !mBookDetails.getTags().trim().isEmpty()) {
            binding.tvTags.setText(mBookDetails.getTags().trim());
            binding.layoutTags.setVisibility(View.VISIBLE);
        } else {
            binding.layoutTags.setVisibility(View.GONE);
        }
    }

    private void loadChapters() {
        if (mBook == null) return;

        showProgressDialog(true);

        apiService.getChaptersByBook(mBook.getId()).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(@NonNull Call<List<Chapter>> call, @NonNull Response<List<Chapter>> response) {
                showProgressDialog(false);
                if (response.isSuccessful() && response.body() != null) {
                    mListChapters.clear();
                    mListChapters.addAll(response.body());
                    chapterAdapter.notifyDataSetChanged();

                    // Update chapter count
                    binding.tvChapterCount.setText(String.format("%d chapters", mListChapters.size()));

                    // Show/hide empty state
                    if (mListChapters.isEmpty()) {
                        binding.rcvChapters.setVisibility(View.GONE);
                        binding.tvNoChapters.setVisibility(View.VISIBLE);
                        binding.btnStartReading.setEnabled(false);
                    } else {
                        binding.rcvChapters.setVisibility(View.VISIBLE);
                        binding.tvNoChapters.setVisibility(View.GONE);
                        binding.btnStartReading.setEnabled(true);
                    }
                } else {
                    Toast.makeText(BookInfoActivity.this, "Failed to load chapters", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chapter>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(BookInfoActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToChapterRead(int chapterIndex) {
        if (mBook == null || mListChapters.isEmpty()) return;

        Intent intent = new Intent(this, ChapterReadActivity.class);
        intent.putExtra(Constant.BOOK_ID, mBook.getId());
        intent.putExtra(Constant.BOOK_TITLE, mBook.getTitle());
        intent.putExtra(Constant.CHAPTER_INDEX, chapterIndex);
        startActivity(intent);
    }
}


