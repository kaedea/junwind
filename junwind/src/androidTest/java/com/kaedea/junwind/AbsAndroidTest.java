package com.kaedea.junwind;

import android.content.Context;

import org.junit.Before;

import androidx.test.platform.app.InstrumentationRegistry;

public class AbsAndroidTest {

    protected Context mContext;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
}