package com.investmango.hrconsole.model;

import java.util.List;

public class MeetingDetails {
    private long id;
    private String purpose;
    private String date;
    private String description;
    private List<Long> userIds;
    private String status;
    private long meetingTime;
    private String location;
    private long timeStamp;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(long meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public int size() {
        return 0;
    }
}
