package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;

import com.kateluckerman.discovereats.databinding.ActivityUserSearchBinding;
import com.kateluckerman.discovereats.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
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

        ParseQuery<ParseUser> allUsers = ParseUser.getQuery();
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
    }
}