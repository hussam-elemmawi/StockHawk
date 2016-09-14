package com.sam_chordas.android.stockhawk.rest;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by hussamelemmawi on 14/09/16.
 */
public class StockHawkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
