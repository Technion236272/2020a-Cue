package com.technion.cue.data_classes;

import java.util.Date;

public class Appointment {
    public String business_id, client_id, type, notes;
    public Date date;

    public Appointment() { }

    public Appointment(String business_id, String client_id, String type, String notes, Date date) {
        this.business_id = business_id;
        this.client_id = client_id;
        this.type = type;
        this.notes = notes;
        this.date = date;
    }
}
