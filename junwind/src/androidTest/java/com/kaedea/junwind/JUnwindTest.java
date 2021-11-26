package com.kaedea.junwind;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class JUnwindTest {

    private Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void testJUnwindWithTid() {
        String stack = JUnwind.jUnwind(Process.myPid()); // Main Thread
        Assert.assertFalse(TextUtils.isEmpty(stack));
    }
}