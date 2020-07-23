package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
                fusedLocationClient.getLastLocation().addOnSuccessListener(FilterActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.i(TAG, "Location: " + location.getLongitude() + " " + location.getLatitude());
                        currentLocation = location;
                    }
                });
            }
        });

        binding.btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        if(EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }


}