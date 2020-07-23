package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.TextView;

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

        setSwipeButton();
        setSettingsMenu();

        user = new User(ParseUser.getCurrentUser());

        // set name and username in view and initialize business array
        setName(binding.tvName);
        binding.tvUsername.setText(user.getUsername());
        allBusinesses = new ArrayList<>();

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

    private void setSwipeButton() {
        binding.ivSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, SwipeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setName(TextView textView) {
        if (user.getName() != null)
            textView.setText(user.getName());
    }

    private void setSettingsMenu() {
        binding.ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(ProfileActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_settings, popup.getMenu());
                popup.setOnMenuItemClickListener(new SettingsMenuClickListener());
                popup.show();
            }
        });
    }

    public class SettingsMenuClickListener implements PopupMenu.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_edit_profile: {
                    turnOnEdit();
                    break;
                }
                case R.id.action_logout: {
                    user.getUser().logOut();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                }
            }
            return true;
        }

        private void turnOnEdit() {
            makeGone(binding.tvName);
            makeGone(binding.ivSettings);

            makeVisible(binding.etEditName);
            makeVisible(binding.btnDoneEdit);

            setName(binding.etEditName);

            binding.btnDoneEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.setName(binding.etEditName.getText().toString());
                    user.getUser().saveInBackground();

                    turnOffEdit();
                }
            });
        }

        private void turnOffEdit() {
            makeGone(binding.etEditName);
            makeGone(binding.btnDoneEdit);

            makeVisible(binding.ivSettings);
            makeVisible(binding.tvName);

            setName(binding.tvName);

            // make the keyboard go away
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(binding.etEditName.getWindowToken(), 0);
        }

        private void makeVisible(View view) {
            view.setVisibility(View.VISIBLE);
        }

        private void makeGone(View view) {
            view.setVisibility(View.GONE);
        }
    }


}