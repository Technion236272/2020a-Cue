package com.technion.cue;

import java.util.ArrayList;
import java.util.List;

public class Client {
    public String id,email, name, phone;
    public List<Business> favorites = new ArrayList<>();
    public List<String> c_appoitments = new ArrayList<>(); //Appoitment


    public Client(){}

    public Client(String id, String email, String name, String phone){
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;

    }
}
