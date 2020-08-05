package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
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
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
    public final static int GALLERY_PICK_CODE = 1046;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        // if it's the current user's profile, allow them to edit
        if (user.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            setSettingsMenu();
            binding.ivEditList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    turnOnListEditing();
                }
            });
            setListEditingOptions();
        }

        setSwipeButton();
        // set name and username in view and initialize business array
        setName(binding.tvName);
        setProfileImage();
        binding.tvUsername.setText(user.getUsername());
        allBusinesses = new ArrayList<>();

        // set up RecyclerView with adapter, layout manager, and empty business list
        adapter = new ListAdapter(this, allBusinesses);
        binding.rvList.setAdapter(adapter);
        binding.rvList.setLayoutManager(new LinearLayoutManager(this));

        getList();
    }

    private void getList() {
        // user's saved business list
        final ParseRelation<Business> list = user.getUser().getRelation(User.KEY_LIST);
        // get user's completed business list
        final ParseQuery<ParseObject> completedQuery = user.getUser().getRelation(User.KEY_COMPLETED).getQuery();
        // first get businesses that aren't completed to be on top of list
        list.getQuery().whereDoesNotMatchKeyInQuery(Business.KEY_OBJECT_ID, Business.KEY_OBJECT_ID, completedQuery)
                .findInBackground(new FindCallback<Business>() {
                    @Override
                    public void done(List<Business> objects, ParseException e) {
                        allBusinesses.addAll(objects);
                        // then get completed businesses on bottom of list
                        list.getQuery().whereMatchesKeyInQuery(Business.KEY_OBJECT_ID, Business.KEY_OBJECT_ID, completedQuery)
                                .findInBackground(new FindCallback<Business>() {
                                    @Override
                                    public void done(List<Business> objects, ParseException e) {
                                        Business.setCompleted(objects);
                                        allBusinesses.addAll(objects);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }

    private void setSwipeButton() {
        binding.ivSwipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, SwipeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
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

    private void setListEditingOptions() {
        binding.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Business> selected = adapter.getSelected();
                // delete from view
                allBusinesses.removeAll(selected);
                adapter.notifyDataSetChanged();
                for (Business business : selected) {
                    user.getUser().getRelation(User.KEY_LIST).remove(business);
                }
                user.getUser().saveInBackground();
                turnOffListEditing();
            }
        });

        binding.tvCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Business> selected = adapter.getSelected();
                for (Business business : selected) {
                    user.markCompleted(business);
                }
                turnOffListEditing();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void turnOnListEditing() {
        makeVisible(binding.llEditItems);
        makeVisible(binding.tvCancelEdit);
        makeGone(binding.ivEditList);

        makeGone(binding.llButtons);
        makeGone(binding.llNames);
        makeGone(binding.ivProfileImage);
        makeGone(binding.ivSettings);

        adapter.turnOnListEditing();

        binding.tvCancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOffListEditing();
            }
        });
    }

    private void turnOffListEditing() {
        makeVisible(binding.ivEditList);
        makeGone(binding.tvCancelEdit);
        makeGone(binding.llEditItems);

        makeVisible(binding.llButtons);
        makeVisible(binding.llNames);
        makeVisible(binding.ivProfileImage);
        makeVisible(binding.ivSettings);

        adapter.turnOffListEditing();
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
                    turnOnProfileEdit();
                    break;
                }
                case R.id.action_logout: {
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    startActivity(intent);
                    ParseUser.logOut();
                    finish();
                    break;
                }
            }
            return true;
        }

        private void turnOnProfileEdit() {
            makeGone(binding.tvName);
            makeGone(binding.ivSettings);

            makeVisible(binding.etEditName);
            makeVisible(binding.btnDoneEdit);
            makeVisible(binding.ivProfileImage);

            setName(binding.etEditName);

            // option to edit profile image when clicked
            binding.ivProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(ProfileActivity.this, view);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_profile_image, popup.getMenu());
                    popup.setOnMenuItemClickListener(new ImageMenuClickListener());
                    popup.show();
                }
            });

            binding.btnDoneEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // save inputted name
                    user.setName(binding.etEditName.getText().toString());
                    user.getUser().saveInBackground();

                    turnOffProfileEdit();
                }
            });
        }


        private void turnOffProfileEdit() {
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

    public class ImageMenuClickListener implements PopupMenu.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_camera: {
                    launchCamera();
                    break;
                }
                case R.id.action_gallery: {
                    launchGallery(binding.getRoot());
                    break;
                }
            }
            return true;
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

        // make sure that the activity can be resolved in another app
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (profilePhotoFile == null) {
                Log.e(TAG, "Photo not found");
                return;
            }

            saveProfileImage(new ParseFile(profilePhotoFile));
        }

        if ((data != null) && requestCode == GALLERY_PICK_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);

            // convert to ParseFile
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageByte = byteArrayOutputStream.toByteArray();
            ParseFile parseFile = new ParseFile("image_file.png", imageByte);

            saveProfileImage(parseFile);
        }
    }

    private void saveProfileImage(ParseFile parseFile) {
        // save the photo to user
        user.setProfileImage(parseFile);
        user.getUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue saving profile image", e);
                    return;
                }
                // set the profile photo in view
                setProfileImage();
            }
        });
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos without requesting permissions
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    public void launchGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // make sure that the activity can be resolved in another app
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GALLERY_PICK_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

}