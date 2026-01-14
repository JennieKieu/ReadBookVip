package com.example.book.api;

import com.example.book.model.BookText;
import com.example.book.model.Chapter;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookApiService {
    // Books API
    @GET("api/books")
    Call<List<BookText>> getAllBooks();

    @GET("api/books/{id}")
    Call<BookText> getBookById(@Path("id") long id);

    @GET("api/books/featured")
    Call<List<BookText>> getFeaturedBooks();

    @GET("api/books/category/{categoryId}")
    Call<List<BookText>> getBooksByCategory(@Path("categoryId") long categoryId);

    @POST("api/books")
    Call<BookText> createBook(@Body BookText book);

    @PUT("api/books/{id}")
    Call<BookText> updateBook(@Path("id") long id, @Body BookText book);

    @DELETE("api/books/{id}")
    Call<Void> deleteBook(@Path("id") long id);

    // Chapters API
    @GET("api/chapters/books/{bookId}")
    Call<List<Chapter>> getChaptersByBook(@Path("bookId") long bookId);

    @GET("api/chapters/{id}")
    Call<Chapter> getChapterById(@Path("id") long id);

    @POST("api/chapters")
    Call<Chapter> createChapter(@Body Chapter chapter);

    @PUT("api/chapters/{id}")
    Call<Chapter> updateChapter(@Path("id") long id, @Body Chapter chapter);

    @DELETE("api/chapters/{id}")
    Call<Void> deleteChapter(@Path("id") long id);

    // History API
    @GET("api/books/{bookId}/history")
    Call<Map<String, Object>> getHistory(@Path("bookId") long bookId, @Query("userEmail") String userEmail);

    @POST("api/books/{bookId}/history")
    Call<Map<String, Object>> saveHistory(@Path("bookId") long bookId, @Body Map<String, Object> historyData);

    // Favorites API
    @GET("api/books/favorites")
    Call<List<Long>> getFavorites(@Query("userEmail") String userEmail);

    @POST("api/books/{bookId}/favorites")
    Call<Void> addFavorite(@Path("bookId") long bookId, @Query("userEmail") String userEmail);

    @DELETE("api/books/{bookId}/favorites")
    Call<Void> removeFavorite(@Path("bookId") long bookId, @Query("userEmail") String userEmail);
}

