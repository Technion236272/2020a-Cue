package com.technion.cue;

public class CAppointmentListItem {

    private String appointment_business_ref;

    public CAppointmentListItem() {}

    public CAppointmentListItem(String appointment_business_ref) {
        this.appointment_business_ref=appointment_business_ref;
    }


    public String getAppointment_business_ref() {
        return appointment_business_ref;
    }
}
