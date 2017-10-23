package com.cantalou.app;

import android.content.Context;
import android.support.multidex.MultiDex;

public class Application extends android.app.Application {

    public static long time;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        long start = System.currentTimeMillis();
        MultiDex.install(this ,MultiDex.MODE_SERIAL);
        time = System.currentTimeMillis() - start;
    }
}
