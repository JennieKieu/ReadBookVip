package com.example.book.utils;

import android.content.Context;

import com.example.book.api.AdvertisementApiService;
import com.example.book.api.ApiClient;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.model.Advertisement;
import com.example.book.activity.AdvertisementActivity;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdvertisementHelper {

    /**
     * Load active advertisements from API
     */
    public static void loadActiveAdvertisement(Context context, OnAdvertisementLoadedListener listener) {
        AdvertisementApiService apiService = ApiClient.getInstance().getAdvertisementApiService();
        
        apiService.getActiveAdvertisements().enqueue(new Callback<List<Advertisement>>() {
            @Override
            public void onResponse(Call<List<Advertisement>> call, Response<List<Advertisement>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Advertisement> activeAds = response.body();
                    // Select random advertisement
                    Advertisement selectedAd = activeAds.get(new Random().nextInt(activeAds.size()));
                    if (listener != null) {
                        listener.onAdvertisementLoaded(selectedAd);
                    }
                } else {
                    if (listener != null) {
                        listener.onNoAdvertisement();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Advertisement>> call, Throwable t) {
                if (listener != null) {
                    listener.onError(t.getMessage());
                }
            }
        });
    }

    /**
     * Show advertisement
     */
    public static void showAdvertisement(Context context, Advertisement advertisement) {
        if (context == null || advertisement == null) return;

        android.os.Bundle bundle = new android.os.Bundle();
        bundle.putSerializable(Constant.OBJECT_ADVERTISEMENT, advertisement);
        GlobalFunction.startActivity(context, AdvertisementActivity.class, bundle);
    }

    public interface OnAdvertisementLoadedListener {
        void onAdvertisementLoaded(Advertisement advertisement);
        void onNoAdvertisement();
        void onError(String error);
    }
}
