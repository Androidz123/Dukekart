package com.dukeKart.android.database;

import android.app.Activity;

public final class AppSettings extends OSettings {
    public static final String PREFS_MAIN_FILE = "PREFS_MAIN_FILE";
    public static String status;

    public AppSettings(Activity mActivity) {
        super(mActivity);
    }
}