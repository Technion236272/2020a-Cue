package com.technion.cue.data_classes;

import java.util.ArrayList;
import java.util.List;

public class Client {
    public String u_id,email, name, phone;
    public List<Business> favorites = new ArrayList<>();
    public List<String> c_appoitments = new ArrayList<>(); //Appoitment


    public Client(){}

    public Client(String u_id, String email, String name, String phone){
        this.u_id = u_id;
        this.email = email;
        this.name = name;
        this.phone = phone;

    }
}
