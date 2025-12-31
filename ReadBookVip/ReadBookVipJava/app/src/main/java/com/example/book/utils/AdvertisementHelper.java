package com.example.book.utils;

import android.content.Context;

import com.example.book.MyApplication;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.model.Advertisement;
import com.example.book.activity.AdvertisementActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdvertisementHelper {

    /**
     * Load quảng cáo active từ Firebase
     */
    public static void loadActiveAdvertisement(Context context, OnAdvertisementLoadedListener listener) {
        MyApplication.get(context).advertisementDatabaseReference()
                .orderByChild("isActive")
                .equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Advertisement> activeAds = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Advertisement ad = dataSnapshot.getValue(Advertisement.class);
                            if (ad != null && ad.isActive()) {
                                activeAds.add(ad);
                            }
                        }

                        if (activeAds.isEmpty()) {
                            if (listener != null) {
                                listener.onNoAdvertisement();
                            }
                        } else {
                            // Chọn ngẫu nhiên một quảng cáo
                            Advertisement selectedAd = activeAds.get(new Random().nextInt(activeAds.size()));
                            if (listener != null) {
                                listener.onAdvertisementLoaded(selectedAd);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        if (listener != null) {
                            listener.onError(error.getMessage());
                        }
                    }
                });
    }

    /**
     * Hiển thị quảng cáo
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

