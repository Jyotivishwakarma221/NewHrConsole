
package com.investmango.hrconsole.model;

import com.google.gson.annotations.SerializedName;
public class AssignedUser {

    @SerializedName("assignToEmail")
    private String mAssignToEmail;
    @SerializedName("assignToId")
    private Long mAssignToId;
    @SerializedName("assignToName")
    private String mAssignToName;
    @SerializedName("assignToPhone")
    private String mAssignToPhone;

    public String getAssignToEmail() {
        return mAssignToEmail;
    }

    public void setAssignToEmail(String assignToEmail) {
        mAssignToEmail = assignToEmail;
    }

    public Long getAssignToId() {
        return mAssignToId;
    }

    public void setAssignToId(Long assignToId) {
        mAssignToId = assignToId;
    }

    public String getAssignToName() {
        return mAssignToName;
    }

    public void setAssignToName(String assignToName) {
        mAssignToName = assignToName;
    }

    public String getAssignToPhone() {
        return mAssignToPhone;
    }

    public void setAssignToPhone(String assignToPhone) {
        mAssignToPhone = assignToPhone;
    }

}
