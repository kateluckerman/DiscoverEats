package com.kateluckerman.discovereats.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Business")
public class Business extends ParseObject {

    private String name;
    private List<String> categories;
    private String location;
    private String address;
    private String price;
    private double rating;
    private String photoURL;
    private String website;
    private String objectID;

    public static final String KEY_NAME = "name";
    public static final String KEY_CATEGORIES = "categories";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PRICE = "price";
    public static final String KEY_RATING = "rating";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_IMAGE_URL = "imageURL";

    public Business() {}

    // sets business fields based on Yelp request JSON object information
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
        if (jsonObject.has("price"))
            business.price = jsonObject.getString("price");
        business.rating = jsonObject.getDouble("rating");
        business.photoURL = jsonObject.getString("image_url");
        business.website = jsonObject.getString("url");
        return business;
    }

    // processes businesses JSON array from Yelp request
    public static List<Business> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Business> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    public void setParseFields() {
        put(KEY_NAME, name);
        addAll(KEY_CATEGORIES, categories);
        put(KEY_LOCATION, location);
        put(KEY_ADDRESS, address);
        put(KEY_PRICE, price);
        put(KEY_RATING, rating);
        put(KEY_IMAGE_URL, photoURL);
    }

    public void getFromParse() {
        name = getString(KEY_NAME);
        categories = getList(KEY_CATEGORIES);
        location = getString(KEY_LOCATION);
        address = getString(KEY_ADDRESS);
        price = getString(KEY_PRICE);
        rating = getDouble(KEY_RATING);
        photoURL = getString(KEY_IMAGE_URL);
    }

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

    // convert list of categories to comma separated string
    public String getCategoryString() {
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
