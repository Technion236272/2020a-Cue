package com.technion.cue.data_classes;

import com.google.type.DayOfWeek;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Business {

    public String phone_number = "", business_name = "", name = "",
            description = "", logo_path = "", location = "";

    // the supported key values are { SUN, MOD, TUE, WED, THU, FRI, SAT }
    public Map<String, String> open_hours = new HashMap<>();
    public Map<String,String> attributes = new HashMap<>();

    public static class ClienteleMember {
        public String client_id = "";
        public ClienteleMember() { }
        public ClienteleMember(String client_id) {
            this.client_id = client_id;
        }
    }

    public static class Review {
        public String client_id, content;
        public Date date;
        public Review() { }
        public Review(String client_id, String content, Date date) {
            this.client_id = client_id;
            this.content = content;
            this.date = date;
        }
    }

    public static class AppointmentType {
        public String name;
        public Map<String, String> attributes = new HashMap<>();
        public AppointmentType() { }
        public AppointmentType(String name) {
            this.name = name;
        }
    }

    public Business() { }

    public Business(String business_name, String name, String phone_number) {
        this.business_name = business_name;
        this.name = name;
        this.phone_number = phone_number;
    }

}
