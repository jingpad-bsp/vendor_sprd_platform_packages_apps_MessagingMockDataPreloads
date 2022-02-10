package com.sprd.preload;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import java.util.HashMap;

public class PreLoadService extends Service {
    public static final String TAG = "PreLoad";
    private static final boolean DEBUG = true;
    private ServiceHandler mHandler = null;
    private static final int EVENT_CHECK_FLAG_INTENT = 1;
    private static final int EVENT_BEGIN_LOAD = 2;
    private static  final String MMS = "Mms";
    private static  final String CONCTACT = "Contact";
    private static Context mContext;
    private HashMap<String,Boolean> mPreloadInfoList = new HashMap<String,Boolean>();
    private ThreadPoolManager poolManager;

    @Override
    public void onCreate() {
        mHandler = new ServiceHandler();
        mContext = this;
        poolManager = new ThreadPoolManager(ThreadPoolManager.TYPE_FIFO, 5);
        poolManager.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.i(TAG, "onStartCommand startId:" + startId);
            Message msg = mHandler.obtainMessage(EVENT_CHECK_FLAG_INTENT);
            msg.arg1 = startId;
            msg.obj = intent;
            mHandler.sendMessage(msg);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        poolManager.stop();
        super.onDestroy();
    }

    private final class ServiceHandler extends Handler {
        public void handleMessage(Message msg) {
            if (DEBUG) {
                Log.i(TAG, "Handling incoming message: " + msg.what);
            }

            switch (msg.what) {
                case EVENT_CHECK_FLAG_INTENT:
                    checkPreloadFlag();
                    break;
                case EVENT_BEGIN_LOAD:
                    beginLoad((String)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private void beginLoad(String obj) {
	 Log.d(TAG, "beginLoad obj:" + obj);
        switch (obj){
            case MMS:{
                MmsPrepare m = new MmsPrepare(mContext);
                poolManager.addAsyncTask(new ThreadPoolTaskPreload(m, m, null));
                break;
            }
            case CONCTACT:{
                break;
            }
            default:
                break;
        }
    }

    private void checkPreloadFlag() {
        boolean bPreLoadSms = Util.checkFlag(mContext, "preload_sms");
        boolean bPreLoadConctact = Util.checkFlag(mContext, "preload_conctact");;
        mPreloadInfoList.put(MMS, bPreLoadSms);
        mPreloadInfoList.put(CONCTACT, bPreLoadConctact);
        preLoad();
    }
    private void preLoad() {
        for(String sKey: mPreloadInfoList.keySet()){
            boolean bPreLoad = mPreloadInfoList.get(sKey);
            if(!bPreLoad){
                mHandler.sendMessage(mHandler.obtainMessage(EVENT_BEGIN_LOAD, (Object) sKey));
            }
        }
    }
}
