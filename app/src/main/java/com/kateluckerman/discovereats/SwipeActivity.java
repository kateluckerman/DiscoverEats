package com.kateluckerman.discovereats;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestHeaders;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.kateluckerman.discovereats.databinding.ActivitySwipeBinding;
import com.kateluckerman.discovereats.models.Business;
import com.kateluckerman.discovereats.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class SwipeActivity extends AppCompatActivity {

    public static final String YELP_SEARCH_ENDPOINT = "https://api.yelp.com/v3/businesses/search";
    public static final String TAG = "SwipeFragment";
    public static final int FILTER_REQUEST_CODE = 1;

    private ActivitySwipeBinding binding;

    public static final int INITIAL_INDEX = -1;
    List<Business> businesses;

    ParseUser currUser;
    ParseObject currSearch;
    String location;

    // for each combination of search queries, the Yelp API can access up to 1000 results in increments of up to 50
    // for some searches, the total is lower than 1000, so we have to store the total to prevent swiping past the results
    private int resultTotal;
    // stores the index of user's last seen result out of total results of this search query
    private int searchIndex;
    // stores the index of user's last seen result out of results of the most recent API call
    private int APIresultIndex;
    // dictates how many results the API gets with each call - max allowed is 50
    public static final int LIMIT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_swipe);

        setSwipeOnCard();
        setProfileButton();
        setFilterButton();

        currUser = ParseUser.getCurrentUser();

        getResults();
    }

    private void getResults() {
        businesses = new ArrayList<>();
        if (location == null)
            location = "Chesterfield, MO"; // this is a placeholder location to be changed based on user later

        // check if the user already has a search with the same location
        currUser.getRelation(User.KEY_SEARCHES).getQuery().whereEqualTo(User.Search.KEY_LOCATION, location).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Parse search check: " + e.getMessage(), e);
                    return;
                }
                // if no matching search is found
                if (objects.isEmpty()) {
                    createNewSearch();
                } else {
                    resumeSearch(objects.get(0));
                }
                getYelpResults();
            }
        });
    }

    private void createNewSearch() {
        currSearch = new ParseObject(User.Search.CLASS_NAME);
        currSearch.put(User.Search.KEY_LOCATION, location);
        currSearch.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // add the new search to the current user
                currUser.getRelation(User.KEY_SEARCHES).add(currSearch);
                currUser.saveInBackground();
            }
        });
        // no results for this search have been seen yet, so set index to initial value
        searchIndex = INITIAL_INDEX;
    }

    private void resumeSearch(ParseObject object) {
        // save the Parse search locally so the user's progress can be updated
        currSearch = object;
        // initialize the user's last seen index to the previous result so they will be shown the one they saw last again
        Number storedIndex = currSearch.getNumber(User.Search.KEY_SEARCH_INDEX);
        if (storedIndex != null) {
            searchIndex = (int) storedIndex - 1;
        } else {
            searchIndex = INITIAL_INDEX;
        }
    }

    public void getYelpResults() {
        // set up request with parameters and api key header
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("limit", LIMIT);
        params.put("location", location);
        params.put("categories", "restaurants");
        RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.put("Authorization", "Bearer " + getString(R.string.yelp_api_key));
        // tell API to only load results after the index of the last one seen
        params.put("offset", searchIndex + 1);

        // send request
        client.get(YELP_SEARCH_ENDPOINT, requestHeaders, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(final int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    // reset progress through API results
                    APIresultIndex = INITIAL_INDEX;
                    // store the number of accessible results for this search query
                    resultTotal = jsonObject.getInt("total");
                    if (resultTotal > 1000) {
                        resultTotal = 1000;
                    }
                    // add result into global list of Business objects
                    JSONArray results = jsonObject.getJSONArray("businesses");
                    Log.i(TAG, "Success with Yelp network request: " + jsonObject.toString());
                    businesses.clear();
                    businesses.addAll(Business.fromJsonArray(results));

                    // load the first result into view
                    loadNextResult();

                    // set fork icon click to save business and load next
                    binding.ivHeart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveAndLoadNext(businesses.get(APIresultIndex));
                        }
                    });

                    // set x icon click to load next
                    binding.ivX.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            loadNextResult();
                        }
                    });
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

    // chose to suppress accessibility warning because buttons can also be used instead of swiping
    @SuppressLint("ClickableViewAccessibility")
    private void setSwipeOnCard() {
        final GestureDetector gestureDetector = setSwipeDetector();

        binding.card.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private GestureDetector setSwipeDetector() {
        final GestureDetector gesture = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {

                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            // moved too much in Y direction
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                return false;
                            // swipe right to left
                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                loadNextResult();
                            }
                            // swipe left to right
                            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                saveAndLoadNext(businesses.get(APIresultIndex));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });
        return gesture;
    }

    private void setProfileButton() {
        binding.ivProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SwipeActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setFilterButton() {
        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SwipeActivity.this, FilterActivity.class);
                startActivityForResult(intent, FILTER_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILTER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String locationString = data.getStringExtra("locationString");
                if (!locationString.isEmpty()) {
                    location = locationString;
                    Log.i(TAG, "filter result" + location);
                    getResults();
                }
            }
        }
    }

    private void loadBusinessView(Business business) {
        binding.tvName.setText(business.getName());
        binding.tvCategories.setText(business.getCategoryString());
        binding.tvLocation.setText(business.getLocation());
        binding.tvPrice.setText(business.getPrice());
        final int resourceId = getResources().getIdentifier(business.getRatingDrawableName(true), "drawable",
                getPackageName());
        binding.ivRating.setImageDrawable(ContextCompat.getDrawable(this, resourceId));
        Glide.with(this).load(business.getPhotoURL()).into(binding.ivMainImage);
//        // Create "view on Yelp" link and set the textview to respond to link clicks
//        Spanned html = Html.fromHtml("<a href='" + business.getWebsite() + "'>" + getString(R.string.yelp_link) + "</a>");
//        binding.tvWebsite.setMovementMethod(LinkMovementMethod.getInstance());
//        binding.tvWebsite.setText(html);
    }

    private void saveAndLoadNext(final Business business) {
        ParseQuery<Business> query = ParseQuery.getQuery("Business");
        query.whereEqualTo(Business.KEY_ALIAS, business.getAlias());
        query.findInBackground(new FindCallback<Business>() {
            @Override
            public void done(List<Business> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage(), e);
                    return;
                }
                // if the business is not already stored in Parse, save it and load next
                if (objects.isEmpty()) {
                    business.setParseFields();
                    business.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e("TAG", "Error while saving", e);
                                Toast.makeText(SwipeActivity.this, "Error while saving!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            currUser.getRelation(User.KEY_LIST).add(business);
                            currUser.saveInBackground();
                            loadNextResult();
                        }
                    });
                } // otherwise, save the existing business to the user
                else {
                    currUser.getRelation(User.KEY_LIST).add(objects.get(0));
                    currUser.saveInBackground();
                    loadNextResult();
                }
            }
        });
    }

    private void loadNextResult() {
        // if the next result is at the end of all of the results, display a message and reset buttons to continue displaying message
        if (searchIndex + 1 >= resultTotal) {
            Toast.makeText(this, "You have reached the end of results for this location", Toast.LENGTH_LONG).show();
            binding.ivHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(SwipeActivity.this, "You have reached the end of results for this location", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        // if we've reached the last index of the business list, load more results
        if (APIresultIndex >= businesses.size() - 1) {
            getYelpResults();
            return;
        }
        // if no issues encountered:
        // increase progress through results
        APIresultIndex++;
        searchIndex++;
        // save the progress through this search
        currSearch.put(User.Search.KEY_SEARCH_INDEX, searchIndex);
        currSearch.saveInBackground();

        // check if user has already saved the next result in their list
        ParseQuery<ParseObject> listQuery = currUser.getRelation("list").getQuery();
        listQuery.whereEqualTo(Business.KEY_ALIAS, businesses.get(APIresultIndex).getAlias());
        listQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> matches, ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage(), e);
                    return;
                }
                if (matches.isEmpty()) {
                    loadBusinessView(businesses.get(APIresultIndex));
                } else {
                    loadNextResult();
                }
            }
        });
    }
}