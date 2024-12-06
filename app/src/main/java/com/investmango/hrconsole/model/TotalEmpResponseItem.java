package com.investmango.hrconsole.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TotalEmpResponseItem implements Serializable {

    @SerializedName("userPhone")
    private String userPhone;

    @SerializedName("userEmail")
    private String userEmail;

    @SerializedName("id")
    private long id;

    @SerializedName("isTodayPresent")
    private boolean isTodayPresent;

    public boolean isTodayPresent() {
        return isTodayPresent;
    }

    public void setTodayPresent(boolean todayPresent) {
        isTodayPresent = todayPresent;
    }

    @SerializedName("designation")
    private String designation;

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setManagerId(Object managerId) {
        this.managerId = managerId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setManagerName(Object managerName) {
        this.managerName = managerName;
    }

    @SerializedName("profile")
    private String profile;

    @SerializedName("managerId")
    private Object managerId;

    @SerializedName("userName")
    private String userName;
    boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @SerializedName("managerName")
    private Object managerName;

    public String getUserPhone() {
        return userPhone;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public long getId() {
        return id;
    }

    public String getDesignation() {
        return designation;
    }

    public Object getManagerId() {
        return managerId;
    }

    public String getUserName() {
        return userName;
    }

    public Object getManagerName() {
        return managerName;
    }
}