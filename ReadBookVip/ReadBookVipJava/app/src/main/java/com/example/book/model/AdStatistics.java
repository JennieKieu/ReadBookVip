package com.example.book.model;

import java.io.Serializable;
import java.util.List;

public class AdStatistics implements Serializable {
    private int totalViews;
    private int viewsToday;
    private int viewsWeek;
    private int viewsMonth;
    private int totalCompleted;
    private int completedToday;
    private int completedWeek;
    private int completedMonth;
    private Long topAdvertisementId;
    private String topAdvertisementTitle;
    private int topAdvertisementViews;
    private List<AdViewCount> viewCountsByAdvertisement;
    
    public AdStatistics() {}
    
    public int getTotalViews() {
        return totalViews;
    }
    
    public void setTotalViews(int totalViews) {
        this.totalViews = totalViews;
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
    
    public int getTotalCompleted() {
        return totalCompleted;
    }
    
    public void setTotalCompleted(int totalCompleted) {
        this.totalCompleted = totalCompleted;
    }
    
    public int getCompletedToday() {
        return completedToday;
    }
    
    public void setCompletedToday(int completedToday) {
        this.completedToday = completedToday;
    }
    
    public int getCompletedWeek() {
        return completedWeek;
    }
    
    public void setCompletedWeek(int completedWeek) {
        this.completedWeek = completedWeek;
    }
    
    public int getCompletedMonth() {
        return completedMonth;
    }
    
    public void setCompletedMonth(int completedMonth) {
        this.completedMonth = completedMonth;
    }
    
    public Long getTopAdvertisementId() {
        return topAdvertisementId;
    }
    
    public void setTopAdvertisementId(Long topAdvertisementId) {
        this.topAdvertisementId = topAdvertisementId;
    }
    
    public String getTopAdvertisementTitle() {
        return topAdvertisementTitle;
    }
    
    public void setTopAdvertisementTitle(String topAdvertisementTitle) {
        this.topAdvertisementTitle = topAdvertisementTitle;
    }
    
    public int getTopAdvertisementViews() {
        return topAdvertisementViews;
    }
    
    public void setTopAdvertisementViews(int topAdvertisementViews) {
        this.topAdvertisementViews = topAdvertisementViews;
    }
    
    public List<AdViewCount> getViewCountsByAdvertisement() {
        return viewCountsByAdvertisement;
    }
    
    public void setViewCountsByAdvertisement(List<AdViewCount> viewCountsByAdvertisement) {
        this.viewCountsByAdvertisement = viewCountsByAdvertisement;
    }
}

