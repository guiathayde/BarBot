package com.barbot;

import android.content.Context;
import android.content.SharedPreferences;

public class SecurityPreferences {

    private SharedPreferences mSharedPreferences;

    public SecurityPreferences(Context context) {
        this.mSharedPreferences = context.getSharedPreferences("BarBot", Context.MODE_PRIVATE);
    }

    public void storeString(String key, String value) {
        this.mSharedPreferences.edit().putString(key, value).apply();
    }

    public String getStoredString(String key) {
        return this.mSharedPreferences.getString(key, "");
    }

    public void remove(String key) {
        this.mSharedPreferences.edit().remove(key).apply();
    }
}
