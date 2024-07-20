package com.investmango.hrconsole.model;

import java.util.List;

public class ApprovedLeaves {
    private String ofByCompany;
    private boolean approved;
    private String comment;
    private long createdDate;
    private long id;
    private List<String> leaveDates;
    private String leaveType;
    private String reason;
    private String status;
    private long updatedDateTime;
    private String userEmail;
    private long userId;
    private String userName;
    private String userPhone;

    public String getOfByCompany() {
        return ofByCompany;
    }

    public void setOfByCompany(String ofByCompany) {
        this.ofByCompany = ofByCompany;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getLeaveDates() {
        return leaveDates;
    }

    public void setLeaveDates(List<String> leaveDates) {
        this.leaveDates = leaveDates;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getUpdatedDateTime() {
        return updatedDateTime;
    }

    public void setUpdatedDateTime(long updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
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

    public ApprovedLeaves(String ofByCompany, boolean approved, String comment, long createdDate, long id, List<String> leaveDates, String leaveType, String reason, String status, long updatedDateTime, String userEmail, long userId, String userName, String userPhone) {
        this.ofByCompany = ofByCompany;
        this.approved = approved;
        this.comment = comment;
        this.createdDate = createdDate;
        this.id = id;
        this.leaveDates = leaveDates;
        this.leaveType = leaveType;
        this.reason = reason;
        this.status = status;
        this.updatedDateTime = updatedDateTime;
        this.userEmail = userEmail;
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
    }

    @Override
    public String toString() {
        return "ApprovedLeaves{" +
                "ofByCompany='" + ofByCompany + '\'' +
                ", approved=" + approved +
                ", comment='" + comment + '\'' +
                ", createdDate=" + createdDate +
                ", id=" + id +
                ", leaveDates=" + leaveDates +
                ", leaveType='" + leaveType + '\'' +
                ", reason='" + reason + '\'' +
                ", status='" + status + '\'' +
                ", updatedDateTime=" + updatedDateTime +
                ", userEmail='" + userEmail + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                '}';
    }
}
