package com.example.book.listener;

import com.example.book.model.Book;
import com.example.book.model.Category;

public interface IOnClickBookListener {
    void onClickItemBook(Book book);
    void onClickCategoryOfBook(Category category);
    void onClickFavoriteBook(Book book, boolean favorite);
}
