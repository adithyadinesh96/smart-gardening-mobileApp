package com.garden.garden;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
class User {

    public String fullname;
    public String email;
    public int isActive;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String fullname, String email, int isActive) {
        this.fullname = fullname;
        this.email = email;
        this.isActive = isActive;
    }

}


