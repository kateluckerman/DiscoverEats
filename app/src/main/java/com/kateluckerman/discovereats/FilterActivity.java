package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kateluckerman.discovereats.databinding.ActivityFilterBinding;

import org.parceler.Parcels;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class FilterActivity extends AppCompatActivity {

    public final String TAG = "FilterActivity";

    private ActivityFilterBinding binding;
    private FusedLocationProviderClient fusedLocationClient;

    private final int REQUEST_LOCATION_PERMISSION = 1;

    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        binding.ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        binding.btnUseLocation.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                binding.etLocation.setText("");
                requestLocationPermission();

                //TODO: Location requests are currently not working on physical device
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
                        }
                        else {
                            currentLocation = location;
                            Log.i(TAG, "Location: " + currentLocation.getLongitude() + " " + currentLocation.getLatitude());
                        }
                    }
                });
            }
        });

        binding.btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                fusedLocationClient.removeLocationUpdates(new LocationCallback());
                String locationString = binding.etLocation.getText().toString();
                // pass both of those things back to the other activity
                Intent intent = new Intent();
                intent.putExtra("locationString", locationString);
                intent.putExtra("currentLocation", Parcels.wrap(currentLocation));
                setResult(RESULT_OK, intent);
                finish();

            }
        });
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
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }


}