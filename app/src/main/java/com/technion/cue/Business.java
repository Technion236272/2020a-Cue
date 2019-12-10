package com.technion.cue;

public class Business {
    public String business_name;
    public String bo_name;
    public String description;
    public String logo_path;

    // TODO: add additional properties later

    public Business() { }

    public Business(String name, String bo_name, String description, String logo_path) {
        this.business_name = name;
        this.bo_name = bo_name;
        this.description = description;
        this.logo_path = logo_path;
    }

}
