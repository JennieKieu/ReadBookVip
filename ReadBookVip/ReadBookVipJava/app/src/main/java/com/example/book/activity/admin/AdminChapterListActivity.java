package com.example.book.activity.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.R;
import com.example.book.activity.BaseActivity;
import com.example.book.adapter.admin.AdminChapterAdapter;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.databinding.ActivityAdminChapterListBinding;
import com.example.book.model.BookText;
import com.example.book.model.Chapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChapterListActivity extends BaseActivity {

    private ActivityAdminChapterListBinding binding;
    private BookText mBook;
    private List<Chapter> mListChapters;
    private AdminChapterAdapter mAdapter;
    private BookApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminChapterListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();
        loadListChapters();
    }

    private void loadDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBook = (BookText) bundle.getSerializable(Constant.OBJECT_BOOK);
        }
    }

    private void initToolbar() {
        binding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        if (mBook != null) {
            binding.layoutToolbar.tvToolbarTitle.setText("Chương: " + mBook.getTitle());
        }
    }

    private void initView() {
        apiService = ApiClient.getApiService();
        mListChapters = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rcvChapters.setLayoutManager(linearLayoutManager);

        mAdapter = new AdminChapterAdapter(mListChapters, new AdminChapterAdapter.IOnAdminChapterListener() {
            @Override
            public void onClickEditChapter(Chapter chapter) {
                goToEditChapter(chapter);
            }

            @Override
            public void onClickDeleteChapter(Chapter chapter) {
                deleteChapter(chapter);
            }
        });
        binding.rcvChapters.setAdapter(mAdapter);

        FloatingActionButton fabAddChapter = binding.fabAddChapter;
        fabAddChapter.setOnClickListener(v -> goToAddChapter());
    }

    private void loadListChapters() {
        if (mBook == null) return;

        showProgressDialog(true);
        apiService.getChaptersByBook(mBook.getId()).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(@NonNull Call<List<Chapter>> call, @NonNull Response<List<Chapter>> response) {
                showProgressDialog(false);
                if (response.isSuccessful() && response.body() != null) {
                    mListChapters.clear();
                    mListChapters.addAll(response.body());
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AdminChapterListActivity.this,
                            "Lỗi khi tải danh sách chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chapter>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(AdminChapterListActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToAddChapter() {
        if (mBook == null) return;
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, mBook);
        com.example.book.constant.GlobalFunction.startActivity(this,
                AdminAddChapterActivity.class, bundle);
    }

    private void goToEditChapter(Chapter chapter) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, mBook);
        bundle.putSerializable("chapter", chapter);
        com.example.book.constant.GlobalFunction.startActivity(this,
                AdminAddChapterActivity.class, bundle);
    }

    private void deleteChapter(Chapter chapter) {
        showProgressDialog(true);
        apiService.deleteChapter(chapter.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showProgressDialog(false);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminChapterListActivity.this,
                            "Xóa chương thành công", Toast.LENGTH_SHORT).show();
                    loadListChapters();
                } else {
                    Toast.makeText(AdminChapterListActivity.this,
                            "Lỗi khi xóa chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(AdminChapterListActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListChapters();
    }
}

