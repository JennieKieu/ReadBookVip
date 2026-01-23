package com.example.book.activity.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.activity.BaseActivity;
import com.example.book.adapter.admin.AdminBookAdapter;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityAdminBookOfCategoryBinding;
import com.example.book.listener.IOnAdminManagerBookListener;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminBookOfCategoryActivity extends BaseActivity {

    private ActivityAdminBookOfCategoryBinding binding;
    private List<Book> mListBook;
    private AdminBookAdapter mAdminBookAdapter;
    private Category mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBookOfCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();
        loadListBook();
    }

    private void loadDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            mCategory = (Category) bundleReceived.get(Constant.OBJECT_CATEGORY);
        }
    }

    private void initToolbar() {
        binding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        binding.layoutToolbar.tvToolbarTitle.setText(mCategory.getName());
    }

    private void initView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rcvBook.setLayoutManager(linearLayoutManager);
        mListBook = new ArrayList<>();
        mAdminBookAdapter = new AdminBookAdapter(mListBook, new IOnAdminManagerBookListener() {
            @Override
            public void onClickUpdateBook(Book book) {
                onClickEditBook(book);
            }

            @Override
            public void onClickDeleteBook(Book book) {
                deleteBookItem(book);
            }

            @Override
            public void onClickDetailBook(Book book) {
                // Click vào sách sẽ mở màn hình chỉnh sửa
                onClickEditBook(book);
            }
        });
        binding.rcvBook.setAdapter(mAdminBookAdapter);
    }

    private void goToBookDetail(@NonNull Book book) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, book);
        GlobalFunction.startActivity(this, AdminBookDetailActivity.class, bundle);
    }

    private void onClickEditBook(Book book) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, book);
        GlobalFunction.startActivity(this, AdminAddBookActivity.class, bundle);
    }

    private void deleteBookItem(Book book) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) ->
                        MyApplication.get(this).bookDatabaseReference()
                        .child(String.valueOf(book.getId())).removeValue((error, ref) ->
                                Toast.makeText(this,
                                        getString(R.string.msg_delete_book_successfully),
                                        Toast.LENGTH_SHORT).show()))
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void resetListBook() {
        if (mListBook != null) {
            mListBook.clear();
        } else {
            mListBook = new ArrayList<>();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadListBook() {
        MyApplication.get(this).bookDatabaseReference()
                .orderByChild("categoryId").equalTo(mCategory.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        resetListBook();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Book book = dataSnapshot.getValue(Book.class);
                            if (book == null) return;
                            mListBook.add(0, book);
                        }
                        if (mAdminBookAdapter != null) mAdminBookAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}