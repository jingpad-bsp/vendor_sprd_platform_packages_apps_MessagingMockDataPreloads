package com.sprd.preload;

import android.content.Context;

/**
 * Task
 * Created by elena.guo on 2016/3/29.
 */
public abstract class ThreadPoolTask implements Runnable {

    protected IPrepare iPrepare;

    public ThreadPoolTask(IPrepare iPrepare) {
        this.iPrepare = iPrepare;
    }

    public abstract void run();

    public IPrepare getPrepare() {
        return this.iPrepare;
    }
}
