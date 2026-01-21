package com.example.book.repository;

import androidx.annotation.NonNull;

import com.example.book.api.ApiCallback;
import com.example.book.api.ApiClient;
import com.example.book.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository pattern for Category data
 */
public class CategoryRepository {
    
    private static CategoryRepository instance;
    
    private CategoryRepository() {}
    
    public static synchronized CategoryRepository getInstance() {
        if (instance == null) {
            instance = new CategoryRepository();
        }
        return instance;
    }
    
    /**
     * Get all categories from API
     */
    public void getAllCategories(ApiCallback<List<Category>> callback) {
        ApiClient.getInstance().getApiService().getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load categories: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Get category by ID
     */
    public void getCategoryById(long id, ApiCallback<Category> callback) {
        ApiClient.getInstance().getApiService().getCategoryById(id).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(@NonNull Call<Category> call, @NonNull Response<Category> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Category not found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Category> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}

