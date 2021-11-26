package com.kaedea.junwind;

import android.os.Process;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

@Keep
public class JUnwind {
    private static final String TAG = "JUnwind";
    private static volatile boolean initialized;
    private static final Map<Integer, WeakReference<Thread>> sTidThreads = new HashMap<>();

    static {
        init();
    }

    public static synchronized boolean init() {
        if (!initialized) {
            try {
                System.loadLibrary("junwind");
                initialized = true;
            } catch (Exception e) {
                Log.e(TAG, "load JUnwind failed!", e);
                initialized = false;
            }
        }
        return initialized;
    }

    @Keep
    private static native String nativeJUnwind(int tid);

    @Keep
    private static String currJavaThrowable() {
        synchronized (sTidThreads) {
            sTidThreads.put(Process.myTid(), new WeakReference<>(Thread.currentThread()));
        }
        return null;
    }

    @Nullable
    public static String jUnwindCurr() {
        try {
            return getThreadStack(Thread.currentThread());
        } catch (Exception e) {
            Log.w(TAG, "try getCurrThreadStack failed", e);
        }
        return null;
    }

    @Nullable
    public static String jUnwind(int tid) {
        if (tid == Process.myTid()) {
            return jUnwindCurr();
        }
        Thread thread = getThreadByTid(tid);
        if (thread == null) {
            if (initialized) {
                try {
                    nativeJUnwind(tid);
                    thread = getThreadByTid(tid);
                } catch (Exception e) {
                    Log.w(TAG, "try nativeJUnwind failed", e);
                }
            }
        }
        return getThreadStack(thread);
    }

    private static Thread getThreadByTid(int tid) {
        WeakReference<Thread> ref;
        synchronized (sTidThreads) {
            ref = sTidThreads.get(tid);
        }
        if (ref != null) {
            return ref.get();
        }
        return null;
    }

    private static String getThreadStack(Thread thread) {
        if (thread != null) {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement traceElement : thread.getStackTrace()) {
                sb.append("\n").append("at ").append(traceElement);
            }
            return sb.length() > 0 ? "JUnwind" + sb.toString() : null;
        }
        return null;
    }
}