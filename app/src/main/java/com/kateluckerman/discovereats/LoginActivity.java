package com.kateluckerman.discovereats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private boolean createAccountSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        // if user is already logged in skip login activity
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        // click listeners to switch between log in/create account
        binding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLogInSelected();
            }
        });

        binding.tvCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCreateSelected();
            }
        });


        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();
                String passConfirm = binding.etConfirmPass.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "You must enter a username and password.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (createAccountSelected) {
                    // check that password and confirm password are the same
                    if (!passConfirm.equals(password)) {
                        Toast.makeText(LoginActivity.this, "Password confirmation does not match password.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    signUp(username, password);
                } else {
                    logIn(username, password);
                }
            }
        });
    }

    private void goMainActivity() {
        Intent intent = new Intent(LoginActivity.this, SwipeActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLogInSelected() {
        createAccountSelected  = false;
        setSelected(binding.tvLogin);
        setUnselected(binding.tvCreateAcc);
        binding.etConfirmPass.setVisibility(View.GONE);
        binding.btnLogin.setText(getString(R.string.login_button));
    }

    private void setCreateSelected() {
        createAccountSelected = true;
        setSelected(binding.tvCreateAcc);
        setUnselected(binding.tvLogin);
        binding.etConfirmPass.setVisibility(View.VISIBLE);
        binding.btnLogin.setText(getString(R.string.create_button));
    }

    private void setSelected(TextView textView) {
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(24);
        textView.setTypeface(null, Typeface.BOLD);
    }

    private void setUnselected(TextView textView) {
        textView.setTextColor(getResources().getColor(R.color.unselected_text, getTheme()));
        textView.setTextSize(20);
        textView.setTypeface(null, Typeface.NORMAL);
    }

    private void signUp(String username, String password) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    // display error messages and log errors
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Issue with sign-up", e);
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
                    // display error messages and log errors
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Issue with login", e);
                    return;
                }
                goMainActivity();
            }
        });
    }
}