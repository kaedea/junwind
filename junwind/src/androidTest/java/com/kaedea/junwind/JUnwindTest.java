package com.kaedea.junwind;

import android.os.Process;
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.test.ext.junit.runners.AndroidJUnit4;


@RunWith(AndroidJUnit4.class)
public class JUnwindTest extends AbsAndroidTest {

    @Test
    public void testJUnwindCurr() {
        String stack = JUnwind.jUnwindCurr();
        Assert.assertFalse(TextUtils.isEmpty(stack));

        stack = JUnwind.jUnwind(Process.myTid());
        Assert.assertFalse(TextUtils.isEmpty(stack));

        final AtomicBoolean hasDone = new AtomicBoolean();
        new Thread(() -> {
            String stack1 = JUnwind.jUnwind(Process.myTid());
            Assert.assertFalse(TextUtils.isEmpty(stack1));
            hasDone.set(true);
        }).start();
        while (!hasDone.get()) {
        }
    }

    @Test
    public void testJUnwindWithTid() {
        String stack = JUnwind.jUnwind(Process.myPid()); // Main Thread
        Assert.assertFalse(TextUtils.isEmpty(stack));

        final AtomicInteger tid = new AtomicInteger(0);
        new Thread(() -> {
            tid.set(Process.myTid());
            try {
                Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException e) {
            }
        }).start();

        while (tid.get() == 0) {
        }

        stack = JUnwind.jUnwind(tid.get());
        Assert.assertFalse(TextUtils.isEmpty(stack));
    }
}