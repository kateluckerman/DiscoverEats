package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.kateluckerman.discovereats.databinding.ActivityProfileBinding;
import com.kateluckerman.discovereats.models.Business;
import com.kateluckerman.discovereats.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    public static final String TAG = "ProfileFragment";

    private ListAdapter adapter;
    private List<Business> allBusinesses;
    private User user;
    private ActivityProfileBinding binding;

    private File profilePhotoFile;

    public static final String photoFileName = "photo.jpg";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        setSwipeButton();
        setSettingsMenu();

        user = new User(ParseUser.getCurrentUser());

        // set name and username in view and initialize business array
        setName(binding.tvName);
        setProfileImage();
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

    private void setProfileImage() {
        if (user.getProfileImage() != null) {
            makeVisible(binding.ivProfileImage);
            Glide.with(ProfileActivity.this).load(((ParseFile) user.getProfileImage()).getUrl()).transform(new CircleCrop()).into(binding.ivProfileImage);
        }
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
            makeVisible(binding.ivProfileImage);

            setName(binding.etEditName);

            binding.ivProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchCamera();
                }
            });

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

            // if a profile picture wasn't added, make the imageView go away
            if (user.getProfileImage() == null) {
                makeGone(binding.ivProfileImage);
            }

            // make the keyboard go away
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(binding.etEditName.getWindowToken(), 0);
        }

    }

    private void makeVisible(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void makeGone(View view) {
        view.setVisibility(View.GONE);
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        profilePhotoFile = getPhotoFileUri(photoFileName);

        Uri fileProvider = FileProvider.getUriForFile(this, "com.kateluckerman.fileprovider", profilePhotoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (profilePhotoFile != null) {
                    user.setProfileImage(profilePhotoFile);
                    user.getUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Issue saving profile image", e);
                            }
                            setProfileImage();
                        }
                    });
                }
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

}