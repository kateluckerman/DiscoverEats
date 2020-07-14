package com.kateluckerman.discovereats.models;

import java.util.List;

public class Business {

    private String name;
    private List<String> categories;
    private String location;
    private String address;
    private int price;
    private double rating;
    private String photoURL;

    // Methods to implement:

//    public static Business fromJson(JSONObject jsonObject) throws JSONException

//    public static List<Business> fromJsonArray(JSONArray jsonArray) throws JSONException

//    public void saveToParse()

    public String getName() {
        return name;
    }

    public List<String> getCategories() {
        return categories;
    }

    public String getLocation() {
        return location;
    }

    public String getAddress() {
        return address;
    }

    public int getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public String getPhotoURL() {
        return photoURL;
    }
}
