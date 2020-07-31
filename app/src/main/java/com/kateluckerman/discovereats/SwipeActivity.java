package com.kateluckerman.discovereats;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
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
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class SwipeActivity extends AppCompatActivity {

    public static final String TAG = "SwipeFragment";
    public static final int FILTER_REQUEST_CODE = 1;

    private ActivitySwipeBinding binding;

    public static final int INITIAL_INDEX = -1;
    List<Business> businesses;

    ParseUser currUser;
    ParseObject currSearch;

    // for each combination of search queries, the Yelp API can access up to 1000 results in increments of up to 50
    // for some searches, the total is lower than 1000, so we have to store the total to prevent swiping past the results
    private int resultTotal;
    // stores the index of user's last seen result out of total results of this search query
    private int searchIndex;
    // stores the index of user's last seen result out of results of the most recent API call
    private int APIresultIndex;

    YelpClient client;
    double searchLatitude;
    double searchLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_swipe);

        setProfileButton();
        setFilterButton();

        currUser = ParseUser.getCurrentUser();
        businesses = new ArrayList<>();

        client = new YelpClient(this);

        defaultSearch();
    }

    private void defaultSearch() {
        // initialize startup search to be user's most recent search or default
        ParseQuery<ParseObject> recentSearchQuery = currUser.getRelation(User.KEY_SEARCHES).getQuery().orderByDescending(ParseUser.KEY_UPDATED_AT);
        recentSearchQuery.setLimit(1).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                // user has no search queries
                if (objects == null || objects.isEmpty()) {
                    // create new search with default location
                    createNewSearch(toBasicString("Bay Area"));
                } else {
                    // resume user's most recent search
                    resumeSearch(objects.get(0));
                }
                getYelpResults();
            }
        });
    }

    private void setSearch(final String location) {
        // check if the user already has a search with the same location string
        currUser.getRelation(User.KEY_SEARCHES).getQuery().whereEqualTo(User.Search.KEY_LOCATION, location).setLimit(1).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Parse search check: " + e.getMessage(), e);
                    return;
                }
                // if no matching search is found
                if (objects.isEmpty()) {
                    createNewSearch(location);
                } else {
                    resumeSearch(objects.get(0));
                }
                getYelpResults();
            }
        });
    }

    private void setSearch(final String location, final String category) {
        currUser.getRelation(User.KEY_SEARCHES).getQuery().whereEqualTo(User.Search.KEY_LOCATION, location)
                .whereEqualTo(User.Search.KEY_CATEGORY, category).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Parse search check: " + e.getMessage(), e);
                    return;
                }
                if (objects == null || objects.isEmpty()) {
                    createNewSearch(location, category);
                } else {
                    resumeSearch(objects.get(0));
                }
            }
        });
    }

    private void createNewSearch(String location) {
        client.setLocation(location);
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

    private void createNewSearch(String location, String category) {
        client.setLocation(location);
        currSearch = new ParseObject(User.Search.CLASS_NAME);
        currSearch.put(User.Search.KEY_LOCATION, location);
        client.setCategory(category);
        currSearch.put(User.Search.KEY_CATEGORY, category);
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
        String location = currSearch.getString(User.Search.KEY_LOCATION);
        client.setLocation(toBasicString(location));
        String category = currSearch.getString(User.Search.KEY_CATEGORY);
        if (category != null && !category.isEmpty()) {
            client.setCategory(category);
        }
        // initialize the user's last seen index to the previous result so they will be shown the one they saw last again
        Number storedIndex = currSearch.getNumber(User.Search.KEY_SEARCH_INDEX);
        if (storedIndex != null) {
            searchIndex = (int) storedIndex - 1;
        } else {
            searchIndex = INITIAL_INDEX;
        }
    }

    public void getYelpResults() {

        client.setBusinessSearch();
        // tell API to only load results after the index of the last one seen
        client.setOffset(searchIndex + 1);

        // send request
        client.get(client.endpoint, client.headers, client.params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    // reset progress through API results
                    APIresultIndex = INITIAL_INDEX;
                    // store the number of accessible results for this search query
                    resultTotal = Math.min(jsonObject.getInt("total"), 1000);
                    // add results into global list of Business objects
                    JSONArray results = jsonObject.getJSONArray("businesses");
                    Log.i(TAG, "Success with Yelp network request: " + jsonObject.toString());
                    businesses.clear();
                    businesses.addAll(Business.fromJsonArray(results));

                    // store center of search region for calculating distance
                    searchLatitude = jsonObject.getJSONObject("region").getJSONObject("center").getDouble("latitude");
                    searchLongitude = jsonObject.getJSONObject("region").getJSONObject("center").getDouble("longitude");

                    // load the first result into view
                    loadNextResult();

                    // set buttons and swiping
                    configureOptions();
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

    private void configureOptions() {
        setSwipeOnCard();
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
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        Intent intent = new Intent(SwipeActivity.this, DetailsActivity.class);
                        Business business = businesses.get(APIresultIndex);
                        intent.putExtra("business", business);
                        intent.putExtra("searchLatitude", searchLatitude);
                        intent.putExtra("searchLongitude", searchLongitude);
                        startActivity(intent);
                        return true;
                    }

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
                Location currentLocation = Parcels.unwrap(data.getParcelableExtra("currentLocation"));
                String category = data.getStringExtra("category");
                if (currentLocation != null) {
                    client.useCurrentLocation(currentLocation);
                    Log.i(TAG, "current location is not null");
                    currSearch = null;
                    if (!category.isEmpty()) {
                        client.setCategory(category);
                    }
                    getYelpResults();
                    return;
                }
                String locationString = data.getStringExtra("locationString");
                if (!locationString.isEmpty()) {
                    if (category.isEmpty()) {
                        setSearch(toBasicString(locationString));
                    }
                    else {
                        setSearch(toBasicString(locationString), category);
                    }
                    return;
                }
            }
        }
    }

    private String toBasicString(String string) {
        String result = string.trim();
        result = result.toLowerCase();
        result = result.replaceAll(",", "");
        return result;
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
                                Log.e(TAG, "Error while saving", e);
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
        if (currSearch != null) {
            currSearch.put(User.Search.KEY_SEARCH_INDEX, searchIndex);
            currSearch.saveInBackground();
        }

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