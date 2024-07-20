package com.investmango.hrconsole.model;

public class PresentEmployee {
    private String date;
    private String designation;
    private long id;
    private double inLat;
    private double inLong;
    private long inTime;
    private double outLat;
    private double outLong;
    private long outTime;
    private String profileImage;
    private String userEmail;
    private long userId;
    private String userName;
    private String userPhone;

    // Constructors, getters, and setters

    public PresentEmployee() {
        // Default constructor
    }

    public PresentEmployee(String date, String designation, long id, double inLat, double inLong, long inTime,
                           double outLat, double outLong, long outTime, String profileImage, String userEmail,
                           long userId, String userName, String userPhone) {
        this.date = date;
        this.designation = designation;
        this.id = id;
        this.inLat = inLat;
        this.inLong = inLong;
        this.inTime = inTime;
        this.outLat = outLat;
        this.outLong = outLong;
        this.outTime = outTime;
        this.profileImage = profileImage;
        this.userEmail = userEmail;
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
    }

    // Getters and setters for each field

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getInLat() {
        return inLat;
    }

    public void setInLat(double inLat) {
        this.inLat = inLat;
    }

    public double getInLong() {
        return inLong;
    }

    public void setInLong(double inLong) {
        this.inLong = inLong;
    }

    public long getInTime() {
        return inTime;
    }

    public void setInTime(long inTime) {
        this.inTime = inTime;
    }

    public double getOutLat() {
        return outLat;
    }

    public void setOutLat(double outLat) {
        this.outLat = outLat;
    }

    public double getOutLong() {
        return outLong;
    }

    public void setOutLong(double outLong) {
        this.outLong = outLong;
    }

    public long getOutTime() {
        return outTime;
    }

    public void setOutTime(long outTime) {
        this.outTime = outTime;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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
}
