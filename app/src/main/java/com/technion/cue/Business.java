package com.technion.cue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Business {

    public String b_id;
    public String business_name;
    public String bo_name;
    public String description;
    public String logo_path;
    public String location;
    public String open_hours;
    public List<String> b_appointments = new ArrayList<>(); //Appointments
    public List<String> types = new ArrayList<>(); // Type
    public List<String> reviews = new ArrayList<>(); // Review
    public List<Client> b_clients = new ArrayList<>();
    public Map<String,String> b_attributes = new HashMap<>();

    public Business() { }

    public Business(String b_id, String business_name, String bo_name) {
        this.b_id = b_id;
        this.business_name = business_name;
        this.bo_name = bo_name;
        this.logo_path = "";
        this.description = "";
        this.location = "";
        this.open_hours= "";
    }

}
