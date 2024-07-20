package com.investmango.hrconsole.service;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedUtils {
    private final Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SharedUtils(Context context) {
        this.context = context;
    }

    public SharedPreferences getSharedPreferencesContext() {
        SharedPreferences preferences = null;
        try {
            preferences = context.getSharedPreferences(SharedPreferencesConstants.APPLICATION_NAME, MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return preferences;
    }

    public void setLastUsedTimeInSharedPreference(String lastUsedTime) {
        preferences = getSharedPreferencesContext();
        editor = preferences.edit();
        editor.putString("lastUsedTime", lastUsedTime);
        editor.apply();
    }

    public String getStringPreference(String key) {
        preferences = getSharedPreferencesContext();
        return preferences.getString(key, null);
    }

    public String getUserAttendancePreference(String key) {
        preferences = getSharedPreferencesContext();
        return preferences.getString(key, "0");
    }

    public void setStringPreference(String key, String value) {
        preferences = getSharedPreferencesContext();
        editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void updateSharedPreferences(String authToken) {
        preferences = getSharedPreferencesContext();
        editor = preferences.edit();
        editor.putString(SharedPreferencesConstants.TOKEN, authToken);
        editor.apply();
    }
}
