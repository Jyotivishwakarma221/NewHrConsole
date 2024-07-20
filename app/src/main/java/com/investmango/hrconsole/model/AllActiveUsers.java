package com.investmango.hrconsole.model;

import java.util.List;

public class AllActiveUsers {
    private boolean active;
    private List<Authority> authorities;
    private String createdByEmail;
    private long createdById;
    private String createdByName;
    private String createdByPhone;
    private long createdDate;
    private String currentAddress;
    private String department;
    private String designation;
    private String dob;
    private String email;
    private boolean enabled;
    private String firstName;
    private String gender;
    private long id;
    private long lastLogin;
    private String lastName;
    private String permanentAddress;
    private String phone;
    private String profileImage;
    private long updatedDate;
    private String userName;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedByPhone() {
        return createdByPhone;
    }

    public void setCreatedByPhone(String createdByPhone) {
        this.createdByPhone = createdByPhone;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public static class Authority {
        private String authority;

        // Constructors, getters, and setters go here

        @Override
        public String toString() {
            return "Authority{" +
                    "authority='" + authority + '\'' +
                    '}';
        }
    }

    // Additional methods, if needed
    @Override
    public String toString() {
        return "AllActiveUsers{" +
                "active=" + active +
                ", authorities=" + authorities +
                ", createdByEmail='" + createdByEmail + '\'' +
                ", createdById=" + createdById +
                ", createdByName='" + createdByName + '\'' +
                ", createdByPhone='" + createdByPhone + '\'' +
                ", createdDate=" + createdDate +
                ", currentAddress='" + currentAddress + '\'' +
                ", department='" + department + '\'' +
                ", designation='" + designation + '\'' +
                ", dob='" + dob + '\'' +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                ", firstName='" + firstName + '\'' +
                ", gender='" + gender + '\'' +
                ", id=" + id +
                ", lastLogin=" + lastLogin +
                ", lastName='" + lastName + '\'' +
                ", permanentAddress='" + permanentAddress + '\'' +
                ", phone='" + phone + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", updatedDate=" + updatedDate +
                ", userName='" + userName + '\'' +
                '}';


    }
}
