package com.investmango.hrconsole.model;

import java.util.List;

public class AssignTask {
    private long userId;
    private String subject;
    private List<String> fileurl;

    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<String> getFileurl() {
        return fileurl;
    }

    public void setFileurl(List<String> fileUrl) {
        this.fileurl = fileUrl;
    }
}
