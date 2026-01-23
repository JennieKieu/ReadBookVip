package com.example.book.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.book.R;
import com.example.book.adapter.BookAdapter;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityFavoriteBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.example.book.prefs.DataStoreManager;
import com.example.book.repository.BookRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends BaseActivity {

    private ActivityFavoriteBinding mBinding;
    private List<Book> mListBook;
    private BookAdapter mBookAdapter;
    private BookApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initToolbar();
        initUi();
        loadDataFavorite();
    }

    private void initToolbar() {
        mBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        mBinding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_favorite));
    }

    private void initUi() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mBinding.rcvData.setLayoutManager(gridLayoutManager);

        mListBook = new ArrayList<>();
        apiService = ApiClient.getInstance().getBookApiService();
        mBookAdapter = new BookAdapter(mListBook, new IOnClickBookListener() {
            @Override
            public void onClickItemBook(Book book) {
                GlobalFunction.goToBookDetail(FavoriteActivity.this, book);
            }

            @Override
            public void onClickCategoryOfBook(Category category) {
                GlobalFunction.goToBookByCategory(FavoriteActivity.this, category);
            }

            @Override
            public void onClickFavoriteBook(Book book, boolean favorite) {
                GlobalFunction.onClickFavoriteBook(FavoriteActivity.this, book, favorite);
                // Reload favorite list after change
                loadDataFavorite();
            }
        });
        mBinding.rcvData.setAdapter(mBookAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadDataFavorite() {
        if (DataStoreManager.getUser() == null) return;
        String userEmail = DataStoreManager.getUser().getEmail();
        if (userEmail == null || userEmail.isEmpty()) return;
        
        // First, get favorite book IDs from API
        apiService.getFavorites(userEmail).enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(@NonNull Call<List<Long>> call, @NonNull Response<List<Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<Long> favoriteBookIds = new HashSet<>(response.body());
                    // Then load all books and filter by favorite IDs
                    loadAllBooksAndFilter(favoriteBookIds);
                } else {
                    resetListData();
                    if (mBookAdapter != null) mBookAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Long>> call, @NonNull Throwable t) {
                GlobalFunction.showToastMessage(FavoriteActivity.this,
                        getString(R.string.msg_get_date_error));
                resetListData();
                if (mBookAdapter != null) mBookAdapter.notifyDataSetChanged();
            }
        });
    }
    
    private void loadAllBooksAndFilter(Set<Long> favoriteBookIds) {
        BookRepository.getInstance().getAllBooks(new com.example.book.api.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> books) {
                resetListData();
                // Filter books that are in favorite list
                for (Book book : books) {
                    if (favoriteBookIds.contains(book.getId())) {
                        mListBook.add(book);
                    }
                }
                if (mBookAdapter != null) {
                    mBookAdapter.notifyDataSetChanged();
                    // Refresh favorite list in adapter
                    mBookAdapter.refreshFavoriteList();
                }
            }

            @Override
            public void onError(String errorMessage) {
                GlobalFunction.showToastMessage(FavoriteActivity.this, errorMessage);
                resetListData();
                if (mBookAdapter != null) mBookAdapter.notifyDataSetChanged();
            }
        });
    }

    private void resetListData() {
        if (mListBook == null) {
            mListBook = new ArrayList<>();
        } else {
            mListBook.clear();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorite list when returning to this activity
        loadDataFavorite();
    }
}
