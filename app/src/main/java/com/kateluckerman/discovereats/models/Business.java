package com.kateluckerman.discovereats.models;

import com.kateluckerman.discovereats.R;
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
    private String price;
    private double rating;
    private String photoURL;
    private String website;
    private String objectID;

    // Methods to implement:

    public static Business fromJson(JSONObject jsonObject) throws JSONException {
        Business business = new Business();
        business.name = jsonObject.getString("name");
        business.categories = new ArrayList<>();
        JSONArray categoryArray = jsonObject.getJSONArray("categories");
        for (int i = 0; i < categoryArray.length(); i++) {
            business.categories.add(categoryArray.getJSONObject(i).getString("title"));
        }
        business.location = jsonObject.getJSONObject("location").getString("city");
        business.address = jsonObject.getJSONObject("location").getString("address1");
        business.price = jsonObject.getString("price");
        business.rating = jsonObject.getDouble("rating");
        business.photoURL = jsonObject.getString("image_url");
        business.website = jsonObject.getString("url");
        return business;
    }

    public static List<Business> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Business> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
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

    public String getPrice() {
        return price;
    }

    public double getRating() {
        return rating;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getWebsite() { return website; }

    public String getCategoryString() {
        // convert list of categories to comma separated string
        String categoryString = "";
        for (int i = 0; i < categories.size(); i++) {
            if (i != 0) {
                categoryString += ", ";
            }
            categoryString += categories.get(i);
        }
        return categoryString;
    }

    public String getRatingDrawableName(boolean vertical) {
        int intRating = (int) (2 * rating);
        String drawableFileName = "stars_small_";
        switch (intRating) {
            case 10: {
                drawableFileName += "5";
                break;
            } case 9: {
                drawableFileName += "4_half";
                break;
            } case 8: {
                drawableFileName += "4";
                break;
            } case 7: {
                drawableFileName += "3_half";
                break;
            } case 6: {
                drawableFileName += "3";
                break;
            } case 5: {
                drawableFileName += "2_half";
                break;
            } case 4: {
                drawableFileName += "2";
                break;
            } case 3: {
                drawableFileName += "1_half";
                break;
            } case 2: {
                drawableFileName += "1";
                break;
            } default: {
                drawableFileName += "0";
                break;
            }
        }
        if (vertical)
            drawableFileName += "_v";
        return drawableFileName;
    }
}
