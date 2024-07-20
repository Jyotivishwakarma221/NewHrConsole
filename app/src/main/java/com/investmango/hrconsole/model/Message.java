package com.investmango.hrconsole.model;

public class Message {
    private Long id;
    private String purpose;
    private String message;
    private long meetingTime;
    private String image;
    private String docs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



    public String getDocs() {
        return docs;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

    public long getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(long meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
