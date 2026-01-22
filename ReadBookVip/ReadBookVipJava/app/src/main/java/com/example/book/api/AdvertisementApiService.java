package com.example.book.api;

import com.example.book.model.Advertisement;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AdvertisementApiService {
    
    @GET("api/advertisements")
    Call<List<Advertisement>> getAllAdvertisements();
    
    @GET("api/advertisements/active")
    Call<List<Advertisement>> getActiveAdvertisements();
    
    @GET("api/advertisements/{id}")
    Call<Advertisement> getAdvertisementById(@Path("id") long id);
    
    @POST("api/advertisements")
    Call<Advertisement> createAdvertisement(@Body Advertisement advertisement);
    
    @PUT("api/advertisements/{id}")
    Call<Advertisement> updateAdvertisement(@Path("id") long id, @Body Advertisement advertisement);
    
    @DELETE("api/advertisements/{id}")
    Call<Void> deleteAdvertisement(@Path("id") long id);
    
    @POST("api/advertisements/{id}/increment-view")
    Call<Void> incrementViewCount(@Path("id") long id);
}

