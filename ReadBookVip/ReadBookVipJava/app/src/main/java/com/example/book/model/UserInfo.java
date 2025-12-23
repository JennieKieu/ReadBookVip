package com.example.book.model;

import java.io.Serializable;

public class UserInfo implements Serializable {

    private long id;
    private String emailUser;
    private int currentPage;

    public UserInfo() {}

    public UserInfo(long id, String emailUser, int currentPage) {
        this.id = id;
        this.emailUser = emailUser;
        this.currentPage = currentPage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
