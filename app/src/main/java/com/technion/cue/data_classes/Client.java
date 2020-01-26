package com.technion.cue.data_classes;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class Client implements Serializable {

    @DocumentId
    public String id;

    public String email, name, phone_number;


    public static class Favorite {
        @DocumentId
        public String id;

        public String business_id;
        public Favorite() { }
        public Favorite(String business_id) {
            this.business_id = business_id;
        }
    }

    public Client(){}

    public Client(String email, String name, String phone_number){
        this.email = email;
        this.name = name;
        this.phone_number = phone_number;

    }
}
