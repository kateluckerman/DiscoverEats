package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.kateluckerman.discovereats.databinding.ActivityProfileBinding;
import com.kateluckerman.discovereats.models.Business;
import com.kateluckerman.discovereats.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = "ProfileFragment";

    private ListAdapter adapter;
    private List<Business> allBusinesses;
    private User user;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        binding.ivSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, SwipeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        user = new User(ParseUser.getCurrentUser());

        // set name in view if user has one, otherwise switch out TextView with EditText to add a name
        if (ParseUser.getCurrentUser().getString(User.KEY_NAME) != null) {
            binding.tvName.setText(user.getName());
        } else {
            binding.tvName.setVisibility(View.GONE);
            binding.llEditName.setVisibility(View.VISIBLE);

            binding.btnSaveName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = binding.etName.getText().toString();
                    user.setName(name);
                    binding.tvName.setText(name);
                    binding.llEditName.setVisibility(View.GONE);
                    binding.tvName.setVisibility(View.VISIBLE);
                }
            });
        }

        // set username in view and initialize business array
        binding.tvUsername.setText(user.getUsername());
        allBusinesses = new ArrayList<>();

        // on "Log Out" click, loads login screen and finished current activity
        binding.tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // set up RecyclerView with adapter, layout manager, and empty business list
        adapter = new ListAdapter(this, allBusinesses);
        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(this));

        // get the user's saved business list
        ParseRelation<Business> list = user.getUser().getRelation(User.KEY_LIST);
        list.getQuery().findInBackground(new FindCallback<Business>() {
            @Override
            public void done(List<Business> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error getting business list", e);
                } else {
                    Log.i(TAG, "success");
                    allBusinesses.addAll(objects);
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }
}