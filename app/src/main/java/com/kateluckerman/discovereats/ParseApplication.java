package com.kateluckerman.discovereats;

import android.app.Application;

import com.kateluckerman.discovereats.models.Business;
import com.kateluckerman.discovereats.models.Category;
import com.kateluckerman.discovereats.models.User;
import com.parse.Parse;
import com.parse.ParseObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

// reference : https://guides.codepath.org/android/Building-Data-driven-Apps-with-Parse#setup

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Business.class);
        ParseObject.registerSubclass(Category.class);

        // Use for troubleshooting -- remove this line for production
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Use for monitoring Parse OkHttp traffic
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("discover-eats") // should correspond to APP_ID env variable
                .clientKey("keytoeverything")  // set explicitly unless clientKey is explicitly configured on Parse server
                .server("https://discover-eats.herokuapp.com/parse/").build());

    }
}

