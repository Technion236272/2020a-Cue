package com.technion.cue.data_classes;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Business implements Serializable {

    public String phone_number = "", business_name = "", name = "",
            description = "", logo_path = "";

    @DocumentId
    public String id;

    // the supported key values are { SUN, MOD, TUE, WED, THU, FRI, SAT }
    public Map<String, String> open_hours = new HashMap<>();
    public Map<String, String> location = new HashMap<>();
    public Map<String,String> attributes = new HashMap<>();

    public static class ClienteleMember {
        @DocumentId
        public String id;

        public String client_id = "", name = "";
        public ClienteleMember() { }
        public ClienteleMember(String client_id) {
            this.client_id = client_id;
        }
        public ClienteleMember(String client_id, String name) {
            this.client_id = client_id;
            this.name = name;
        }
    }

    public static class Review {
        @DocumentId
        public String id;

        public String client_id, content;
        public Date date;
        public Review() { }
        public Review(String client_id, String content, Timestamp ts) {
            this.client_id = client_id;
            this.content = content;
            this.date = ts.toDate();
        }
    }

    public static class AppointmentType {
        @DocumentId
        public String id;

        public String name;
        public Map<String, String> attributes = new HashMap<>();
        public AppointmentType() { }
        public AppointmentType(String name, Map<String, String> attributes) {
            this.attributes = attributes;
            this.name = name;
        }
    }

    public static class AppointmentAction {
        @DocumentId
        public String id;

        public String action_type, client_name, appointment_type,
                new_appointment_type, action_doer, notes;
        public Date action_date, appointment_date, new_appointment_date;

        public AppointmentAction() { }

        public AppointmentAction(String action_type, String client_name,
                                 Timestamp action_date, Timestamp appointment_date,
                                 Timestamp new_appointment_date,
                                 String appointment_type, String new_appointment_type,
                                 String actionDoer, String notes) {
            this.action_type = action_type;
            this.client_name = client_name;
            this.action_date = action_date.toDate();
            this.appointment_date = appointment_date.toDate();
            this.new_appointment_date = new_appointment_date.toDate();
            this.appointment_type = appointment_type;
            this.new_appointment_type = new_appointment_type;
            this.action_doer = actionDoer;
            this.notes = notes;
        }

        public AppointmentAction(String action_type, String client_name,
                                 Date action_date, Date appointment_date,
                                 Date new_appointment_date,
                                 String appointment_type, String new_appointment_type,
                                 String actionDoer, String notes) {
            this.action_type = action_type;
            this.client_name = client_name;
            this.action_date = action_date;
            this.appointment_date = appointment_date;
            this.new_appointment_date = new_appointment_date;
            this.appointment_type = appointment_type;
            this.new_appointment_type = new_appointment_type;
            this.action_doer = actionDoer;
            this.notes = notes;
        }
    }

    public Business() { }

    public Business(String business_name, String name, String phone_number, String description,
                    String state, String city, String address, Map<String,String> open_hours,
                    String logo_path) {

        this.business_name = business_name;
        this.name = name;
        this.phone_number = phone_number;

        this.logo_path = logo_path;

        this.description = description;
        location.put("state",state);
        location.put("city",city);
        location.put("address",address);

        this.open_hours.putAll(open_hours);
    }

}
