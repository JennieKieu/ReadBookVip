package com.example.book.model;

import java.io.Serializable;

public class AdViewCount implements Serializable {
    private long advertisementId;
    private int totalViews;
    private int completedViews;
    private int viewsToday;
    private int viewsWeek;
    private int viewsMonth;
    
    public AdViewCount() {}
    
    public long getAdvertisementId() {
        return advertisementId;
    }
    
    public void setAdvertisementId(long advertisementId) {
        this.advertisementId = advertisementId;
    }
    
    public int getTotalViews() {
        return totalViews;
    }
    
    public void setTotalViews(int totalViews) {
        this.totalViews = totalViews;
    }
    
    public int getCompletedViews() {
        return completedViews;
    }
    
    public void setCompletedViews(int completedViews) {
        this.completedViews = completedViews;
    }
    
    public int getViewsToday() {
        return viewsToday;
    }
    
    public void setViewsToday(int viewsToday) {
        this.viewsToday = viewsToday;
    }
    
    public int getViewsWeek() {
        return viewsWeek;
    }
    
    public void setViewsWeek(int viewsWeek) {
        this.viewsWeek = viewsWeek;
    }
    
    public int getViewsMonth() {
        return viewsMonth;
    }
    
    public void setViewsMonth(int viewsMonth) {
        this.viewsMonth = viewsMonth;
    }
}

