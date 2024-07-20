
package com.investmango.hrconsole.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MeetingResponse {

    @SerializedName("assignByEmail")
    private String mAssignByEmail;
    @SerializedName("assignById")
    private Long mAssignById;
    @SerializedName("assignByName")
    private String mAssignByName;
    @SerializedName("assignByPhone")
    private String mAssignByPhone;
    @SerializedName("assignedUsers")
    private List<AssignedUser> mAssignedUsers;
    @SerializedName("createdDate")
    private Long mCreatedDate;
    @SerializedName("date")
    private String mDate;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("id")
    private Long mId;
    @SerializedName("location")
    private String mLocation;
    @SerializedName("meetingTime")
    private Long mMeetingTime;
    @SerializedName("purpose")
    private String mPurpose;
    @SerializedName("status")
    private String mStatus;

    public String getAssignByEmail() {
        return mAssignByEmail;
    }

    public void setAssignByEmail(String assignByEmail) {
        mAssignByEmail = assignByEmail;
    }

    public Long getAssignById() {
        return mAssignById;
    }

    public void setAssignById(Long assignById) {
        mAssignById = assignById;
    }

    public String getAssignByName() {
        return mAssignByName;
    }

    public void setAssignByName(String assignByName) {
        mAssignByName = assignByName;
    }

    public String getAssignByPhone() {
        return mAssignByPhone;
    }

    public void setAssignByPhone(String assignByPhone) {
        mAssignByPhone = assignByPhone;
    }

    public List<AssignedUser> getAssignedUsers() {
        return mAssignedUsers;
    }

    public void setAssignedUsers(List<AssignedUser> assignedUsers) {
        mAssignedUsers = assignedUsers;
    }

    public Long getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(Long createdDate) {
        mCreatedDate = createdDate;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public Long getMeetingTime() {
        return mMeetingTime;
    }

    public void setMeetingTime(Long meetingTime) {
        mMeetingTime = meetingTime;
    }

    public String getPurpose() {
        return mPurpose;
    }

    public void setPurpose(String purpose) {
        mPurpose = purpose;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

}
