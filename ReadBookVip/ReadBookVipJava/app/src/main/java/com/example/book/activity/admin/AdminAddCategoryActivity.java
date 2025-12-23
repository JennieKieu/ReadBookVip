package com.example.book.activity.admin;

import android.os.Bundle;
import android.widget.Toast;

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.activity.BaseActivity;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityAdminAddCategoryBinding;
import com.example.book.model.Category;
import com.example.book.utils.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class AdminAddCategoryActivity extends BaseActivity {

    private ActivityAdminAddCategoryBinding binding;
    private boolean isUpdate;
    private Category mCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();

        binding.btnAddOrEdit.setOnClickListener(v -> addOrEditCategory());
    }

    private void loadDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            isUpdate = true;
            mCategory = (Category) bundleReceived.get(Constant.OBJECT_CATEGORY);
        }
    }

    private void initToolbar() {
        binding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
    }

    private void initView() {
        if (isUpdate) {
            binding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_update_category));
            binding.btnAddOrEdit.setText(getString(R.string.action_edit));

            binding.edtName.setText(mCategory.getName());
            binding.edtImage.setText(mCategory.getImage());
        } else {
            binding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_add_category));
            binding.btnAddOrEdit.setText(getString(R.string.action_add));
        }
    }

    private void addOrEditCategory() {
        String strName = binding.edtName.getText().toString().trim();
        String strImage = binding.edtImage.getText().toString().trim();

        if (StringUtil.isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_input_name_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_input_image_require), Toast.LENGTH_SHORT).show();
            return;
        }

        // Update category
        if (isUpdate) {
            showProgressDialog(true);
            Map<String, Object> map = new HashMap<>();
            map.put("name", strName);
            map.put("image", strImage);

            MyApplication.get(this).categoryDatabaseReference()
                    .child(String.valueOf(mCategory.getId())).updateChildren(map, (error, ref) -> {
                showProgressDialog(false);
                GlobalFunction.hideSoftKeyboard(this);
                clearFocus();
                Toast.makeText(AdminAddCategoryActivity.this,
                        getString(R.string.msg_edit_category_success), Toast.LENGTH_SHORT).show();
            });
            return;
        }

        // Add category
        showProgressDialog(true);
        long categoryId = System.currentTimeMillis();
        Category category = new Category(categoryId, strName, strImage);
        MyApplication.get(this).categoryDatabaseReference()
                .child(String.valueOf(categoryId)).setValue(category, (error, ref) -> {
            showProgressDialog(false);
            binding.edtName.setText("");
            binding.edtImage.setText("");
            GlobalFunction.hideSoftKeyboard(this);
            clearFocus();
            Toast.makeText(this, getString(R.string.msg_add_category_success),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void clearFocus() {
        binding.edtName.clearFocus();
        binding.edtImage.clearFocus();
    }
}