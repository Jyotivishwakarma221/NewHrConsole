package com.investmango.hrconsole.model;

public class AddEvent {
    private String description;
    private long eventDateTime;
    private String poster;
    private String subject;

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

    AddEvent() {

    }

    public AddEvent(String description, long eventDateTime, String poster, String subject) {
        this.description = description;
        this.eventDateTime = eventDateTime;
        this.poster = poster;
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "AddEvent{" +
                "description='" + description + '\'' +
                ", eventDateTime=" + eventDateTime +
                ", poster='" + poster + '\'' +
                ", subject='" + subject + '\'' +
                '}';
    }
}
