package com.smart_garden.garden;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
class User {

    public String fullname;
    public String email;
    public int isActive;
    public int device_count;


    public User(String fullname, String email, int isActive, int device_count) {
        this.fullname = fullname;
        this.email = email;
        this.isActive = isActive;
        this.device_count = device_count;
    }

}


