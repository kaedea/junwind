package com.kaedea.junwind.app;

import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kaedea.junwind.JUnwind;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static {
        JUnwind.init();
    }

    private int mTid = 0;
    private Thread mThread;
    private final StringBuilder mDump = new StringBuilder();
    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = findViewById(R.id.tv_dump);

        mThread = new Thread(() -> {
            mTid = Process.myTid();
            while (true) {
                if (!mThread.isInterrupted()) {
                    foo();
                }
            }
        }, "test-thread");
        mThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThread.interrupt();
    }

    public void onGetCurrTid(View view) {
        if (JUnwind.init()) {
            final String stacks = JUnwind.jUnwindCurr();
            Log.i("JUnwind", "#jUnwindCurr = \n" + stacks);
            mDump.append(stacks).append("\n\n");
            mTv.setText(mDump.toString());
        }
    }

    public void onGetOtherTid(View view) {
        if (JUnwind.init()) {
            final String stacks = JUnwind.jUnwind(mTid);
            Log.i("JUnwind", "#onGetCurrTid = \n" + stacks);
            mDump.append(stacks).append("\n\n");
            mTv.setText(mDump.toString());
        }
    }

    void foo() {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        foo0();
    }

    void foo0() {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        foo1();
    }

    void foo1() {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        foo2();
    }

    void foo2() {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        foo3();
    }

    void foo3() {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        foo4();
    }

    void foo4() {
        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}