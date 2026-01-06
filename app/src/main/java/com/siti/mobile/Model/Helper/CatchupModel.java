package com.siti.mobile.Model.Helper;

public class CatchupModel {
    private String Date;
    private String Time;
    private String displayText;

    public CatchupModel(String date, String time, String displayText) {
        Date = date;
        Time = time;
        this.displayText = displayText;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}
