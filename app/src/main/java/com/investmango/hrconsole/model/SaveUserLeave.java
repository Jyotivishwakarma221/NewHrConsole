package com.investmango.hrconsole.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SaveUserLeave implements Serializable {
    private String comment;
    private List<String> leaveDates;
    private long id;
    private String leaveType;
    private String userName;
    private String reason;
    private String status;
    private String formattedDate;
    private String approvedByName;

    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum LeaveType {
        ABSENT("Absent"),
        HALF_DAY("Half Day"),
        PAID_LEAVE("Paid Leave"),
        LATE("Late");

        private final String label;

        LeaveType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public static LeaveType fromString(String text) {
            for (LeaveType leaveType : LeaveType.values()) {
                if (leaveType.label.equalsIgnoreCase(text)) {
                    return leaveType;
                }
            }
            return null;
        }
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getLeaveDates() {
        return leaveDates;
    }

    public void setLeaveDates(List<String> leaveDates) {
        this.leaveDates = leaveDates;
    }

    public long getLeaveId() {
        return id;
    }

    public void setLeaveId(long leaveId) {
        this.id = id;
    }

    public String getFormattedDate() {
        return formattedDate;
    }
    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public SaveUserLeave(List<String> leaveDates, String reason, String leaveType, String status, String comment) {
        this.leaveDates = leaveDates;
        this.reason = reason;
        this.leaveType = leaveType;
        this.status = status;
        this.comment = comment;
    }

    @NonNull
    @Override
    public String toString() {
        return "SaveUserLeave{" +
                "leaveDates=" + leaveDates +
                ", reason='" + reason + '\'' +
                ", leaveType='" + leaveType + '\'' +
                ", status='" + status + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
