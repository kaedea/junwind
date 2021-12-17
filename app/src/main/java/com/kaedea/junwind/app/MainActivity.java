package com.kaedea.junwind.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kaedea.junwind.JUnwind;

public class MainActivity extends AppCompatActivity {

    static {
        JUnwind.init();
    }

    private int[] mInts;
    private int mTid = 0;
    private Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mTid = Process.myTid();
                while (true) {
                    if (!mThread.isInterrupted()) {
                        foo();
                    }
                }
            }
        }, "test-thread");
        mThread.start();
    }

    public void onGetCurrTid(View view) {
        if (JUnwind.init()) {
            final String stacks = JUnwind.jUnwindCurr();
            Log.i("JUnwind", "#jUnwindCurr = \n" + stacks);
            Toast.makeText(MainActivity.this, stacks, Toast.LENGTH_LONG).show();
        }
    }

    public void onGetOtherTid(View view) {
        if (JUnwind.init()) {
            final String stacks = JUnwind.jUnwind(mTid);
            Log.i("JUnwind", "#onGetCurrTid = \n" + stacks);
            Toast.makeText(MainActivity.this, stacks, Toast.LENGTH_LONG).show();
        }
    }


    void foo() {
        mInts = new int[]{};
        foo0();
    }
    void foo0() {
        mInts = new int[]{};
        foo1();
    }
    void foo1() {
        mInts = new int[]{};
        foo2();
    }
    void foo2() {
        mInts = new int[]{};
        foo3();
    }
    void foo3() {
        mInts = new int[]{};
        foo4();
    }
    void foo4() {
        mInts = new int[]{};
    }
}