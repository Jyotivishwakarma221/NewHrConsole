package com.investmango.hrconsole.model;

public class calenderDate {
    String Date;

    public calenderDate(String date, String dayOfDay) {
        Date = date;
        this.dayOfDay = dayOfDay;
    }

    String dayOfDay;

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDayOfDay() {
        return dayOfDay;
    }

    public void setDayOfDay(String dayOfDay) {
        this.dayOfDay = dayOfDay;
    }
}
