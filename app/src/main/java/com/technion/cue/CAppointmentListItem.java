package com.technion.cue;

public class CAppointmentListItem {

    private String business;
    private String notes;
    private String type;
    private String date;

    public CAppointmentListItem() {}

    public CAppointmentListItem(String business, String date , String notes, String type) {
        this.business=business;
        this.notes=notes;
        this.type=type;
        this.date=date;
    }


    public String getBusiness() {
        return business;
    }
    public String getNotes() {
        return notes;
    }
    public String getType() {
        return type;
    }
    public String getDate() {
        return date;
    }
}
