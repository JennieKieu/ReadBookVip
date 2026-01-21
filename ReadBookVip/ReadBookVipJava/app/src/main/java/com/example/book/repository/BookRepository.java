package com.example.book.repository;

import androidx.annotation.NonNull;

import com.example.book.api.ApiCallback;
import com.example.book.api.ApiClient;
import com.example.book.model.Book;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository pattern for Book data
 * Handles API calls and data management
 */
public class BookRepository {
    
    private static BookRepository instance;
    
    private BookRepository() {}
    
    public static synchronized BookRepository getInstance() {
        if (instance == null) {
            instance = new BookRepository();
        }
        return instance;
    }
    
    /**
     * Get all books from API
     */
    public void getAllBooks(ApiCallback<List<Book>> callback) {
        ApiClient.getInstance().getApiService().getAllBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load books: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get book by ID
     */
    public void getBookById(long id, ApiCallback<Book> callback) {
        ApiClient.getInstance().getApiService().getBookById(id).enqueue(new Callback<Book>() {
            @Override
            public void onResponse(@NonNull Call<Book> call, @NonNull Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Book not found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Book> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get featured books
     */
    public void getFeaturedBooks(ApiCallback<List<Book>> callback) {
        ApiClient.getInstance().getApiService().getFeaturedBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load featured books");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get books by category
     */
    public void getBooksByCategory(long categoryId, ApiCallback<List<Book>> callback) {
        ApiClient.getInstance().getApiService().getBooksByCategory(categoryId).enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load books for this category");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}

