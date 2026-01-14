package com.example.book.model;

import java.io.Serializable;

public class Chapter implements Serializable {
    private long id;
    private long bookId;
    private int chapterNumber;
    private String title;
    private String content; // HTML content

    public Chapter() {}

    public Chapter(long id, long bookId, int chapterNumber, String title, String content) {
        this.id = id;
        this.bookId = bookId;
        this.chapterNumber = chapterNumber;
        this.title = title;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBookId() {
        return bookId;
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

