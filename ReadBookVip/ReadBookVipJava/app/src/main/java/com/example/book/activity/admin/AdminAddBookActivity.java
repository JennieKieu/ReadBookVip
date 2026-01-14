package com.example.book.activity.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.activity.BaseActivity;
import com.example.book.adapter.admin.AdminSelectAdapter;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityAdminAddBookBinding;
import com.example.book.model.Book;
import com.example.book.model.BookText;
import com.example.book.model.Category;
import com.example.book.model.SelectObject;
import com.example.book.utils.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddBookActivity extends BaseActivity {

    private ActivityAdminAddBookBinding binding;
    private boolean isUpdate;
    private Book mBook; // Keep for compatibility, but will convert to BookText for API
    private BookText mBookText;
    private SelectObject mCategorySelected;
    private BookApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();

        apiService = ApiClient.getApiService();
        binding.btnAddOrEdit.setOnClickListener(v -> addOrEditBook());
        binding.btnManageChapters.setOnClickListener(v -> goToManageChapters());
    }

    private void loadDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            isUpdate = true;
            mBook = (Book) bundleReceived.get(Constant.OBJECT_BOOK);
        }
    }

    private void initToolbar() {
        binding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
    }

    private void initView() {
        if (isUpdate) {
            binding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_update_book));
            binding.btnAddOrEdit.setText(getString(R.string.action_edit));

            binding.edtName.setText(mBook.getTitle());
            binding.edtImage.setText(mBook.getImage());
            binding.edtBanner.setText(mBook.getBanner());
            // PDF URL field is hidden - commented out
            // binding.edtLink.setText(mBook.getUrl());
            binding.chbFeatured.setChecked(mBook.isFeatured());
            
            // Convert Book to BookText for API
            mBookText = new BookText();
            mBookText.setId(mBook.getId());
            mBookText.setTitle(mBook.getTitle());
            mBookText.setImage(mBook.getImage());
            mBookText.setBanner(mBook.getBanner());
            mBookText.setCategoryId(mBook.getCategoryId());
            mBookText.setCategoryName(mBook.getCategoryName());
            mBookText.setFeatured(mBook.isFeatured());
        } else {
            binding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_add_book));
            binding.btnAddOrEdit.setText(getString(R.string.action_add));
        }
        loadListCategory();
    }

    private void loadListCategory() {
        MyApplication.get(this).categoryDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<SelectObject> list = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Category category = dataSnapshot.getValue(Category.class);
                            if (category == null) return;
                            list.add(0, new SelectObject(category.getId(), category.getName()));
                        }
                        AdminSelectAdapter adapter = new AdminSelectAdapter(AdminAddBookActivity.this,
                                R.layout.item_choose_option, list);
                        binding.spnCategory.setAdapter(adapter);
                        binding.spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                mCategorySelected = adapter.getItem(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                        if (mBook != null && mBook.getCategoryId() > 0) {
                            binding.spnCategory.setSelection(getPositionSelected(list, mBook.getCategoryId()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private int getPositionSelected(List<SelectObject> list, long id) {
        int position = 0;
        for (int i = 0; i < list.size(); i++) {
            if (id == list.get(i).getId()) {
                position = i;
                break;
            }
        }
        return position;
    }

    private void addOrEditBook() {
        String strName = binding.edtName.getText().toString().trim();
        String strImage = binding.edtImage.getText().toString().trim();
        String strBanner = binding.edtBanner.getText().toString().trim();
        // PDF URL validation removed - field is hidden
        // String strUrl = binding.edtLink.getText().toString().trim();

        if (StringUtil.isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_input_name_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_input_image_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strBanner)) {
            Toast.makeText(this, getString(R.string.msg_input_banner_require), Toast.LENGTH_SHORT).show();
            return;
        }

        // PDF URL validation removed - no longer required
        // if (StringUtil.isEmpty(strUrl)) {
        //     Toast.makeText(this, getString(R.string.msg_input_url_require), Toast.LENGTH_SHORT).show();
        //     return;
        // }

        if (mCategorySelected == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create BookText object
        BookText bookText = new BookText();
        bookText.setTitle(strName);
        bookText.setImage(strImage);
        bookText.setBanner(strBanner);
        bookText.setFeatured(binding.chbFeatured.isChecked());
        bookText.setCategoryId(mCategorySelected.getId());
        bookText.setCategoryName(mCategorySelected.getName());

        showProgressDialog(true);

        // Update book
        if (isUpdate && mBookText != null) {
            bookText.setId(mBookText.getId());
            apiService.updateBook(mBookText.getId(), bookText).enqueue(new Callback<BookText>() {
                @Override
                public void onResponse(@NonNull Call<BookText> call, @NonNull Response<BookText> response) {
                    showProgressDialog(false);
                    if (response.isSuccessful()) {
                        GlobalFunction.hideSoftKeyboard(AdminAddBookActivity.this);
                        clearFocus();
                        Toast.makeText(AdminAddBookActivity.this,
                                getString(R.string.msg_edit_book_success), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminAddBookActivity.this,
                                "Lỗi khi cập nhật sách", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookText> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        // Add book
        apiService.createBook(bookText).enqueue(new Callback<BookText>() {
            @Override
            public void onResponse(@NonNull Call<BookText> call, @NonNull Response<BookText> response) {
                showProgressDialog(false);
                if (response.isSuccessful()) {
                    binding.edtName.setText("");
                    binding.edtImage.setText("");
                    binding.edtBanner.setText("");
                    binding.chbFeatured.setChecked(false);
                    binding.spnCategory.setSelection(0);
                    GlobalFunction.hideSoftKeyboard(AdminAddBookActivity.this);
                    clearFocus();
                    Toast.makeText(AdminAddBookActivity.this,
                            getString(R.string.msg_add_book_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminAddBookActivity.this,
                            "Lỗi khi thêm sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookText> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(AdminAddBookActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToManageChapters() {
        if (!isUpdate || mBookText == null) {
            Toast.makeText(this, "Vui lòng lưu sách trước khi quản lý chương", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, mBookText);
        GlobalFunction.startActivity(this, AdminChapterListActivity.class, bundle);
    }

    private void clearFocus() {
        binding.edtName.clearFocus();
        binding.edtImage.clearFocus();
        binding.edtBanner.clearFocus();
        // binding.edtLink.clearFocus(); // PDF URL field is hidden
    }
}