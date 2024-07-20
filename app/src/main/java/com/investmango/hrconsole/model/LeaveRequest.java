package com.investmango.hrconsole.model;

import java.util.List;

public class LeaveRequest{
    private String byCompany;
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
    private String approvedByName;
    private String approvedById;

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getApprovedById() {
        return approvedById;
    }

    public void setApprovedById(String approvedById) {
        this.approvedById = approvedById;
    }

    public String getByCompany() {
        return byCompany;
    }

    public void setByCompany(String byCompany) {
        this.byCompany = byCompany;
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


}
