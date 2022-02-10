package com.sprd.preload;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by elena.guo on 2016/2/4.
 */
public abstract class BasePrepare implements IPrepare{
    protected static Context mContext;
    protected static final int EVENT_BEGIN = 1;
    protected static final int EVENT_PROCESS = 2;
    protected static final int EVENT_END = 3;
    protected static final int EVENT_ERROR = 4;

    protected final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_BEGIN:
                    begin((String) msg.obj);
                    break;

                case EVENT_PROCESS:
                    process(msg.obj);
                    break;

                case EVENT_END:
                    end(msg.obj);
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }};
    BasePrepare(Context context){
        mContext = context;
    }
    @Override
    public int begin(Object obj) {
        return 0;
    }

    @Override
    public int process(Object obj) {
        return 0;
    }

    @Override
    public int end(Object obj) {
        return 0;
    }

   // public static Handler getHandler() {
    //    return mHandler;
    //}
}
