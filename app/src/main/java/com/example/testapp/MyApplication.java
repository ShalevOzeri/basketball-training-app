package com.example.testapp;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Locale setup removed - Android handles it automatically
        // based on system settings and AndroidManifest configuration
    }
}
