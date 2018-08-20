package android.support.multidex.handler.exception.impl;

import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.multidex.handler.exception.AbstractHandler;

import java.io.File;
import java.io.FileFilter;

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
            delete(context.getCacheDir(), null);
            delete(context.getExternalCacheDir(), null);
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                delete(context.getExternalCacheDir(), null);
                delete(Environment.getExternalStorageDirectory(), new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        String fileName = file.getName();
                        return fileName.matches(".*(?i)(temp|cahce|log|360|tencent|4399|baidu|qq)(s?).*");
                    }
                });
            }
            MultiDex.useLock = false;
        } catch (Throwable e) {
            MultiDex.log("delete cache file error ", e);
        }
        return true;
    }

    private void delete(File file, FileFilter fileFilter) {

        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    if (fileFilter != null && fileFilter.accept(subFile)) {
                        delete(subFile, null);
                    } else {
                        delete(subFile, fileFilter);
                    }
                }
            }
        } else {
            if (fileFilter == null || fileFilter.accept(file)) {
                MultiDex.log("delete file " + file + " " + file.delete());
            }
        }
    }
}
