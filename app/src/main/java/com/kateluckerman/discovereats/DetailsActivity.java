package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestHeaders;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.kateluckerman.discovereats.databinding.ActivityDetailsBinding;
import com.kateluckerman.discovereats.models.Business;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Headers;

public class DetailsActivity extends AppCompatActivity {

    public static final String TAG = "DetailsActivity";
    public static final String YELP_SEARCH_ENDPOINT = "https://api.yelp.com/v3/businesses/";

    Business business;
    double searchLatitude;
    double searchLongitude;

    private ActivityDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details);

        business = getIntent().getParcelableExtra("business");
        // TODO: if location is enabled, use location first so that it works when clicking from the list view
        searchLatitude = getIntent().getDoubleExtra("searchLatitude", 0);
        searchLongitude = getIntent().getDoubleExtra("searchLongitude", 0);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.put("Authorization", "Bearer " + getString(R.string.yelp_api_key));

        client.get(YELP_SEARCH_ENDPOINT + business.getId(), requestHeaders, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    business.fromDetailsJSON(jsonObject);
                    populateView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "Failure of Yelp network request: " + response, throwable);
            }
        });
    }

    private void populateView() {
        setBasicInfo();
        setAddress();
        setTags();
        setViewYelp();
        setImages();
        setRating();
        setHours();
        setDistance();
    }

    private void setBasicInfo() {
        binding.tvName.setText(business.getName());
        binding.tvCategories.setText(business.getCategoryString());
        binding.tvPrice.setText(business.getPrice());
    }

    private void setAddress() {
        binding.tvLocation.setText(business.getAddress());
        binding.tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationAddress = "geo:" + business.latitude + "," + business.longitude;
                Uri gmmIntentUri = Uri.parse(locationAddress);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }

    private void setTags() {
        List<String> tags = business.tags;
        if (tags.contains("pickup"))
            binding.tagPickup.setVisibility(View.VISIBLE);
        if (tags.contains("delivery"))
            binding.tagDelivery.setVisibility(View.VISIBLE);
        if (tags.contains("restaurant_reservation"))
            binding.tagReservations.setVisibility(View.VISIBLE);
    }

    private void setViewYelp() {
        binding.btnYelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(business.getWebsite()));
                startActivity(intent);
            }
        });
    }

    private void setImages() {
        // TODO: Error handling in the case that there are less than three photos
        Glide.with(this).load(business.photoURLS.get(0)).into(binding.ivImage1);
        Glide.with(this).load(business.photoURLS.get(1)).into(binding.ivImage2);
        Glide.with(this).load(business.photoURLS.get(2)).into(binding.ivImage3);
    }

    private void setRating() {
        final int ratingResourceId = getResources().getIdentifier(business.getRatingDrawableName(false), "drawable",
                getPackageName());
        binding.ivRating.setImageDrawable(ContextCompat.getDrawable(DetailsActivity.this, ratingResourceId));
        binding.tvOutOf.setText("out of " + business.reviewCount + " ratings");
    }

    private void setHours() {
        String isOpen;
        if (business.open_now)
            isOpen = "Open Now";
        else
            isOpen = "Closed Now";
        binding.tvHours.setText(isOpen);
    }

    private void setDistance() {
        float[] distance = new float[1];
        Location.distanceBetween(searchLatitude, searchLongitude, business.latitude, business.longitude, distance);
        String distanceString = String.format("%.2f", metersToMiles(distance[0])) + " mi";
        binding.tvDistance.setText(distanceString);
    }

    private double metersToMiles(double meters) {
        return meters*0.000621371192;
    }
}