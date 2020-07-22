package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.kateluckerman.discovereats.databinding.ActivityFilterBinding;

public class FilterActivity extends AppCompatActivity {

    private ActivityFilterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter);

        binding.ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        binding.btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationString = binding.etLocation.getText().toString();

                // check if there's anything in the current location

                // pass both of those things back to the other activity
                Intent intent = new Intent();
                intent.putExtra("locationString", locationString);
                // TODO: put the current location too
                setResult(RESULT_OK, intent);
                finish();

            }
        });


        binding.btnUseLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if you click current location, empty the text edit
                binding.etLocation.setText("");
                // process the current location
            }
        });

    }


}