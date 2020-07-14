package com.kateluckerman.discovereats;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestHeaders;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.kateluckerman.discovereats.databinding.FragmentSwipeBinding;
import com.kateluckerman.discovereats.models.Business;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Headers;

/**
 * A simple {@link Fragment} subclass.
 */
public class SwipeFragment extends Fragment {

    public static final String YELP_SEARCH_ENDPOINT = "https://api.yelp.com/v3/businesses/search";
    public static final String TAG = "SwipeFragment";

    private FragmentSwipeBinding binding;

    List<Business> businesses;

    public SwipeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSwipeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getYelpResults();
    }

    public void getYelpResults() {
        // set up request with parameters and api key header
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        // TODO: Use user's location instead of placeholder and expand limit
        params.put("limit", 2);
        params.put("location", "st louis");
        RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.put("Authorization", "Bearer " + getString(R.string.yelp_api_key));

        // send request
        client.get(YELP_SEARCH_ENDPOINT, requestHeaders, params,  new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    // convert result into global list of Business objects
                    JSONArray results = jsonObject.getJSONArray("businesses");
                    businesses = Business.fromJsonArray(results);
                    loadBusinessView(businesses.get(0));
                    Log.i(TAG, "Success with Yelp network request: " + results.toString());
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
        // TODO: Set image, price, and rating
        binding.tvName.setText(business.getName());
        // convert list of categories to comma separated string
        String categoriesText = "";
        List<String> categories = business.getCategories();
        for (int i = 0; i < categories.size(); i++) {
            if (i != 0) {
                categoriesText += ", ";
            }
            categoriesText += categories.get(i);
        }
        binding.tvCategories.setText(categoriesText);
        binding.tvLocation.setText(business.getLocation());
        binding.tvWebsite.setText(business.getWebsite());
    }
}