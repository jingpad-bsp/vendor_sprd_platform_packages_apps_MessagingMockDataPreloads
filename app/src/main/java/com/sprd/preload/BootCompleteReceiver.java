package com.sprd.preload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = PreLoadService.TAG;

    private static final boolean DEBUG = true;
    public BootCompleteReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Log.d(TAG, "PreLoad Received BOOT_COMPLETED Intent: " + intent);
                intent.setClass(context, PreLoadService.class);
                context.startService(intent);
        }
    }
}
