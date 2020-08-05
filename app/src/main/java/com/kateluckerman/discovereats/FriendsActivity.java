package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.kateluckerman.discovereats.adapters.UserSearchAdapter;
import com.kateluckerman.discovereats.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    RecyclerView rvList;
    List<User> friends;
    UserSearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        rvList = findViewById(R.id.rvList);

        friends = new ArrayList<>();
        adapter = new UserSearchAdapter(this, friends, true);
        rvList.setAdapter(adapter);
        rvList.setLayoutManager(new LinearLayoutManager(this));

        ParseRelation<ParseUser> friendList = ParseUser.getCurrentUser().getRelation(User.KEY_FRIENDS);
        friendList.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                friends.addAll(User.getUserList(objects));
                adapter.notifyDataSetChanged();
            }
        });
    }
}