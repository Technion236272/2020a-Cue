package com.technion.cue.data_classes;

public class Client {

    public String email, name, phone_number;


    static class Favorite {
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
