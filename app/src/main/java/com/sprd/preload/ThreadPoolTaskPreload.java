package com.sprd.preload;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.util.List;

/**
 *
 * Created by elena.guo on 2016/3/29.
 */
public class ThreadPoolTaskPreload  extends ThreadPoolTask {
    private static final String TAG = PreLoadService.TAG;
    private CallBack callBack;
    private  Object object;
    private List<Object> listObject;
    private int  iResult;
    private long  lResult;
    private String msg;

    public ThreadPoolTaskPreload(IPrepare iPrepare, CallBack callBack, Object object) {
        super(iPrepare);
        this.callBack = callBack;
        this.object = object;
    }
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
        iPrepare.begin(null);
        int result = iPrepare.process(null);
        this.iResult = iPrepare.end(result);
        if (callBack != null) {
            callBack.onUpdate(this.msg, this.iResult, this.lResult, this.object, this.listObject);
        }
    }

    public interface CallBack {
        public void onUpdate(String msg, int a, long b, Object c, List<Object> d);
    }
}