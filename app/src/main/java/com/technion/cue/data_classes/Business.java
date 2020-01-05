package com.technion.cue.data_classes;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
