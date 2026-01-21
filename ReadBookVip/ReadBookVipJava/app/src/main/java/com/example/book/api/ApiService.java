package com.example.book.api;

import com.example.book.model.Book;
import com.example.book.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    
    // ============ BOOKS ============
    
    @GET("api/books")
    Call<List<Book>> getAllBooks();
    
    @GET("api/books/{id}")
    Call<Book> getBookById(@Path("id") long id);
    
    @GET("api/books/featured")
    Call<List<Book>> getFeaturedBooks();
    
    @GET("api/books/category/{categoryId}")
    Call<List<Book>> getBooksByCategory(@Path("categoryId") long categoryId);
    
    // ============ CATEGORIES ============
    
    @GET("api/categories")
    Call<List<Category>> getAllCategories();
    
    @GET("api/categories/{id}")
    Call<Category> getCategoryById(@Path("id") long id);
}

