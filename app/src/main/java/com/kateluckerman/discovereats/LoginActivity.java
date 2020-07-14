package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kateluckerman.discovereats.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private boolean createAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // TODO: If user is already logged in, go to main activity

        final TextView tvCreateAcc = binding.tvCreateAcc;
        final TextView tvLogin = binding.tvLogin;

        // when "Log In" is clicked, it becomes black, "Create Account" becomes gray, and confirm goes away
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount = false;
                tvLogin.setTextColor(Color.BLACK);
                tvCreateAcc.setTextColor(getResources().getColor(R.color.unselected_text));
                binding.etConfirmPass.setVisibility(View.GONE);
            }
        });

        // when "Create Account" is clicked, it becomes black, "Log In" becomes gray, and confirm shows up
        tvCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount = true;
                tvCreateAcc.setTextColor(Color.BLACK);
                tvLogin.setTextColor(getResources().getColor(R.color.unselected_text));
                binding.etConfirmPass.setVisibility(View.VISIBLE);
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Logic for create account/login
//                if (createAccount) {
//
//                } else {
//
//                }
                goMainActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}