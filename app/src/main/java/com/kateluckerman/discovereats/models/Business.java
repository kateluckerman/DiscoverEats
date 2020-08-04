package com.kateluckerman.discovereats.models;

import android.os.Bundle;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Business")
public class Business extends ParseObject {

    public String name;
    public List<String> categories;
    public String location;
    public String price;
    public double rating;
    public String photoURL;
    public String website;
    public String alias;
    public String id;
    public String address = "";
    public int reviewCount;
    public double latitude;
    public double longitude;
    public List<String> photoURLS;
    public List<String> tags;
    public boolean open_now;

    public boolean completed;

    public static final String KEY_NAME = "name";
    public static final String KEY_CATEGORIES = "categories";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PRICE = "price";
    public static final String KEY_RATING = "rating";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_IMAGE_URL = "imageURL";
    public static final String KEY_WEBSITE = "website";
    public static final String KEY_ALIAS = "alias";
    public static final String KEY_ID = "businessID";

    public Business() {
        completed = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAlias() {return alias; }

    public String getId() { return id; }

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
        if (jsonObject.has("price"))
            business.price = jsonObject.getString("price");
        business.rating = jsonObject.getDouble("rating");
        business.photoURL = jsonObject.getString("image_url");
        business.website = jsonObject.getString("url");
        business.alias = jsonObject.getString("alias");
        business.id = jsonObject.getString("id");
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

    public void fromDetailsJSON(JSONObject jsonObject) throws JSONException {
        this.reviewCount = jsonObject.getInt("review_count");
        JSONArray displayAddress = jsonObject.getJSONObject("location").getJSONArray("display_address");
        for (int i = 0; i < displayAddress.length(); i++) {
            this.address += displayAddress.getString(i) + " ";
        }
        this.latitude = jsonObject.getJSONObject("coordinates").getDouble("latitude");
        this.longitude = jsonObject.getJSONObject("coordinates").getDouble("longitude");
        // more photos
        JSONArray photos = jsonObject.getJSONArray("photos");
        this.photoURLS = new ArrayList<>();
        for (int i = 0; i < photos.length(); i++) {
            this.photoURLS.add(photos.getString(i));
        }
        this.open_now = jsonObject.getJSONArray("hours").getJSONObject(0).getBoolean("is_open_now");
        JSONArray transactions = jsonObject.getJSONArray("transactions");
        this.tags = new ArrayList<>();
        for (int i = 0; i < transactions.length(); i++) {
            this.tags.add(transactions.getString(i));
        }
    }

    public void setParseFields() {
        put(KEY_NAME, name);
        addAll(KEY_CATEGORIES, categories);
        put(KEY_LOCATION, location);
//        put(KEY_ADDRESS, address);
        put(KEY_PRICE, price);
        put(KEY_RATING, rating);
        put(KEY_IMAGE_URL, photoURL);
        put(KEY_WEBSITE, website);
        put(KEY_ALIAS, alias);
        put(KEY_ID, id);
    }

    public void getFromParse() {
        name = getString(KEY_NAME);
        categories = getList(KEY_CATEGORIES);
        location = getString(KEY_LOCATION);
        address = getString(KEY_ADDRESS);
        price = getString(KEY_PRICE);
        rating = getDouble(KEY_RATING);
        photoURL = getString(KEY_IMAGE_URL);
        website = getString(KEY_WEBSITE);
        alias = getString(KEY_ALIAS);
        id = getString(KEY_ID);
    }

    public static void setCompleted(List<Business> businesses) {
        for (int i = 0; i < businesses.size(); i++) {
            businesses.get(i).completed = true;
        }
    }

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
        drawableFileName += intRating / 2;
        if (intRating % 2 != 0) {
            drawableFileName += "_half";
        }
        if (vertical)
            drawableFileName += "_v";
        return drawableFileName;
    }

    // ParseObject implements Parcelable, so Parse recommends using onSave/onRestoreInstanceState
    // to hold instance information not stored in Parse
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("name", name);
        outState.putStringArrayList("categories", (ArrayList<String>) categories);
//        outState.putString("location", location);
        outState.putString("price", price);
        outState.putDouble("rating", rating);
//        outState.putString("photoURL", photoURL);
        outState.putString("website", website);
//        outState.putString("alias", alias);
        outState.putString("id", id);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        name = savedInstanceState.getString("name");
        categories = savedInstanceState.getStringArrayList("categories");
//        location = savedInstanceState.getString("location");
        price = savedInstanceState.getString("price");
        rating = savedInstanceState.getDouble("rating");
//        photoURL = savedInstanceState.getString("photoURL");
        website = savedInstanceState.getString("website", website);
//        alias = savedInstanceState.getString("alias", alias);
        id = savedInstanceState.getString("id");
    }
}
