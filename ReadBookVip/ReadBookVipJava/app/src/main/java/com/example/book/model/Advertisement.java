package com.example.book.model;

import com.example.book.utils.DateTimeDeserializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Advertisement implements Serializable {
    @Expose(serialize = false, deserialize = true)
    private long id;
    
    @Expose
    private String title;
    
    @Expose
    private String videoUrl;  // Google Drive link to video
    
    @Expose
    private String url;  // URL to open when ad is clicked
    
    @Expose
    private String thumbnailUrl;  // Thumbnail image (optional)
    
    @Expose
    private boolean isActive;  // Enable/disable advertisement
    
    @Expose(serialize = false, deserialize = true)
    @JsonAdapter(DateTimeDeserializer.class)
    @SerializedName("createdAt")
    private long createdAt;
    
    @Expose(serialize = false, deserialize = true)
    @JsonAdapter(DateTimeDeserializer.class)
    @SerializedName("updatedAt")
    private long updatedAt;
    
    @Expose(serialize = false, deserialize = true)
    private int viewCount;  // Total view count

    public Advertisement() {}

    public Advertisement(long id, String title, String videoUrl) {
        this.id = id;
        this.title = title;
        this.videoUrl = videoUrl;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isActive = true;
        this.viewCount = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}




