package com.cantalou.app;

import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexExtractor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivateKey;
import java.util.List;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.time)).setText(Application.time + "");

        findViewById(R.id.load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apkFile = "/data/data/com.cantalou.divider.app/files/4399GameCenter.294.jar";
                String apkOptFile = "/data/data/com.cantalou.divider.app/files/dexOpt";
                File dexDir = new File(apkOptFile);
                dexDir.mkdirs();
                try {

                    loadApplication(getBaseContext(), apkFile, apkOptFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadApplication(Context context, String apkFile, String apkOptFile) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, IOException, IllegalAccessException {

        CustomClassLoader classLoader = new CustomClassLoader(apkFile, apkOptFile, getClassLoader());

        Class clazz = classLoader.loadClass("com.m4399.gamecenter.plugin.main.PluginApplication");
        Log.d("MainActivity", clazz.toString() + ", " + clazz.hashCode());

        File sunDexDir = new File(apkOptFile + "/sub");
        sunDexDir.mkdirs();
        List<? extends File> files = MultiDexExtractor.load(context, new File(apkFile), sunDexDir, "1", false, null);
        MultiDex.installSecondaryDexes(classLoader, sunDexDir, files);

        clazz = classLoader.loadClass("tv.danmaku.ijk.media.player.IjkMediaPlayer");
        Log.d("MainActivity", clazz.toString());
    }

    private void deleteFil(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    deleteFil(subFile);
                }
            }
            file.delete();
        }
    }

    static class CustomClassLoader extends DexClassLoader {

        public CustomClassLoader(String dexPath, String optimizedDirectory, ClassLoader parent) {
            super(dexPath, optimizedDirectory, optimizedDirectory, parent);
        }

        @Override
        protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
            Exception e1;
            try {
                return findClass(className);
            } catch (Exception e) {
                e1 = e;
            }

            try {
                return super.loadClass(className, resolve);
            } catch (Exception e) {
                e1.printStackTrace();
                throw e;
            }
        }


        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }
}
