package com.example.book.api;

import com.example.book.model.AdView;
import com.example.book.model.AdStatistics;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AdViewApiService {
    
    @POST("api/AdViews")
    Call<AdView> createAdView(@Body CreateAdViewDto createDto);
    
    @GET("api/AdViews/statistics")
    Call<AdStatistics> getStatistics();
    
    // DTO for creating ad view
    class CreateAdViewDto {
        private long advertisementId;
        private String userEmail;
        private int duration;
        private boolean completed;
        
        public CreateAdViewDto() {}
        
        public CreateAdViewDto(long advertisementId, String userEmail, int duration, boolean completed) {
            this.advertisementId = advertisementId;
            this.userEmail = userEmail;
            this.duration = duration;
            this.completed = completed;
        }
        
        public long getAdvertisementId() {
            return advertisementId;
        }
        
        public void setAdvertisementId(long advertisementId) {
            this.advertisementId = advertisementId;
        }
        
        public String getUserEmail() {
            return userEmail;
        }
        
        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }
        
        public int getDuration() {
            return duration;
        }
        
        public void setDuration(int duration) {
            this.duration = duration;
        }
        
        public boolean isCompleted() {
            return completed;
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }
}

