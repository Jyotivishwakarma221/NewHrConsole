package com.investmango.hrconsole.model;

public class UpdateMeeting {
    private Status status;
    private long id;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public enum Status {
        SCHEDULED,
        POSTPONED,
        PENDING,
        SUSPENDED,
        DONE
    }
}
