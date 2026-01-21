package com.example.book.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    
    // TODO: Replace with your actual backend URL
    // For emulator: http://10.0.2.2:5000/
    // For real device: http://YOUR_COMPUTER_IP:5000/
    private static final String BASE_URL = "http://10.0.2.2:5000/";
    
    private static ApiClient instance;
    private final Retrofit retrofit;
    private final ApiService apiService;
    private final BookApiService bookApiService;
    
    private ApiClient() {
        // Logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(ApiService.class);
        bookApiService = retrofit.create(BookApiService.class);
    }
    
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    public ApiService getApiService() {
        return apiService;
    }
    
    public BookApiService getBookApiService() {
        return bookApiService;
    }
    
    /**
     * Update base URL dynamically (useful for switching between emulator and real device)
     */
    public static void setBaseUrl(String baseUrl) {
        instance = null; // Force recreation with new URL
        // Recreate with new base URL
        instance = new ApiClient();
    }
}
