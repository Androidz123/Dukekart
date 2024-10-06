package com.dukeKart.android.database;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class OSettings {

    @SuppressLint("StaticFieldLeak")
    private static Activity mActivity;

    public OSettings(Activity mActivity) {
        OSettings.mActivity = mActivity;
    }

    public static void clearSharedPreference(String FileName) {
        SharedPreferences prefs = mActivity.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static void clearSharedPreference() {
        SharedPreferences prefs = mActivity.getSharedPreferences(AppSettings.PREFS_MAIN_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    public static void putString(String key, String value, String FileName) {
        SharedPreferences prefs = mActivity.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void putString(String key, String value) {
        SharedPreferences prefs = mActivity.getSharedPreferences(AppSettings.PREFS_MAIN_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(String key, String FileName) {
        SharedPreferences prefs = mActivity.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public static String getString(String key) {
        SharedPreferences prefs = mActivity.getSharedPreferences(AppSettings.PREFS_MAIN_FILE, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public static void putBoolean(String key, boolean value, String FileName) {
        SharedPreferences prefs = mActivity.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void putBoolean(String key, boolean value) {
        SharedPreferences prefs = mActivity.getSharedPreferences(AppSettings.PREFS_MAIN_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static Boolean getBoolean(String key, String FileName) {
        SharedPreferences prefs = mActivity.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, true);
    }

    public static Boolean getBoolean(String key) {
        SharedPreferences prefs = mActivity.getSharedPreferences(AppSettings.PREFS_MAIN_FILE, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, true);
    }


    public static void putInt(String key, int value, String FileName) {
        SharedPreferences prefs = mActivity.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }


    public static void putInt(String key, int value) {
        SharedPreferences prefs = mActivity.getSharedPreferences(AppSettings.PREFS_MAIN_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getInt(String key, String FileName) {
        SharedPreferences prefs = mActivity.getSharedPreferences(FileName, Context.MODE_PRIVATE);
        return prefs.getInt(key, 0);
    }


    public static int getInt(String key) {
        SharedPreferences prefs = mActivity.getSharedPreferences(AppSettings.PREFS_MAIN_FILE, Context.MODE_PRIVATE);
        return prefs.getInt(key, 0);
    }

}
