package com.cantalou.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import java.io.File;

public class Application extends android.app.Application {

    public static long time;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
