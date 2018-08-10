package android.support.multidex.handler.exception.impl;

import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.multidex.handler.exception.AbstractHandler;

import java.io.File;

/**
 * java.lang.RuntimeException: MultiDex installation failed (write failed: ENOSPC (No space left on device)).
 * at android.support.multidex.MultiDex.install(SourceFile:181)
 */
public class NoSpaceLeftOnDeviceHandler extends AbstractHandler {

    @Override
    public boolean match(String msg) {
        return msg.contains("No space left on device");
    }

    @Override
    public boolean handle(Context context) {
        try {
            delete(context.getCacheDir());
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                delete(context.getExternalCacheDir());
            }
        } catch (Throwable e) {
            MultiDex.log("delete cache file error ", e);
        }
        return true;
    }

    private void delete(File file) {

        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    delete(subFile);
                }
            }
        }else{
            MultiDex.log("delete file " + file + " " + file.delete());
        }
    }
}
