package com.investmango.hrconsole.model;

public class AllSalaryDetail {
    private double takenAmount;
    private long userId;  // Rename the id field to avoid conflicts
    private String userName;

    public double getTakenAmount() {
        return takenAmount;
    }

    public void setTakenAmount(double takenAmount) {
        this.takenAmount = takenAmount;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
