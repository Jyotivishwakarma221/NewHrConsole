package com.investmango.hrconsole.model;

public class FeedbackRequest {
    private String feedback;
    private String givenByEmail;
    private long givenById;
    private String givenByName;
    private String section;
    private String status;
    private String url;


    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getGivenByEmail() {
        return givenByEmail;
    }

    public void setGivenByEmail(String givenByEmail) {
        this.givenByEmail = givenByEmail;
    }

    public long getGivenById() {
        return givenById;
    }

    public void setGivenById(long givenById) {
        this.givenById = givenById;
    }

    public String getGivenByName() {
        return givenByName;
    }

    public void setGivenByName(String givenByName) {
        this.givenByName = givenByName;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

