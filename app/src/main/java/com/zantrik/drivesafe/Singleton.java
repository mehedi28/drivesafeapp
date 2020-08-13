package com.zantrik.drivesafe;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Singleton {

    static Singleton _instance;

    Context context;
    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedPrefEditor;

    public static Singleton instance(Context context) {
        if (_instance == null) {
            _instance = new Singleton();
            _instance.configSharedPrefs(context);
        }
        return _instance;
    }

    public static Singleton instance() {
        return _instance;
    }

    public void configSharedPrefs(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences("sharedPrefs", Activity.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
    }

    public void storeValueString(String key, String value) {
        sharedPrefEditor.putString(key, value);
        sharedPrefEditor.commit();
    }

    public String fetchValueString(String key) {
        return sharedPref.getString(key, null);
    }
}

