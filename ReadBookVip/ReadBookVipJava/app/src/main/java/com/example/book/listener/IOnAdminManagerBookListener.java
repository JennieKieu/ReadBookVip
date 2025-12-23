package com.example.book.listener;

import com.example.book.model.Book;

public interface IOnAdminManagerBookListener {
    void onClickUpdateBook(Book book);
    void onClickDeleteBook(Book book);
    void onClickDetailBook(Book book);
}
