package com.android.ffmpeg;

import android.os.Looper;

/**
 * Created by sachin on 2/6/2015.
 */
public class ThreadPreconditions {

    public static void checkOnMainThread() {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                throw new IllegalStateException("should be called from the Main Thread");
            }
        }
    }
}
