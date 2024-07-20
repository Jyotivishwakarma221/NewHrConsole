package com.investmango.hrconsole.model;

import java.util.List;

public class Date {

    private long id;
    private List<Integer> date;

    private List<SaveUserLeave> list;


    public Date() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Integer> getDate() {
        return date;
    }

    public void setDate(List<Integer> date) {
        this.date = date;
    }

    public List<SaveUserLeave> getList() {
        return list;
    }

    public void setList(List<SaveUserLeave> list) {
        this.list = list;
    }

    public Date(long id, List<Integer> date, List<SaveUserLeave> list) {
        this.id = id;
        this.date = date;
        this.list = list;
    }
}
