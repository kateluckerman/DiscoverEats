package com.kateluckerman.discovereats.models;

import com.parse.ParseFile;
import com.parse.ParseUser;

public class User {

    public static final String KEY_NAME = "name";
    public static final String KEY_PROFILE_IMAGE = "profileImage";
    public static final String KEY_LIST = "list";
    public static final String KEY_SEARCHES = "searches";
    public static final String KEY_COMPLETED = "completed";

    public ParseUser user;

    public User(ParseUser user) {
        this.user = user;
    }

    public ParseUser getUser() {
        return user;
    }

    public String getName() {
        return user.getString(KEY_NAME);
    }

    public void setName(String name) {
        user.put(KEY_NAME, name);
        user.saveInBackground();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public ParseFile getProfileImage() {
        return user.getParseFile(KEY_PROFILE_IMAGE);
    }

    public void setProfileImage(ParseFile photoFile) {
        user.put(KEY_PROFILE_IMAGE, photoFile);
    }

    public void markCompleted(Business business) {
        user.getRelation(KEY_COMPLETED).add(business);
        user.saveInBackground();
    }
}
