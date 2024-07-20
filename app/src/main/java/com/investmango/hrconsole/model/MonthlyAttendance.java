package com.investmango.hrconsole.model;

public class MonthlyAttendance {
    private String month;
    private long totalAbsent;
    private long totalPresent;
    private long totalWorkingDays;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getTotalAbsent() {
        return totalAbsent;
    }

    public void setTotalAbsent(long totalAbsent) {
        this.totalAbsent = totalAbsent;
    }

    public long getTotalPresent() {
        return totalPresent;
    }

    public void setTotalPresent(long totalPresent) {
        this.totalPresent = totalPresent;
    }

    public long getTotalWorkingDays() {
        return totalWorkingDays;
    }

    public void setTotalWorkingDays(long totalWorkingDays) {
        this.totalWorkingDays = totalWorkingDays;
    }

    public int getMonthIndex() {
        return 0;
    }
}
