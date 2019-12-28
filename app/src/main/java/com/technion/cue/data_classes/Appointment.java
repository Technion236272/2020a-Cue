package com.technion.cue.data_classes;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.Date;

public class Appointment implements Serializable {
    public String business_id, client_id, type, notes;

    @DocumentId
    public String id;

    public Date date;

    public Appointment() { }

    public Appointment(String business_id, String client_id,
                       String type, String notes, Timestamp ts,String appointment_id) {
        this.business_id = business_id;
        this.client_id = client_id;
        this.type = type;
        this.notes = notes;
        this.date = ts.toDate();
//        this.appointment_id = appointment_id;
    }

    public Appointment(String business_id, String client_id,
                       String type, Date d,String appointment_id) {
        this.business_id = business_id;
        this.client_id = client_id;
        this.type = type;
        this.date = d;
//        this.appointment_id = appointment_id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Appointment) {
            Appointment other = (Appointment)obj;
            return (other.date == this.date || other.date.equals(this.date))
                    && ((other.notes == this.notes) || other.notes.equals(this.notes))
                    && (other.client_id == this.client_id || other.client_id.equals(this.client_id))
                    && (other.business_id == this.business_id || other.business_id.equals(this.business_id))
                    && (other.type == this.type || other.type.equals(this.type));
        }
        return false;
    }

    // TODO: consider overriding hashCode
}
