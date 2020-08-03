package com.kateluckerman.discovereats;

import android.content.Context;
import android.location.Location;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestHeaders;
import com.codepath.asynchttpclient.RequestParams;

import java.util.Map;

public class YelpClient extends AsyncHttpClient {

    RequestParams params;
    RequestHeaders headers;
    Context context;
    String categories;
    String endpoint;

    public static final String BUSINESS_SEARCH_ENDPOINT = "https://api.yelp.com/v3/businesses/search";
    // for search, dictates how many results the API gets with each call - max allowed is 50
    public static final int SEARCH_LIMIT = 50;

    public YelpClient(Context context) {
        super();
        this.context = context;
        params = new RequestParams();
        headers = new RequestHeaders();
        headers.put("Authorization", "Bearer " + context.getString(R.string.yelp_api_key));
    }

    public void setBusinessSearch(Map<String, String> inputParams) {
        setParams(inputParams);
        params.put("limit", SEARCH_LIMIT);
        endpoint = BUSINESS_SEARCH_ENDPOINT;
    }

    public void setLocation(String location) {
        params.put("location", location);
        params.remove("longitude");
        params.remove("latitude");
    }

    public void setOffset(int offset) {
        params.put("offset", offset);
    }

    public void useCurrentLocation(Location location) {
        params.put("longitude", String.valueOf(location.getLongitude()));
        params.put("latitude", String.valueOf(location.getLatitude()));
        params.remove("location");
    }


    public void setParams(Map<String, String> inputParams) {
        if (inputParams == null) {
            return;
        }
        String category = inputParams.get("category");
        if (category == null || category.isEmpty())
            category = "restaurants";
        params.put("categories", category);
        String price = inputParams.get("price");
        if (price != null && !price.isEmpty()) {
            params.put("price", price);
        }
        String distance = inputParams.get("distance");
        if (distance != null && !distance.isEmpty()) {
            params.put("radius", distance);
        }
    }
}
