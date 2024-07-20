package com.investmango.hrconsole.model;

public class LeaveRequestUpdateStatus {
    private String comment;
    private long id;

   private Status status;
   private Status managerStatus;

    public Status getManagerStatus() {
        return managerStatus;
    }

    public void setManagerStatus(Status managerStatus) {
        this.managerStatus = managerStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}
