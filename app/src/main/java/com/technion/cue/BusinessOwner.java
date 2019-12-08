package com.technion.cue;

import android.text.Editable;

public class BusinessOwner {
    public String name;
    public String bo_name;
    public String description;
    public String logo_path;

    // TODO: add additional properties later

    public BusinessOwner() { }

    public BusinessOwner(String name, String bo_name, String description, String logo_path) {
        this.name = name;
        this.bo_name = bo_name;
        this.description = description;
        this.logo_path = logo_path;
    }

}
