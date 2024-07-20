package com.investmango.hrconsole.model;

public class Event {
    private String announcedByEmail;
    private long announcedById;
    private String announcedByName;
    private String announcedByPhone;
    private long createdDate;
    private String description;
    private long eventDateTime;
    private long id;
    private String poster;
    private String subject;
    private long updatedDate;

    public String getAnnouncedByEmail() {
        return announcedByEmail;
    }

    public void setAnnouncedByEmail(String announcedByEmail) {
        this.announcedByEmail = announcedByEmail;
    }

    public long getAnnouncedById() {
        return announcedById;
    }

    public void setAnnouncedById(long announcedById) {
        this.announcedById = announcedById;
    }

    public String getAnnouncedByName() {
        return announcedByName;
    }

    public void setAnnouncedByName(String announcedByName) {
        this.announcedByName = announcedByName;
    }

    public String getAnnouncedByPhone() {
        return announcedByPhone;
    }

    public void setAnnouncedByPhone(String announcedByPhone) {
        this.announcedByPhone = announcedByPhone;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(long eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }


}
