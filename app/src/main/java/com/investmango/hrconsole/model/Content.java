
package com.investmango.hrconsole.model;

import com.google.gson.annotations.SerializedName;


public class Content {

    @SerializedName("date")
    private String mDate;
    @SerializedName("designation")
    private String mDesignation;
    @SerializedName("id")
    private Long mId;
    @SerializedName("inLatLong")
    private String mInLatLong;
    @SerializedName("inTime")
    private Long mInTime;
    @SerializedName("outLatLong")
    private Object mOutLatLong;
    @SerializedName("outTime")
    private Long mOutTime;
    @SerializedName("profileImage")
    private String mProfileImage;
    @SerializedName("userEmail")
    private String mUserEmail;
    @SerializedName("userId")
    private Long mUserId;
    @SerializedName("userName")
    private String mUserName;
    @SerializedName("userPhone")
    private String mUserPhone;

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDesignation() {
        return mDesignation;
    }

    public void setDesignation(String designation) {
        mDesignation = designation;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getInLatLong() {
        return mInLatLong;
    }

    public void setInLatLong(String inLatLong) {
        mInLatLong = inLatLong;
    }

    public Long getInTime() {
        return mInTime;
    }

    public void setInTime(Long inTime) {
        mInTime = inTime;
    }

    public Object getOutLatLong() {
        return mOutLatLong;
    }

    public void setOutLatLong(Object outLatLong) {
        mOutLatLong = outLatLong;
    }

    public Long getOutTime() {
        return mOutTime;
    }

    public void setOutTime(Long outTime) {
        mOutTime = outTime;
    }

    public String getProfileImage() {
        return mProfileImage;
    }

    public void setProfileImage(String profileImage) {
        mProfileImage = profileImage;
    }

    public String getUserEmail() {
        return mUserEmail;
    }

    public void setUserEmail(String userEmail) {
        mUserEmail = userEmail;
    }

    public Long getUserId() {
        return mUserId;
    }

    public void setUserId(Long userId) {
        mUserId = userId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getUserPhone() {
        return mUserPhone;
    }

    public void setUserPhone(String userPhone) {
        mUserPhone = userPhone;
    }

}
