package com.kateluckerman.discovereats.models;

import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Business {

    private String name;
    private List<String> categories;
    private String location;
    private String address;
    private int price;
    private double rating;
    private String photoURL;
    private String objectID;

    // Methods to implement:

    public static Business fromJson(JSONObject jsonObject) throws JSONException {
        return new Business();
    }

    public static List<Business> fromJsonArray(JSONArray jsonArray) throws JSONException {
        return new ArrayList<>();
    }

//    public void saveToParse()

//    public ParseObject getParseBusiness()

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
