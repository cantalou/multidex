package com.cantalou.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = ((TextView) findViewById(R.id.time));


        findViewById(R.id.load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        new TestTask(true, MultiDex.MODE_SERIAL, "zip serial ").test();
                        new TestTask(true, MultiDex.MODE_EXTRACT_PARALLEL, "zip extract parallel ").test();
                        new TestTask(true, MultiDex.MODE_PARALLEL, "zip all parallel ").test();

                        new TestTask(false, MultiDex.MODE_SERIAL, "dex serial ").test();
                        new TestTask(false, MultiDex.MODE_EXTRACT_PARALLEL, "dex extract parallel ").test();
                        new TestTask(false, MultiDex.MODE_PARALLEL, "dex all parallel ").test();
                    }
                });
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.start();
            }
        });
    }

    private class TestTask {

        private boolean zipMode;

        private int mode;

        String label;

        public TestTask(boolean zipMode, int mode, String label) {
            this.zipMode = zipMode;
            this.mode = mode;
            this.label = label;
        }

        public void test() {
            long totalTime = 0;
            for (int i = 0; i < 10; i++) {
                if (MultiDex.mainDexDir != null) {
                    for (File file : MultiDex.mainDexDir.listFiles()) {
                        file.delete();
                    }
                }
                MultiDex.zipMode = zipMode;
                long start = System.currentTimeMillis();
                MultiDex.install(MainActivity.this, mode);
                long duration = System.currentTimeMillis() - start;
                Log.d("MainActivity", "\n " + label + " " + i + ", duration time:" + duration + "ms");
                totalTime += duration;
            }
            final long totalTime_ = totalTime / 10;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(textView.getText() + "\n" + label + ", time:" + totalTime_ + "ms");
                }
            });
        }
    }
}
