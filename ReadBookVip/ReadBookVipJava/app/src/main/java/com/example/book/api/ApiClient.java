package com.example.book.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    private final Retrofit advertisementRetrofit;  // Separate Retrofit for Advertisement with @Expose
    private final ApiService apiService;
    private final BookApiService bookApiService;
    private final AdvertisementApiService advertisementApiService;
    private final AdViewApiService adViewApiService;
    
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
        
        // Default Gson for other models
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        // Gson with @Expose annotation for Advertisement model
        Gson advertisementGson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        
        advertisementRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(advertisementGson))
                .build();
        
        apiService = retrofit.create(ApiService.class);
        bookApiService = retrofit.create(BookApiService.class);
        advertisementApiService = advertisementRetrofit.create(AdvertisementApiService.class);
        adViewApiService = retrofit.create(AdViewApiService.class);
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
    
    public AdvertisementApiService getAdvertisementApiService() {
        return advertisementApiService;
    }
    
    public AdViewApiService getAdViewApiService() {
        return adViewApiService;
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
