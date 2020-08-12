package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kateluckerman.discovereats.adapters.CategoryAdapter;
import com.kateluckerman.discovereats.databinding.ActivityFilterBinding;
import com.kateluckerman.discovereats.models.Category;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class FilterActivity extends AppCompatActivity {

    public final String TAG = "FilterActivity";

    private ActivityFilterBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    private final int REQUEST_LOCATION_PERMISSION = 1;

    private Location currentLocation;
    List<Integer> priceInterval;

    static Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        intent = new Intent();

        setCancel();
        setUseLocation();
        setUpPriceChoice();
        setApply();

        setUpCategorySearch();
    }

    private void setCancel() {
        binding.ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    private void setUseLocation() {
        binding.btnUseLocation.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                binding.etLocation.setText("");
                requestLocationPermission();

                fusedLocationClient.getLastLocation().addOnSuccessListener(FilterActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            // this isn't doing anything
                            LocationRequest locationRequest = LocationRequest.create();
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            locationRequest.setInterval(20 * 1000);

                            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
                            builder.addLocationRequest(locationRequest);
                            LocationSettingsRequest locationSettingsRequest = builder.build();

                            SettingsClient settingsClient = LocationServices.getSettingsClient(FilterActivity.this);
                            settingsClient.checkLocationSettings(locationSettingsRequest);

                            final LocationCallback locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    if (locationResult == null) {
                                        return;
                                    }
                                    currentLocation = locationResult.getLastLocation();
                                    Log.i(TAG, "Location: " + currentLocation.getLongitude() + " " + currentLocation.getLatitude());
                                }
                            };
                            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        } else {
                            currentLocation = location;
                            binding.btnUseLocation.setBackgroundColor(getColor(R.color.colorPrimary));
                            Log.i(TAG, "Location: " + currentLocation.getLongitude() + " " + currentLocation.getLatitude());
                        }
                    }
                });
            }
        });
    }

    private void setUpPriceChoice() {
        setSpinner(binding.priceFrom);
        setSpinner(binding.priceTo);

        // initialize price interval to correspond with default option
        priceInterval = new ArrayList<>();
        priceInterval.add(0);
        priceInterval.add(0);


        binding.priceFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                priceInterval.set(0, i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        binding.priceTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                priceInterval.set(1, i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void setSpinner(Spinner priceSpinner) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.price_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter(adapter);
    }

    private String getPrice() {
        String price = "";
        if (priceInterval.get(0) == 0 || priceInterval.get(1) == 0)
            return "";

        // price interval should be in format "1, 2"
        for (int i = priceInterval.get(0); i <= priceInterval.get(1); i++) {
            price += i;
            if (i != priceInterval.get(1)) {
                price += ", ";
            }
        }
        return price;
    }

    private void setApply() {
        binding.btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null || !binding.etLocation.getText().toString().isEmpty()) {
                    putIntentExtras(intent);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(FilterActivity.this, "You must select a location", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void putIntentExtras(Intent intent) {
        intent.putExtra("locationString", binding.etLocation.getText().toString());
        intent.putExtra("currentLocation", Parcels.wrap(currentLocation));
//        intent.putExtra("category", binding.etCategory.getText().toString());
        intent.putExtra("price", getPrice());
        intent.putExtra("distance", distanceInMeters());
    }

    private String distanceInMeters() {
        if (binding.etDistance.getText().toString().isEmpty()) {
            return "";
        }
        final double CONVERSION = 1609.34;
        double distance;
        try {
            distance = Double.parseDouble(binding.etDistance.getText().toString());
        } catch (NumberFormatException numberException) {
            Toast.makeText(this, "Distance does not contain a valid number, try again", Toast.LENGTH_LONG).show();
            return null;
        }
        distance = distance * CONVERSION;
        return String.valueOf((int) distance);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    private void setUpCategorySearch() {

        binding.categorySearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                binding.btnApply.setVisibility(View.GONE);
            }
        });

        final List<Category> categories = new ArrayList<>();
        final CategoryAdapter adapter = new CategoryAdapter(this, categories);
        binding.rvCategories.setAdapter(adapter);
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this));

        final ParseQuery<Category> categoryQuery = ParseQuery.getQuery(Category.CLASS_NAME);

        categoryQuery.findInBackground(new FindCallback<Category>() {
            @Override
            public void done(List<Category> objects, ParseException e) {
                categories.clear();
                categories.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });

        binding.categorySearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                categoryQuery.whereStartsWith("title", s);
                categoryQuery.findInBackground(new FindCallback<Category>() {
                    @Override
                    public void done(List<Category> objects, ParseException e) {
                        categories.clear();
                        categories.addAll(objects);
                        adapter.notifyDataSetChanged();
                    }
                });
                return true;
            }
        });
    }

    public static void onCategorySelected(Category category) {
        intent.putExtra("category", category.getAlias());
    }
}