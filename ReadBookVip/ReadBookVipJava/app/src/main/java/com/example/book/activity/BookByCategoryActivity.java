package com.example.book.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;

import com.example.book.adapter.BookAdapter;
import com.example.book.api.ApiCallback;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityBookByCategoryBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.example.book.repository.BookRepository;
import com.example.book.R;

import java.util.ArrayList;
import java.util.List;

public class BookByCategoryActivity extends BaseActivity {

    private ActivityBookByCategoryBinding mBinding;
    private BookAdapter mBookAdapter;
    private List<Book> mListBook;
    private Category mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityBookByCategoryBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();
        initSwipeRefresh();
        loadListBookFromAPI();
    }

    private void loadDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mCategory = (Category) bundle.get(Constant.OBJECT_CATEGORY);
        }
    }

    private void initToolbar() {
        mBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        mBinding.layoutToolbar.tvToolbarTitle.setText(mCategory.getName());
    }

    private void initView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mBinding.rcvData.setLayoutManager(gridLayoutManager);
        mListBook = new ArrayList<>();
        mBookAdapter = new BookAdapter(mListBook, new IOnClickBookListener() {
            @Override
            public void onClickItemBook(Book book) {
                GlobalFunction.goToBookDetail(BookByCategoryActivity.this, book);
            }

            @Override
            public void onClickCategoryOfBook(Category category) {}

            @Override
            public void onClickFavoriteBook(Book book, boolean favorite) {
                GlobalFunction.onClickFavoriteBook(BookByCategoryActivity.this, book, favorite);
            }
        });
        mBinding.rcvData.setAdapter(mBookAdapter);
    }

    private void initSwipeRefresh() {
        mBinding.swipeRefresh.setOnRefreshListener(this::loadListBookFromAPI);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadListBookFromAPI() {
        showLoadingState();
        
        BookRepository.getInstance().getBooksByCategory(mCategory.getId(), new ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> books) {
                mBinding.swipeRefresh.setRefreshing(false);
                resetListBook();
                mListBook.addAll(books);
                
                if (mListBook.isEmpty()) {
                    showEmptyState();
                } else {
                    showContentState();
                    if (mBookAdapter != null) mBookAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String errorMessage) {
                mBinding.swipeRefresh.setRefreshing(false);
                showErrorState(errorMessage);
            }
        });
    }

    private void resetListBook() {
        if (mListBook == null) {
            mListBook = new ArrayList<>();
        } else {
            mListBook.clear();
        }
    }

    private void showLoadingState() {
        if (!mBinding.swipeRefresh.isRefreshing()) {
            showProgressDialog(true);
        }
        mBinding.layoutEmpty.getRoot().setVisibility(View.GONE);
        mBinding.layoutError.getRoot().setVisibility(View.GONE);
    }

    private void showContentState() {
        showProgressDialog(false);
        mBinding.rcvData.setVisibility(View.VISIBLE);
        mBinding.layoutEmpty.getRoot().setVisibility(View.GONE);
        mBinding.layoutError.getRoot().setVisibility(View.GONE);
    }

    private void showEmptyState() {
        showProgressDialog(false);
        mBinding.rcvData.setVisibility(View.GONE);
        mBinding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
        mBinding.layoutError.getRoot().setVisibility(View.GONE);
    }

    private void showErrorState(String errorMessage) {
        showProgressDialog(false);
        mBinding.rcvData.setVisibility(View.GONE);
        mBinding.layoutEmpty.getRoot().setVisibility(View.GONE);
        mBinding.layoutError.getRoot().setVisibility(View.VISIBLE);
        
        // Setup retry button
        mBinding.layoutError.btnRetry.setOnClickListener(v -> loadListBookFromAPI());
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
