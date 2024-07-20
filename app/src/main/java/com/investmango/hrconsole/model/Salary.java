package com.investmango.hrconsole.model;

public class Salary {
    private int basicSalary;
    private int bonus;
    private String commentsForSalary;
    private int convience;
    private long createdDate;
    private double deduction;
    private int hra;
    private long id;
    private int incentive;
    private int medicalFund;
    private double monthlysalary;
    private double totalsalary;
    private long updatedDate;
    private String userEmail;
    private long userId;
    private String userName;
    private String userPhone;

    // Constructors, getters, and setters

    public Salary(
            int basicSalary, int bonus, String commentsForSalary, int convience, long createdDate,
            double deduction, int hra, long id, int incentive, int medicalFund, double monthlySalary,
            double totalSalary, long updatedDate, String userEmail, long userId, String userName, String userPhone) {
        this.basicSalary = basicSalary;
        this.bonus = bonus;
        this.commentsForSalary = commentsForSalary;
        this.convience = convience;
        this.createdDate = createdDate;
        this.deduction = deduction;
        this.hra = hra;
        this.id = id;
        this.incentive = incentive;
        this.medicalFund = medicalFund;
        this.monthlysalary = monthlysalary;
        this.totalsalary = totalsalary;
        this.updatedDate = updatedDate;
        this.userEmail = userEmail;
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
    }

    public int getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(int basicSalary) {
        this.basicSalary = basicSalary;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    public String getCommentsForSalary() {
        return commentsForSalary;
    }

    public void setCommentsForSalary(String commentsForSalary) {
        this.commentsForSalary = commentsForSalary;
    }

    public int getConvience() {
        return convience;
    }

    public void setConvience(int convience) {
        this.convience = convience;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public double getDeduction() {
        return deduction;
    }

    public void setDeduction(double deduction) {
        this.deduction = deduction;
    }

    public int getHra() {
        return hra;
    }

    public void setHra(int hra) {
        this.hra = hra;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIncentive() {
        return incentive;
    }

    public void setIncentive(int incentive) {
        this.incentive = incentive;
    }

    public int getMedicalFund() {
        return medicalFund;
    }

    public void setMedicalFund(int medicalFund) {
        this.medicalFund = medicalFund;
    }

    public double getMonthlySalary() {
        return monthlysalary;
    }

    public void setMonthlySalary(double monthlySalary) {
        this.monthlysalary = monthlySalary;
    }

    public double getTotalSalary() {
        return totalsalary;
    }

    public void setTotalSalary(double totalSalary) {
        this.totalsalary = totalSalary;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}
