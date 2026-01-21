package com.example.book.api;

/**
 * Generic callback interface for API calls
 */
public interface ApiCallback<T> {
    void onSuccess(T data);
    void onError(String errorMessage);
}

