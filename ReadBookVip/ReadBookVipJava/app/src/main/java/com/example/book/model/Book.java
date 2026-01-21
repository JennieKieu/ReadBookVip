package com.example.book.model;

import java.io.Serializable;
import java.util.HashMap;

public class Book implements Serializable {
    private long id;
    private String title;
    private String image;
    private boolean featured;
    private long categoryId;
    private String categoryName;
    private String banner;
    private String url;
    private String description;
    private HashMap<String, UserInfo> favorite;
    private HashMap<String, UserInfo> history;

    public Book() {}

    public Book(long id, String title, String image, boolean featured,
                long categoryId, String categoryName, String banner, String url) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.featured = featured;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.banner = banner;
        this.url = url;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, UserInfo> getFavorite() {
        return favorite;
    }

    public void setFavorite(HashMap<String, UserInfo> favorite) {
        this.favorite = favorite;
    }

    public HashMap<String, UserInfo> getHistory() {
        return history;
    }

    public void setHistory(HashMap<String, UserInfo> history) {
        this.history = history;
    }
}
