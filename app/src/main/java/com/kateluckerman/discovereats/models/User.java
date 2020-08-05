package com.kateluckerman.discovereats.models;

import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class User {

    public static final String KEY_NAME = "name";
    public static final String KEY_PROFILE_IMAGE = "profileImage";
    public static final String KEY_LIST = "list";
    public static final String KEY_SEARCHES = "searches";
    public static final String KEY_COMPLETED = "completed";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_FRIENDS = "friends";

    public ParseUser user;

    // empty constructor for parceler purposes
    public User() {}

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

    public static List<User> getUserList(List<ParseUser> parseUserList) {
        List<User> userList = new ArrayList<>();
        for (ParseUser user : parseUserList) {
            userList.add(new User(user));
        }
        return userList;
    }

    public void markCompleted(Business business) {
        user.getRelation(KEY_COMPLETED).add(business);
        user.saveInBackground();
    }

    public void addFriend() {
        ParseUser.getCurrentUser().getRelation(KEY_FRIENDS).add(user);
        ParseUser.getCurrentUser().saveInBackground();
    }
}
