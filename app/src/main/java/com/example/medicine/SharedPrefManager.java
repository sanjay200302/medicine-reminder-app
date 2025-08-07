package com.example.medicine;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    // ... existing code ...
    /**
     * Clears all stored preferences (logout helper)
     */
    public void clear() {
        prefs.edit().clear().apply();
    }

    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PROFILE_IMAGE_URI = "profile_image_uri";
    private SharedPreferences prefs;

    public SharedPrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(String username, String password, String email) {
        prefs.edit().putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public boolean validateUser(String username, String password, String email) {
        return username.equals(prefs.getString(KEY_USERNAME, "")) &&
               password.equals(prefs.getString(KEY_PASSWORD, "")) &&
               email.equals(prefs.getString(KEY_EMAIL, ""));
    }

    public void saveEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
    }

    public void saveProfileImageUri(String uri) {
        prefs.edit().putString(KEY_PROFILE_IMAGE_URI, uri).apply();
    }

    public String getProfileImageUri() {
        return prefs.getString(KEY_PROFILE_IMAGE_URI, "");
    }

    // Returns true if a user is logged in (username is not empty)
    public boolean isLoggedIn() {
        return !getUsername().isEmpty();
    }
}
