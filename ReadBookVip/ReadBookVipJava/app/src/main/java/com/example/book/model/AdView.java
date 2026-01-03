package com.example.book.model;

import java.io.Serializable;

public class AdView implements Serializable {
    private long id;
    private long advertisementId;  // ID của quảng cáo
    private String advertisementTitle;  // Tên quảng cáo (để dễ query)
    private String userEmail;  // Email user xem quảng cáo
    private long viewedAt;  // Thời gian xem
    private int duration;  // Thời lượng xem (giây) - optional
    private boolean completed;  // Xem hết hay skip

    public AdView() {}

    public AdView(long id, long advertisementId, String advertisementTitle, 
                  String userEmail, long viewedAt, int duration, boolean completed) {
        this.id = id;
        this.advertisementId = advertisementId;
        this.advertisementTitle = advertisementTitle;
        this.userEmail = userEmail;
        this.viewedAt = viewedAt;
        this.duration = duration;
        this.completed = completed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(long advertisementId) {
        this.advertisementId = advertisementId;
    }

    public String getAdvertisementTitle() {
        return advertisementTitle;
    }

    public void setAdvertisementTitle(String advertisementTitle) {
        this.advertisementTitle = advertisementTitle;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(long viewedAt) {
        this.viewedAt = viewedAt;
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




