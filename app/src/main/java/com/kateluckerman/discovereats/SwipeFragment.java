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
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Headers;

public class SwipeFragment extends Fragment {

    public static final String YELP_SEARCH_ENDPOINT = "https://api.yelp.com/v3/businesses/search";
    public static final String TAG = "SwipeFragment";

    private FragmentSwipeBinding binding;

    List<Business> businesses;
    private int resultNumber;

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
        // TODO: Use user's location instead of placeholder
        params.put("location", "st louis");
        params.put("categories", "restaurants");
        RequestHeaders requestHeaders = new RequestHeaders();
        requestHeaders.put("Authorization", "Bearer " + getString(R.string.yelp_api_key));

        // send request
        client.get(YELP_SEARCH_ENDPOINT, requestHeaders, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(final int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    // convert result into global list of Business objects
                    JSONArray results = jsonObject.getJSONArray("businesses");
                    Log.i(TAG, "Success with Yelp network request: " + results.toString());
                    businesses = Business.fromJsonArray(results);
                    resultNumber = 0;
                    loadBusinessView(businesses.get(0));

                    // fork icon saves business and loads next
                    binding.ivFork.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveBusiness(businesses.get(resultNumber));
                            loadNextResult();
                        }
                    });

                    // x icon loads next
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
        final int resourceId = getResources().getIdentifier(business.getRatingDrawableName(true), "drawable",
                getContext().getPackageName());
        binding.ivRating.setImageDrawable(ContextCompat.getDrawable(getContext(), resourceId));
        Glide.with(getContext()).load(business.getPhotoURL()).into(binding.ivMainImage);
        // Create "view on Yelp" link and set the textview to respond to link clicks
        Spanned html = Html.fromHtml("<a href='" + business.getWebsite() + "'>" + getString(R.string.yelp_link) + "</a>");
        binding.tvWebsite.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvWebsite.setText(html);
    }

    private void saveBusiness(final Business business) {
        business.setParseFields();
        business.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e("TAG", "Error while saving", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();

                } else {
                    Log.i(TAG, "Business save was successful!");
                    // TODO: Better error handling - only load next business if saved successfully
                    ParseUser.getCurrentUser().getRelation(User.KEY_LIST).add(business);
                    ParseUser.getCurrentUser().saveInBackground();
                }
            }
        });
    }
    private void loadNextResult() {
        resultNumber ++;
        loadBusinessView(businesses.get(resultNumber));
    }
}