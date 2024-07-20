package com.investmango.hrconsole.model;

import java.io.Serializable;


public class Attendance implements Serializable {
    private String date;
    private Long id;
    private String inLatLong;
    private long inTime;
    private String outLatLong;
    private long outTime;
    private String userEmail;
    private long userId;
    private String userName;
    private String userPhone;
    private User user;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInLatLong() {
        return inLatLong;
    }

    public void setInLatLong(String inLatLong) {
        this.inLatLong = inLatLong;
    }

    public long getInTime() {
        return inTime;
    }

    public void setInTime(long inTime) {
        this.inTime = inTime;
    }

    public String getOutLatLong() {
        return outLatLong;
    }

    public void setOutLatLong(String outLatLong) {
        this.outLatLong = outLatLong;
    }

    public long getOutTime() {
        return outTime;
    }

    public void setOutTime(long outTime) {
        this.outTime = outTime;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
