package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import com.kateluckerman.discovereats.adapters.UserSearchAdapter;
import com.kateluckerman.discovereats.databinding.ActivityUserSearchBinding;
import com.kateluckerman.discovereats.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class UserSearchActivity extends AppCompatActivity {

    public static final String TAG = "UserSearchActivity";

    ActivityUserSearchBinding binding;
    UserSearchAdapter adapter;
    List<User> users;

    ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_search);

        currentUser = ParseUser.getCurrentUser();

        users = new ArrayList<>();
        adapter = new UserSearchAdapter(this, users);
        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(this));

        ParseRelation<ParseUser> friends = ParseUser.getCurrentUser().getRelation(User.KEY_FRIENDS);
        final ParseQuery<ParseUser> friendsQuery = friends.getQuery();

        final ParseQuery<ParseUser> allUsers = ParseUser.getQuery().whereDoesNotMatchKeyInQuery(ParseUser.KEY_OBJECT_ID, ParseUser.KEY_OBJECT_ID, friendsQuery);
        allUsers.whereNotEqualTo(ParseUser.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        allUsers.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, e.getMessage(), e);
                }
                users.addAll(User.getUserList(objects));
                Log.i(TAG, "" + users.size());
                adapter.notifyDataSetChanged();
            }
        });


        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                ParseQuery<ParseUser> inUsername = ParseUser.getQuery().whereContains(User.KEY_USERNAME, s);
                ParseQuery<ParseUser> inName = ParseUser.getQuery().whereContains(User.KEY_NAME, s);
                
                List<ParseQuery<ParseUser>> userOrName = new ArrayList<>();
                userOrName.add(inUsername);
                userOrName.add(inName);

                ParseQuery.or(userOrName).whereDoesNotMatchKeyInQuery(ParseUser.KEY_OBJECT_ID, ParseUser.KEY_OBJECT_ID, friendsQuery)
                        .whereNotEqualTo(ParseUser.KEY_OBJECT_ID, ParseUser.getCurrentUser().getObjectId())
                        .findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        users.clear();
                        users.addAll(User.getUserList(objects));
                        adapter.notifyDataSetChanged();
                    }
                });
                return true;
            }
        });
    }
}