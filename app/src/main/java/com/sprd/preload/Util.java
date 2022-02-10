package com.sprd.preload;

import java.io.File;
import java.io.IOException;
import android.util.Log;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by elena.guo on 2016/2/5.
 */
public class Util {
    private static final String TAG = PreLoadService.TAG;
    public static  final String FLAG_PATTH = "/data/system/preload/";
    public static  final String MMS_FLAG_NAME = "MmsLoadFlag.bat";
    public static  final String CONTACT_FLAG_NAME = "ContactLoadFlag.bat";
    public static  final String MMS_FLAG_FILE_PATH = FLAG_PATTH + MMS_FLAG_NAME;
    public static  final String CONTACT_FLAG_FILE_PATH = FLAG_PATTH + CONTACT_FLAG_NAME;
    public static final String PRELOAD_APP_PACKAGE = "com.sprd.preload";
    public static boolean checkFlag(String path){
        boolean result = false;
        File file = new File(path);
        result = file.exists();
        Log.d(TAG, "checkFlag path:"+ path +";result:" + result);
        return result;
    }

    public static boolean setFlag(String path, boolean bSuccessFlag){
        boolean result = true;
        File file = new File(path);
        if (bSuccessFlag && !file.exists()){
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                Log.e(TAG, "[MMS] setFlag: mkdirs for " + parentFile.getPath() + " failed!");
                return false;
            }
            File folder = new File(FLAG_PATTH);

            try {
                result = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

	if (!bSuccessFlag && file.exists()){
            try {
                result = file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "setFlag path:"+ path+";bSuccessFlag:" + bSuccessFlag +";result:" + result);
        return result;
    }

    public static boolean checkFlag(Context context,  String key){
        boolean result = false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        result = sp.getBoolean(key, false);
        Log.d(TAG, "checkFlag key:"+ key +";result:" + result);
        return result;
    }


    public static boolean setFlag(Context context,  String key, boolean bSuccessFlag){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, bSuccessFlag);
        editor.apply();
        Log.d(TAG, "setFlag key:"+ key+";bSuccessFlag:" + bSuccessFlag);
        return true;
    }

}
