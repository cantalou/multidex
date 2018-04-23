package com.cantalou.app;

import android.content.Context;
import android.support.multidex.MultiDex;

public class Application extends android.app.Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base,MultiDex.MODE_SERIAL,"com.google.firebase.FirebaseOptions");
    }
}
