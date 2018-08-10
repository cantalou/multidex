package android.support.multidex.handler.exception.impl;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.handler.exception.AbstractHandler;

/**
 * MultiDex installation failed (/data/data/[package name]/code_cache/secondary-dexes/MultiDex.lock: open failed: EROFS (Read-only file system)).
 */
public class ReadOnlySystemHandle extends AbstractHandler {

    @Override
    public boolean match(String msg) {
        return msg.contains("open failed") && msg.contains("Read-only file system");
    }

    @Override
    public boolean handle(Context context) {
        MultiDex.useLock = false;
        MultiDex.log("change MultiDex.useLock to " + MultiDex.useLock);
        return true;
    }
}
