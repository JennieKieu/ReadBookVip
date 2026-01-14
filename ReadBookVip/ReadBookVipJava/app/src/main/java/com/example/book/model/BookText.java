package com.example.book.model;

import java.io.Serializable;
import java.util.List;

public class BookText implements Serializable {
    private long id;
    private String title;
    private String image;
    private String banner;
    private Long categoryId;
    private String categoryName;
    private boolean featured;
    private int chapterCount;
    private List<Chapter> chapters;

    public BookText() {}

    public BookText(long id, String title, String image, String banner,
                    Long categoryId, String categoryName, boolean featured, int chapterCount) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.banner = banner;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.featured = featured;
        this.chapterCount = chapterCount;
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

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public int getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(int chapterCount) {
        this.chapterCount = chapterCount;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
}

