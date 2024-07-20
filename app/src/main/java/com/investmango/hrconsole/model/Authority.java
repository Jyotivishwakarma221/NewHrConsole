package com.investmango.hrconsole.model;

import com.google.gson.annotations.SerializedName;

public class Authority {
    @SerializedName("authority")
    private String authority;
    public Authority(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
