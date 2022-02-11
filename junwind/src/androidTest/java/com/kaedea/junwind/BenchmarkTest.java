package com.kaedea.junwind;

import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.hexhacking.xunwind.XUnwind;

@RunWith(AndroidJUnit4.class)
public class BenchmarkTest extends AbsAndroidTest {

    public static final int COUNT = 1000;


    /**
     * 0.002ms
     */
    @Test
    public void testLogging() {
        long bgnMillis = SystemClock.currentThreadTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            //noinspection ThrowableNotThrown
            Log.i("BenchmarkTest", "logging");
        }
        Assert.fail("Time Consumed: " + (SystemClock.currentThreadTimeMillis() - bgnMillis)/(COUNT * 1f) + "ms avg");
    }

    /**
     * 0.02ms
     */
    @Test
    public void testGetThreadStackByThrowable() {
        long bgnMillis = SystemClock.currentThreadTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            //noinspection ThrowableNotThrown
            StackTraceElement[] elements = new Throwable().getStackTrace();
        }
        Assert.fail("Time Consumed: " + (SystemClock.currentThreadTimeMillis() - bgnMillis)/(COUNT * 1f) + "ms avg");
    }

    /**
     * 0.02ms
     */
    @Test
    public void testGetThreadStackByThread() {
        long bgnMillis = SystemClock.currentThreadTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        }
        Assert.fail("Time Consumed: " + (SystemClock.currentThreadTimeMillis() - bgnMillis)/(COUNT * 1f) + "ms avg");
    }

    /**
     * 0.08ms
     */
    @Test
    public void testGetAllThreadStackByThread() {
        long bgnMillis = SystemClock.currentThreadTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        }
        Assert.fail("Time Consumed: " + (SystemClock.currentThreadTimeMillis() - bgnMillis)/(COUNT * 1f) + "ms avg");
    }

    /**
     * 0.02ms
     */
    @Test
    public void testGetThreadStackByTidWithJUnwind() {
        final AtomicInteger tid = new AtomicInteger(0);
        new Thread(() -> {
            tid.set(Process.myTid());
            while (true) {}
        }).start();

        while (tid.get() == 0) {}

        long bgnMillis = SystemClock.currentThreadTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            String stack = JUnwind.jUnwind(tid.get());
            Assert.assertFalse(TextUtils.isEmpty(stack));
        }
        Assert.fail("Time Consumed: " + (SystemClock.currentThreadTimeMillis() - bgnMillis)/(COUNT * 1f) + "ms avg");
    }

    /**
     * CFI: 20ms
     * FP: 0.005ms
     */
    @Test
    public void getGetThreadStackByCFI() {
        XUnwind.init();
        final AtomicInteger tid = new AtomicInteger(0);
        new Thread(() -> {
            tid.set(Process.myTid());
            while (true) {}
        }).start();

        while (tid.get() == 0) {}

        long bgnMillis = SystemClock.currentThreadTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            String stack = XUnwind.getLocalThread(tid.get());
            Assert.assertFalse(TextUtils.isEmpty(stack));
        }
        Assert.fail("Time Consumed: " + (SystemClock.currentThreadTimeMillis() - bgnMillis)/(COUNT * 1f) + "ms avg");
    }
}