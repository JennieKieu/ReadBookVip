package com.example.book.activity;

import android.os.Bundle;
import android.content.Intent;

import androidx.recyclerview.widget.GridLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.book.R;
import com.example.book.adapter.BookAdapter;
import com.example.book.api.ApiCallback;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityHistoryBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.example.book.prefs.DataStoreManager;
import com.example.book.repository.BookRepository;
import com.example.book.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends BaseActivity {

    private ActivityHistoryBinding mBinding;
    private List<Book> mListBook;
    private BookAdapter mBookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initToolbar();
        initUi();
        loadDataHistory();
    }

    private void initToolbar() {
        mBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        mBinding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_history));
        // Không hiển thị nút xóa tất cả, chỉ xóa từng cuốn
        mBinding.layoutToolbar.imgToolbarAction.setVisibility(android.view.View.GONE);
    }

    private void initUi() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mBinding.rcvData.setLayoutManager(gridLayoutManager);

        mListBook = new ArrayList<>();
        mBookAdapter = new BookAdapter(mListBook, new IOnClickBookListener() {
            @Override
            public void onClickItemBook(Book book) {
                if (book == null) return;
                showBookHistoryOptions(book);
            }

            @Override
            public void onClickCategoryOfBook(Category category) {
                GlobalFunction.goToBookByCategory(HistoryActivity.this, category);
            }

            @Override
            public void onClickFavoriteBook(Book book, boolean favorite) {
                GlobalFunction.onClickFavoriteBook(HistoryActivity.this, book, favorite);
            }
        });
        mBinding.rcvData.setAdapter(mBookAdapter);
    }

    private void loadDataHistory() {
        showProgressDialog(true);
        String userEmail = DataStoreManager.getUser() != null ? DataStoreManager.getUser().getEmail() : null;
        if (StringUtil.isEmpty(userEmail)) {
            showProgressDialog(false);
            resetListData();
            if (mBookAdapter != null) mBookAdapter.notifyDataSetChanged();
            return;
        }

        BookRepository.getInstance().getHistoryBooks(userEmail, new ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> books) {
                showProgressDialog(false);
                resetListData();
                if (books != null) {
                    mListBook.addAll(books);
                }
                if (mBookAdapter != null) mBookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                showProgressDialog(false);
                GlobalFunction.showToastMessage(HistoryActivity.this,
                        getString(R.string.msg_get_date_error));
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

    private void showBookHistoryOptions(Book book) {
        new MaterialDialog.Builder(this)
                .title(book.getTitle())
                .content(getString(R.string.msg_history_item_options))
                .positiveText(getString(R.string.action_read))
                .onPositive((dialog, which) -> openBookFromHistory(book))
                .negativeText(getString(R.string.action_delete))
                .onNegative((dialog, which) -> deleteHistoryItem(book))
                .cancelable(true)
                .show();
    }

    private void openBookFromHistory(Book book) {
        Intent intent = new Intent(HistoryActivity.this, ChapterReadActivity.class);
        intent.putExtra(Constant.BOOK_ID, book.getId());
        intent.putExtra(Constant.BOOK_TITLE, book.getTitle());
        intent.putExtra(Constant.CHAPTER_INDEX, 0);
        intent.putExtra(Constant.USE_HISTORY, true);
        startActivity(intent);
    }

    private void deleteHistoryItem(Book book) {
        String userEmail = DataStoreManager.getUser() != null ? DataStoreManager.getUser().getEmail() : null;
        if (StringUtil.isEmpty(userEmail) || book == null) {
            return;
        }

        BookApiService apiService = ApiClient.getInstance().getBookApiService();
        apiService.deleteHistory(book.getId(), userEmail).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                // Xóa khỏi list hiện tại
                mListBook.remove(book);
                if (mBookAdapter != null) mBookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                // Silent fail
            }
        });
    }

}
