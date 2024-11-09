package com.sbi.yono.service.online.FrontServices;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "myData";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save a string value
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // Retrieve a string value
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // Save an integer value
    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    // Retrieve an integer value
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // Save a boolean value
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    // Retrieve a boolean value
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}

