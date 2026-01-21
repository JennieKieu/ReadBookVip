package com.example.book.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
import com.example.book.model.Chapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookInfoActivity extends BaseActivity {

    private ActivityBookInfoBinding binding;
    private Book mBook;
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
            getSupportActionBar().setTitle(mBook != null ? mBook.getTitle() : "");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
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
        binding.tvStatus.setText("Đang cập nhật");
        binding.tvStatus.setTextColor(getColor(R.color.blue));

        // Chapter count - will be updated after loading chapters
        binding.tvChapterCount.setText("Đang tải...");

        // Description - dummy for now
        binding.tvDescription.setText("Đang cập nhật mô tả...");

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
                    binding.tvChapterCount.setText(String.format("%d chương", mListChapters.size()));

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
                    Toast.makeText(BookInfoActivity.this, "Không thể tải danh sách chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chapter>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(BookInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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


