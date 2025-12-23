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
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityAdminAddBookBinding;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.example.book.model.SelectObject;
import com.example.book.utils.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddBookActivity extends BaseActivity {

    private ActivityAdminAddBookBinding binding;
    private boolean isUpdate;
    private Book mBook;
    private SelectObject mCategorySelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();

        binding.btnAddOrEdit.setOnClickListener(v -> addOrEditBook());
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
            binding.edtLink.setText(mBook.getUrl());
            binding.chbFeatured.setChecked(mBook.isFeatured());
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
        String strUrl = binding.edtLink.getText().toString().trim();

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


        if (StringUtil.isEmpty(strUrl)) {
            Toast.makeText(this, getString(R.string.msg_input_url_require), Toast.LENGTH_SHORT).show();
            return;
        }

        // Update book
        if (isUpdate) {
            showProgressDialog(true);
            Map<String, Object> map = new HashMap<>();
            map.put("title", strName);
            map.put("image", strImage);
            map.put("banner", strBanner);
            map.put("url", strUrl);
            map.put("featured", binding.chbFeatured.isChecked());
            map.put("categoryId", mCategorySelected.getId());
            map.put("categoryName", mCategorySelected.getName());

            MyApplication.get(this).bookDatabaseReference()
                    .child(String.valueOf(mBook.getId())).updateChildren(map, (error, ref) -> {
                showProgressDialog(false);
                GlobalFunction.hideSoftKeyboard(this);
                clearFocus();
                Toast.makeText(AdminAddBookActivity.this,
                        getString(R.string.msg_edit_book_success), Toast.LENGTH_SHORT).show();
            });
            return;
        }

        // Add book
        showProgressDialog(true);
        long bookId = System.currentTimeMillis();
        Book book = new Book(bookId, strName, strImage, binding.chbFeatured.isChecked(),
                mCategorySelected.getId(), mCategorySelected.getName(), strBanner, strUrl);
        MyApplication.get(this).bookDatabaseReference()
                .child(String.valueOf(bookId)).setValue(book, (error, ref) -> {
            showProgressDialog(false);
            binding.edtName.setText("");
            binding.edtImage.setText("");
            binding.edtBanner.setText("");
            binding.edtLink.setText("");
            binding.chbFeatured.setChecked(false);
            binding.spnCategory.setSelection(0);
            GlobalFunction.hideSoftKeyboard(this);
            clearFocus();
            Toast.makeText(this, getString(R.string.msg_add_book_success), Toast.LENGTH_SHORT).show();
        });
    }

    private void clearFocus() {
        binding.edtName.clearFocus();
        binding.edtImage.clearFocus();
        binding.edtBanner.clearFocus();
        binding.edtLink.clearFocus();
    }
}