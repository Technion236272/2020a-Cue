package com.technion.cue.data_classes;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Appointment {
    public String business_id, client_id, type, notes;
    public Date date;

    public Appointment() { }

    public Appointment(String business_id, String client_id,
                       String type, String notes, Timestamp ts) {
        this.business_id = business_id;
        this.client_id = client_id;
        this.type = type;
        this.notes = notes;
        this.date = ts.toDate();
    }

    public Appointment(String business_id, String client_id,
                       String type, Date d) {
        this.business_id = business_id;
        this.client_id = client_id;
        this.type = type;
        this.date = d;
    }
}
