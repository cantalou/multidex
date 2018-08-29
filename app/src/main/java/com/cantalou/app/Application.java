package com.cantalou.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

public class Application extends android.app.Application {

    public static long time;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        long start = System.currentTimeMillis();
        try {
            base.getClassLoader()
                .loadClass("okio.Okio");
        } catch (ClassNotFoundException e) {
            Log.d("Application", "class okio.Okio not found before MultiDex.install");
        }
        MultiDex.install(this, MultiDex.MODE_SERIAL);
        time = System.currentTimeMillis() - start;

        try {
            base.getClassLoader()
                .loadClass("okio.Okio");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
