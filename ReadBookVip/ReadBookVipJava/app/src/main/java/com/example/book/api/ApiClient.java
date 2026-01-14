package com.example.book.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // BASE_URL configuration:
    // - For Android Emulator: "http://10.0.2.2:5000/" (default - no change needed)
    // - For Real Device: "http://YOUR_COMPUTER_IP:5000/" (e.g., "http://192.168.1.100:5000/")
    //   To find your IP: Run "ipconfig" (Windows) or "ifconfig" (Mac/Linux)
    private static final String BASE_URL = "http://10.0.2.2:5000/"; // Android emulator localhost
    
    private static Retrofit retrofit;
    private static BookApiService apiService;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static BookApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(BookApiService.class);
        }
        return apiService;
    }

    public static void setBaseUrl(String baseUrl) {
        retrofit = null;
        apiService = null;
        // Recreate with new base URL if needed
    }
}

