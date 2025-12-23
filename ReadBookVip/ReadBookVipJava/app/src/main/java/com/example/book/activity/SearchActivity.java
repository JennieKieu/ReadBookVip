package com.example.book.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.adapter.BookAdapter;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivitySearchBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.example.book.utils.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private ActivitySearchBinding mBinding;
    private List<Book> mListBook;
    private BookAdapter mBookAdapter;
    private ValueEventListener mValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initToolbar();
        initUi();
        initListener();
        getListBookFromFirebase("");
    }

    private void initToolbar() {
        mBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        mBinding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_search));
    }

    private void initUi() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mBinding.rcvSearchResult.setLayoutManager(gridLayoutManager);
        mListBook = new ArrayList<>();
        mBookAdapter = new BookAdapter(mListBook, new IOnClickBookListener() {
            @Override
            public void onClickItemBook(Book book) {
                GlobalFunction.goToBookDetail(SearchActivity.this, book);
            }

            @Override
            public void onClickCategoryOfBook(Category category) {
                GlobalFunction.goToBookByCategory(SearchActivity.this, category);
            }

            @Override
            public void onClickFavoriteBook(Book book, boolean favorite) {
                GlobalFunction.onClickFavoriteBook(SearchActivity.this, book, favorite);
            }
        });
        mBinding.rcvSearchResult.setAdapter(mBookAdapter);
    }

    private void initListener() {
        mBinding.edtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strKey = s.toString().trim();
                if (strKey.isEmpty()) {
                    getListBookFromFirebase("");
                }
            }
        });

        mBinding.imgSearch.setOnClickListener(view -> searchBook());

        mBinding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchBook();
                return true;
            }
            return false;
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getListBookFromFirebase(String key) {
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resetListBookData();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Book book = dataSnapshot.getValue(Book.class);
                    if (book == null) return;
                    if (StringUtil.isEmpty(key)) {
                        mListBook.add(0, book);
                    } else {
                        if (GlobalFunction.getTextSearch(book.getTitle()).toLowerCase().trim()
                                .contains(GlobalFunction.getTextSearch(key).toLowerCase().trim())) {
                            mListBook.add(0, book);
                        }
                    }
                }
                if (mBookAdapter != null) mBookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        MyApplication.get(this).bookDatabaseReference().addValueEventListener(mValueEventListener);
    }

    private void resetListBookData() {
        if (mListBook == null) {
            mListBook = new ArrayList<>();
        } else {
            mListBook.clear();
        }
    }

    private void searchBook() {
        String strKey = mBinding.edtSearchName.getText().toString().trim();
        if (mValueEventListener != null) {
            MyApplication.get(this).bookDatabaseReference().removeEventListener(mValueEventListener);
        }
        getListBookFromFirebase(strKey);
        GlobalFunction.hideSoftKeyboard(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mValueEventListener != null) {
            MyApplication.get(this).bookDatabaseReference().removeEventListener(mValueEventListener);
        }
    }
}
