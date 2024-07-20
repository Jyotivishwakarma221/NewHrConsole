package com.investmango.hrconsole.model;

import java.util.List;

public class MeetingDetailsAdmin {
    private String assignByEmail;
    private long assignById;
    private String assignByName;
    private String assignByPhone;
    private List<AssignedUser> assignedUsers;
    private long createdDate;
    private String date;
    private String description;
    private long id;
    private String location;
    private long meetingTime;
    private String purpose;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Constructors
    public MeetingDetailsAdmin() {
    }

    public MeetingDetailsAdmin(String assignByEmail, long assignById, String assignByName, String assignByPhone, List<AssignedUser> assignedUsers, long createdDate, String date, String description, long id, String location, long meetingTime, String purpose) {
        this.assignByEmail = assignByEmail;
        this.assignById = assignById;
        this.assignByName = assignByName;
        this.assignByPhone = assignByPhone;
        this.assignedUsers = assignedUsers;
        this.createdDate = createdDate;
        this.date = date;
        this.description = description;
        this.id = id;
        this.location = location;
        this.meetingTime = meetingTime;
        this.purpose = purpose;
    }

    // Getters and Setters
    public String getAssignByEmail() {
        return assignByEmail;
    }

    public void setAssignByEmail(String assignByEmail) {
        this.assignByEmail = assignByEmail;
    }

    public long getAssignById() {
        return assignById;
    }

    public void setAssignById(long assignById) {
        this.assignById = assignById;
    }

    public String getAssignByName() {
        return assignByName;
    }

    public void setAssignByName(String assignByName) {
        this.assignByName = assignByName;
    }

    public String getAssignByPhone() {
        return assignByPhone;
    }

    public void setAssignByPhone(String assignByPhone) {
        this.assignByPhone = assignByPhone;
    }

    public List<AssignedUser> getAssignedUsers() {
        return assignedUsers;
    }

    public void setAssignedUsers(List<AssignedUser> assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(long meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }


    // Inner class for AssignedUser
    public static class AssignedUser {
        private String assignToEmail;
        private long assignToId;
        private String assignToName;
        private String assignToPhone;

        // Constructors
        public AssignedUser() {
        }

        public AssignedUser(String assignToEmail, long assignToId, String assignToName, String assignToPhone) {
            this.assignToEmail = assignToEmail;
            this.assignToId = assignToId;
            this.assignToName = assignToName;
            this.assignToPhone = assignToPhone;
        }

        // Getters and Setters
        public String getAssignToEmail() {
            return assignToEmail;
        }

        public void setAssignToEmail(String assignToEmail) {
            this.assignToEmail = assignToEmail;
        }

        public long getAssignToId() {
            return assignToId;
        }

        public void setAssignToId(long assignToId) {
            this.assignToId = assignToId;
        }

        public String getAssignToName() {
            return assignToName;
        }

        public void setAssignToName(String assignToName) {
            this.assignToName = assignToName;
        }

        public String getAssignToPhone() {
            return assignToPhone;
        }

        public void setAssignToPhone(String assignToPhone) {
            this.assignToPhone = assignToPhone;
        }
    }
}
