package com.cantalou.app;

import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Project Name: multidex<p>
 * File Name:    Application.java<p>
 * ClassName:    Application<p>
 * <p>
 * TODO.
 *
 * @author LinZhiWei
 * @date 2017年10月18日 10:39
 * <p>
 * Copyright (c) 2017年, 4399 Network CO.ltd. All Rights Reserved.
 */
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
