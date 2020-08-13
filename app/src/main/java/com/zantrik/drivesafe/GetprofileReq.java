package com.zantrik.drivesafe;

public class GetprofileReq {
    private String Phone;
    private int userid;
    private GetprofileRes Data;

    public GetprofileRes getData() {
        return Data;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }
}
