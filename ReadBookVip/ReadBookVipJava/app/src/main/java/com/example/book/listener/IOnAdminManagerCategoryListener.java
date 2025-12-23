package com.example.book.listener;

import com.example.book.model.Category;

public interface IOnAdminManagerCategoryListener {
    void onClickUpdateCategory(Category category);
    void onClickDeleteCategory(Category category);
    void onClickDetailCategory(Category category);
}
