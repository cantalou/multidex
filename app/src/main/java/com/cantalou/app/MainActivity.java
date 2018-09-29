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
                File apkFile = new File("");
                String apkOptFile = getBaseContext().getFilesDir()
                                                    .getAbsolutePath();
                File dexDir = new File(apkOptFile);
                dexDir.mkdirs();
            }
        });
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
