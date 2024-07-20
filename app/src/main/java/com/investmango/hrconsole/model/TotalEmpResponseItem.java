package com.investmango.hrconsole.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TotalEmpResponseItem implements Serializable {

    @SerializedName("userPhone")
    private String userPhone;

    @SerializedName("userEmail")
    private String userEmail;

    @SerializedName("id")
    private long id;

    @SerializedName("designation")
    private String designation;

    @SerializedName("managerId")
    private Object managerId;

    @SerializedName("userName")
    private String userName;
    boolean isSelected;

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	@SerializedName("managerName")
    private Object managerName;

    public String getUserPhone() {
        return userPhone;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public long getId() {
        return id;
    }

    public String getDesignation() {
        return designation;
    }

    public Object getManagerId() {
        return managerId;
    }

    public String getUserName() {
        return userName;
    }

    public Object getManagerName() {
        return managerName;
    }
}