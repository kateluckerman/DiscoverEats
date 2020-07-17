package com.kateluckerman.discovereats;

import androidx.core.content.ContextCompat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestHeaders;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.kateluckerman.discovereats.databinding.FragmentSwipeBinding;
import com.kateluckerman.discovereats.models.Business;
import com.kateluckerman.discovereats.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class SwipeFragment extends Fragment {

    public static final String YELP_SEARCH_ENDPOINT = "https://api.yelp.com/v3/businesses/search";
    public static final String TAG = "SwipeFragment";

    private FragmentSwipeBinding binding;

    public static final int INITIAL_INDEX = -1;
    List<Business> businesses;

    // for each combination of search queries, the Yelp API can access up to 1000 results in increments of up to 50
    // for some searches, the total is lower than 1000, so we have to store the total to prevent swiping past the results
    private int resultTotal;

    // stores the index of user's last seen result out of total results of this search query
    private int searchIndex;

    // stores the index of user's last seen result out of results of the most recent API call
    private int APIresultIndex;

    // this dictates how many results the API gets with each call - max allowed is 50
    public static final int LIMIT = 5;

    ParseUser currUser;
    ParseObject currSearch;
    String location;

    public SwipeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and apply ViewBinding
        binding = FragmentSwipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currUser = ParseUser.getCurrentUser();
        businesses = new ArrayList<>();
        location = "Chesterfield"; // this is a placeholder location to be changed based on user later

        // check if the user already has a search with the same location
        currUser.getRelation(User.KEY_SEARCHES).getQuery().whereEqualTo("location", location).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error with Parse search check: " + e.getMessage(), e);
                    return;
                }
                // if no matching search is found
                if (objects.isEmpty()) {
                    // create a new search with these parameters
                    currSearch = new ParseObject("Search");
                    currSearch.put("location", location);
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
                } else {
                    // save the Parse search locally so the user's progress can be updated
                    currSearch = objects.get(0);
                    // initialize the user's last seen index to the previous result so they will be shown the one they saw last again
                    if (objects.get(0).getNumber("searchIndex") != null) {
                        searchIndex = (int) objects.get(0).getNumber("searchIndex") - 1;
                    } else {
                        searchIndex = INITIAL_INDEX;
                    }
                }
                getYelpResults();
            }
        });
    }

    public void getYelpResults() {
        // set up request with parameters and api key header
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("limit", LIMIT);
        params.put("location", location);
        params.put("categories", "restaurants");
        RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.put("Authorization", "Bearer " + getContext().getString(R.string.yelp_api_key));
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
                    binding.ivFork.setOnClickListener(new View.OnClickListener() {
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

    private void loadBusinessView(Business business) {
        binding.tvName.setText(business.getName());
        binding.tvCategories.setText(business.getCategoryString());
        binding.tvLocation.setText(business.getLocation());
        binding.tvPrice.setText(business.getPrice());
        final int resourceId = getContext().getResources().getIdentifier(business.getRatingDrawableName(true), "drawable",
                getContext().getPackageName());
        binding.ivRating.setImageDrawable(ContextCompat.getDrawable(getContext(), resourceId));
        Glide.with(getContext()).load(business.getPhotoURL()).into(binding.ivMainImage);
        // Create "view on Yelp" link and set the textview to respond to link clicks
        Spanned html = Html.fromHtml("<a href='" + business.getWebsite() + "'>" + getString(R.string.yelp_link) + "</a>");
        binding.tvWebsite.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvWebsite.setText(html);
    }

    private void saveAndLoadNext(final Business business) {
        business.setParseFields();
        business.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e("TAG", "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();

                } else {
                    Log.i(TAG, "Business save was successful!");
                    currUser.getRelation(User.KEY_LIST).add(business);
                    currUser.saveInBackground();
                    loadNextResult();
                }
            }
        });
    }

    private void loadNextResult() {
        // if the next result is at the end of all of the results, display a message and reset buttons to continue displaying message
        if (searchIndex + 1 >= resultTotal) {
            Toast.makeText(getContext(), "You have reached the end of results for this location", Toast.LENGTH_LONG).show();
            binding.ivFork.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(), "You have reached the end of results for this location", Toast.LENGTH_LONG).show();
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
        currSearch.put("searchIndex", searchIndex);
        currSearch.saveInBackground();
        // load the business
        loadBusinessView(businesses.get(APIresultIndex));
    }
}