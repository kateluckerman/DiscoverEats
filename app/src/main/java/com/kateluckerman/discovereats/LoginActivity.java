package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kateluckerman.discovereats.databinding.ActivityLoginBinding;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private boolean createAccount = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // if user is already logged in skip login activity
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

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

                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();

                if (username.equals("") || password.equals("")) {
                    Toast.makeText(getApplicationContext(), "You must enter a username and password", Toast.LENGTH_LONG).show();
                    return;
                }

                if (createAccount) {
                    signUp(username, password);
                } else {
                    logIn(username, password);
                }
            }
        });
    }

    private void goMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void signUp(String username, String password) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with signup", e);
                    return;
                }
                goMainActivity();
            }
        });
    }

    private void logIn(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login", e);
                    return;
                }
                goMainActivity();
            }
        });
    }
}