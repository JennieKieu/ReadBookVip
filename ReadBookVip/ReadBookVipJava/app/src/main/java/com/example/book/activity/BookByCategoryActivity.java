package com.example.book.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.book.MyApplication;
import com.example.book.adapter.BookAdapter;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityBookByCategoryBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BookByCategoryActivity extends BaseActivity {

    private ActivityBookByCategoryBinding mBinding;
    private BookAdapter mBookAdapter;
    private List<Book> mListBook;
    private Category mCategory;
    private ValueEventListener mBookValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityBookByCategoryBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();
        loadListBookFromFirebase();
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

    @SuppressLint("NotifyDataSetChanged")
    private void loadListBookFromFirebase() {
        mBookValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resetListBook();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Book book = dataSnapshot.getValue(Book.class);
                    if (book == null) return;
                    mListBook.add(0, book);
                }
                if (mBookAdapter != null) mBookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        MyApplication.get(this).bookDatabaseReference()
                .orderByChild("categoryId").equalTo(mCategory.getId())
                .addValueEventListener(mBookValueEventListener);
    }

    private void resetListBook() {
        if (mListBook == null) {
            mListBook = new ArrayList<>();
        } else {
            mListBook.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBookValueEventListener != null) {
            MyApplication.get(this).bookDatabaseReference().removeEventListener(mBookValueEventListener);
        }
    }
}
