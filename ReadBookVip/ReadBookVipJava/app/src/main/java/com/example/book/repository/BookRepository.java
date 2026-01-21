package com.example.book.repository;

import androidx.annotation.NonNull;

import com.example.book.api.ApiCallback;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.model.Book;
import com.example.book.model.BookText;

import java.util.ArrayList;
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
        BookApiService apiService = ApiClient.getInstance().getBookApiService();
        apiService.getAllBooks().enqueue(new Callback<List<BookText>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookText>> call, @NonNull Response<List<BookText>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Convert BookText to Book
                    List<Book> books = convertBookTextListToBookList(response.body());
                    callback.onSuccess(books);
                } else {
                    callback.onError("Failed to load books: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookText>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Convert BookText to Book
     */
    private Book convertBookTextToBook(BookText bookText) {
        Book book = new Book();
        book.setId(bookText.getId());
        book.setTitle(bookText.getTitle());
        book.setImage(bookText.getImage());
        book.setBanner(bookText.getBanner());
        book.setCategoryId(bookText.getCategoryId() != null ? bookText.getCategoryId() : 0);
        book.setCategoryName(bookText.getCategoryName());
        book.setFeatured(bookText.isFeatured());
        book.setDescription(bookText.getDescription()); // Include description
        return book;
    }
    
    /**
     * Convert List<BookText> to List<Book>
     */
    private List<Book> convertBookTextListToBookList(List<BookText> bookTextList) {
        List<Book> bookList = new ArrayList<>();
        for (BookText bookText : bookTextList) {
            bookList.add(convertBookTextToBook(bookText));
        }
        return bookList;
    }
    
    /**
     * Get book by ID
     */
    public void getBookById(long id, ApiCallback<Book> callback) {
        BookApiService apiService = ApiClient.getInstance().getBookApiService();
        apiService.getBookById(id).enqueue(new Callback<BookText>() {
            @Override
            public void onResponse(@NonNull Call<BookText> call, @NonNull Response<BookText> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Book book = convertBookTextToBook(response.body());
                    callback.onSuccess(book);
                } else {
                    callback.onError("Book not found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookText> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get featured books
     */
    public void getFeaturedBooks(ApiCallback<List<Book>> callback) {
        BookApiService apiService = ApiClient.getInstance().getBookApiService();
        apiService.getFeaturedBooks().enqueue(new Callback<List<BookText>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookText>> call, @NonNull Response<List<BookText>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = convertBookTextListToBookList(response.body());
                    callback.onSuccess(books);
                } else {
                    callback.onError("Failed to load featured books");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookText>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get books by category
     */
    public void getBooksByCategory(long categoryId, ApiCallback<List<Book>> callback) {
        BookApiService apiService = ApiClient.getInstance().getBookApiService();
        apiService.getBooksByCategory(categoryId).enqueue(new Callback<List<BookText>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookText>> call, @NonNull Response<List<BookText>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = convertBookTextListToBookList(response.body());
                    callback.onSuccess(books);
                } else {
                    callback.onError("Failed to load books for this category");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookText>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}

