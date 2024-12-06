package com.investmango.hrconsole.model;

import java.util.List;

public class AddTask {
    private String comments;
    private Date dateTime;
    private List<String> fileurl;
    private long id;
    private String status;
    private String subject;
    private String title;
    private Long deadLine;
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(Long deadLine) {
        this.deadLine = deadLine;
    }



    public Long getDeadline() {
        return deadLine;
    }

    public void setDeadline(Long deadline) {
        deadLine = deadline;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public List<String> getFileurl() {
        return fileurl;
    }

    public void setFileurl( List<String> fileUrl) {
        this.fileurl = fileUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
