package com.kateluckerman.discovereats.models;

import com.parse.ParseFile;
import com.parse.ParseUser;

public class User {

    public static final String KEY_NAME = "name";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PROFILE_IMAGE = "profileImage";

    public ParseUser user;

    public User(ParseUser user) {
        this.user = user;
    }

    public String getName() {
        return user.getString(KEY_NAME);
    }

    public void setName(String name) {
        user.put(KEY_NAME, name);
    }

    public String getUsername() {
        return user.getUsername();
    }

    public ParseFile getProfileImage() {
        return user.getParseFile(KEY_PROFILE_IMAGE);
    }
}
