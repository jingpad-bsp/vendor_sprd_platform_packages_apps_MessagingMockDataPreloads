package com.sprd.preload;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.android.internal.telephony.SmsApplication;
import com.android.internal.util.XmlUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.app.AppOpsManager;
import android.content.ContentUris;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Addr;
import android.provider.Telephony.Mms.Part;
import android.provider.Telephony.Threads;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Random;

/**
 * Created by elena.guo on 2016/2/4.
 */
public class MmsPrepare /*extends BroadcastReceiver*/ implements IPrepare, ThreadPoolTaskPreload.CallBack  {
    private static final String TAG = PreLoadService.TAG;
    private static final String MMS_TAG = "MmsPrepare ";
    private static final String XML_NAME = "sms.xml";
    private static final String TAG_MMS_ITEMS = "sms_items";
    private static final String GROUP_SMS = "sms";
    private static final String ATTR_VERSION = "version";
    private static final String TAG_GROUP = "group";
    private static final String ATTR_GROUP_NAME = "name";
    private static final String TAG_PARMS = "parm";
    private static final String ASSET_FILE_VERSION = "1.0";

    private static final String IMAGE_NAME_1 = "image1";
    private static final String IMAGE_NAME_2 = "image2";
    private static final String SMIL_TEXT_IMAGE = "<smil><head><layout><root-layout width=\"320px\" height=\"480px\"/><region id=\"Text\" left=\"0\" top=\"320\" width=\"320px\" height=\"160px\" fit=\"meet\"/><region id=\"Image\" left=\"0\" top=\"0\" width=\"320px\" height=\"320px\" fit=\"meet\"/></layout></head><body><par dur=\"2000ms\"><text src=\"text_1.txt\" region=\"Text\"/><img src=\"%s\" region=\"Image\"/></par><par dur=\"2000ms\"><text src=\"text_2.txt\" region=\"Text\"/><img src=\"%s\" region=\"Image\"/></par></body></smil>";
    private static final String IMAGE_CID = "<img_cid>";

    private static final String AUDIO_NAME = "audio1";
    private static final String SMIL_TEXT_AUDIO = "<smil><head><layout><root-layout width=\"320px\" height=\"480px\"/><region id=\"Text\" left=\"0\" top=\"320\" width=\"320px\" height=\"160px\" fit=\"meet\"/><region id=\"Image\" left=\"0\" top=\"0\" width=\"320px\" height=\"320px\" fit=\"meet\"/></layout></head><body><par dur=\"21500ms\"><text src=\"text_1.txt\" region=\"Text\"/><audio src=\""
            + AUDIO_NAME + "\" dur=\"21500\" /></par></body></smil>";
    private static final String AUDIO_CID = "<300k>";

    private static final String VIDEO_NAME = "video1";
    private static final String SMIL_TEXT_VIDEO = "<smil><head><layout><root-layout width=\"320px\" height=\"480px\"/><region id=\"Text\" left=\"0\" top=\"320\" width=\"320px\" height=\"160px\" fit=\"meet\"/><region id=\"Image\" left=\"0\" top=\"0\" width=\"320px\" height=\"320px\" fit=\"meet\"/></layout></head><body><par dur=\"21500ms\"><text src=\"text_1.txt\" region=\"Text\"/><VIDEO src=\""
            + VIDEO_NAME + "\" dur=\"21500\" /></par></body></smil>";
    private static final String VIDEO_CID = "<300k>";

    private static final String FROM_NUM = "1008";

    private  Context mContext;
   // private static final String MMS_APP_PACKAGE = "com.android.mms";
    private static final String MMS_APP_PACKAGE = "com.android.messaging";
    public static final String PRELOAD_APP_PACKAGE = "com.sprd.preload";
    private List<SmsMessage> mSmsList = null;
    private boolean mResult = true;

    @Override
    public void onUpdate(String msg, int a, long b, Object c, List<Object> d) {
        //TODO:update UI
        Log.d(TAG, MMS_TAG + "onUpdate msg=" + msg +"; a ="+ a +"; b ="+ b+"; c ="+ c);
    }

    public MmsPrepare(Context context) {
        mContext = context;
    }

    public int begin(Object obj) {
       /*Bug 846425 start*/
        //SmsApplication.setDefaultApplication(PRELOAD_APP_PACKAGE, mContext);
        //SmsManager.getDefault().setAutoPersisting(true);
	 //ComponentName c =SmsApplication.getDefaultSmsApplication(mContext, true);
	 //Log.d(TAG, MMS_TAG + "begin:getDefaultSmsApplication=" + c.toString() );
        //SmsApplication.setDefaultApplication(PRELOAD_APP_PACKAGE, mContext);
        /*Bug 846425 end*/
        mSmsList = parseXml();
        return 0;
    }

      public int process(Object obj) {
         AddToDb();
         return 0;
    }

    public int end(Object obj) {
        Log.d(TAG, MMS_TAG+"end: obj =" + obj);
       /*Bug 846425 start*/
	 //SmsManager.getDefault().setAutoPersisting(false);
        //Log.d(TAG, MMS_TAG+"end: setAutoPersisting false");
        //SmsApplication.setDefaultApplication(MMS_APP_PACKAGE, mContext);
        /*Bug 846425 end*/
        Util.setFlag(mContext, "preload_sms", mResult);
        return 0;
    }

    private ArrayList<SmsMessage> parseXml(){
        //read XML
        try {
            XmlPullParser parser = mContext.getResources().getXml(R.xml.sms);
            int eventType = parser.getEventType();

            SmsMessage currentSmsMessage = null;
            ArrayList<SmsMessage> smsMessages = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        smsMessages = new ArrayList<SmsMessage>();
                        break;

                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        if (name.equalsIgnoreCase(GROUP_SMS)) {
                            currentSmsMessage = new SmsMessage();
                        } else if (currentSmsMessage != null) {
                            if (Telephony.Sms.SUBSCRIPTION_ID.equalsIgnoreCase(name)) {
                                currentSmsMessage.subId =  Integer.valueOf(parser.nextText());
                            } else if (Telephony.Sms.ADDRESS.equalsIgnoreCase(name)) {
                                currentSmsMessage.address = parser.nextText();
                            } else if (Telephony.Sms.BODY.equalsIgnoreCase(name)) {
                                currentSmsMessage.body = parser.nextText();
                            } else if (Telephony.Sms.SEEN.equalsIgnoreCase(name)) {
                                currentSmsMessage.seen =  Integer.valueOf(parser.nextText());
                            } else if (Telephony.Sms.DATE.equalsIgnoreCase(name)) {
                                currentSmsMessage.date = Long.valueOf(parser.nextText());
                            } else if (Telephony.Sms.READ.equalsIgnoreCase(name)) {
                                currentSmsMessage.read =  Integer.valueOf(parser.nextText());
                            }else if (Telephony.Sms.STATUS.equalsIgnoreCase(name)) {
                                currentSmsMessage.status =  Integer.valueOf(parser.nextText());
                            }else if (Telephony.Sms.ERROR_CODE.equalsIgnoreCase(name)) {
                                currentSmsMessage.errorCode =  Integer.valueOf(parser.nextText());
                            }else if (Telephony.Sms.TYPE.equalsIgnoreCase(name)) {
                                currentSmsMessage.type =  Integer.valueOf(parser.nextText());
                            }else if (Telephony.Sms.CREATOR.equalsIgnoreCase(name)) {
                                currentSmsMessage.creator =  parser.nextText();
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (parser.getName().equalsIgnoreCase(GROUP_SMS) && currentSmsMessage != null) {
                            smsMessages.add(currentSmsMessage);
                            currentSmsMessage = null;
                        }
                        break;
                }
                eventType = parser.next();
            }
            return smsMessages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void AddToDb(){
        if (null==mSmsList||mSmsList.isEmpty()){
            Log.d(TAG, MMS_TAG + "AddToDb mSmsList = null");
            mResult = false;
            return ;
        }
        for(int i=0;i<mSmsList.size();i++){
            Uri url =  insertSms(mSmsList.get(i));
            Log.d(TAG, MMS_TAG + "url:" + url);
        }
        for (int i = 0; i < 3; ++i) {
            insertMms(Telephony.BaseMmsColumns.MESSAGE_BOX_INBOX, AttachmentType.IMAGE, i);
        }
        insertMms(Telephony.BaseMmsColumns.MESSAGE_BOX_INBOX, AttachmentType.VIDEO, 3);
        insertMms(Telephony.BaseMmsColumns.MESSAGE_BOX_INBOX, AttachmentType.AUDIO, 4);
    }

    private Uri insertSms(SmsMessage s) {
	 Log.d(TAG, MMS_TAG+"insertSms begin");
        Uri insertUri = null;
        switch (s.type) {
            case  Telephony.BaseMmsColumns.MESSAGE_BOX_INBOX:
                insertUri = Telephony.Sms.Inbox.CONTENT_URI;
                break;
            case Telephony.BaseMmsColumns.MESSAGE_BOX_SENT:
                insertUri = Telephony.Sms.Sent.CONTENT_URI;
                break;
        }
        Log.d(TAG, MMS_TAG+"message type:" + s.type);
        if (insertUri == null) {
            //mResult = false;
            return null;
        }
        final ContentValues values = new ContentValues(6);
        values.put(Telephony.Sms.ADDRESS, s.address);
        values.put(Telephony.Sms.DATE, s.date);
        values.put(Telephony.Sms.SEEN, s.seen);
        values.put(Telephony.Sms.READ, s.read);
        values.put(Telephony.Sms.BODY, s.body);
        values.put(Telephony.Sms.TYPE, s.type);
        if (!TextUtils.isEmpty(s.creator)) {
            values.put(Telephony.Sms.CREATOR, s.creator);
        } else {
            values.put(Telephony.Sms.CREATOR, "com.android.messaging");
        }
        values.put(Telephony.Sms.SUBSCRIPTION_ID, s.subId);
        values.put(Telephony.Sms.ERROR_CODE, s.errorCode);
        try {
            Uri resultUri = mContext.getContentResolver().insert(insertUri, values);
            Log.d(TAG, MMS_TAG+"resultUri:" +resultUri);
            return resultUri;
        } catch (SQLiteException e) {
            mResult = false;
            Log.e(TAG, MMS_TAG +"insertSms: failed to persist imported text message", e);
        } finally {

        }
        return null;
    }

    private class SmsMessage {
        int subId;
        String address;
        String body;
        int seen;
        long date;//timestampMillis;
        int read;
        String creator;
        int status;
        int errorCode;
        int type;
        int threadId;
    }
    private void insertMms(int msgBoxType, AttachmentType type, int idx) {
        long threadId = Threads.getOrCreateThreadId(mContext, FROM_NUM + idx);
        Log.d(TAG, "insertMms threadId = " + threadId);

        String name_1 = null;
        String name_2 = null;
        String smil_text = null;
        ContentValues cv_part_1 = null;
        ContentValues cv_part_2 = null;

        switch (type) {
            case IMAGE:
                name_1 = IMAGE_NAME_1;
                name_2 = IMAGE_NAME_2;
                smil_text = String.format(SMIL_TEXT_IMAGE, name_1, name_2);
                cv_part_1 = createPartRecord(0, "image/jpg", name_1, IMAGE_CID, name_1, null, null);
                cv_part_2 = createPartRecord(0, "image/jpg", name_2, IMAGE_CID.replace("cid", "cid_2"), name_2, null, null);
                break;
            case AUDIO:
                name_1 = AUDIO_NAME;
                smil_text = String.format(SMIL_TEXT_AUDIO, name_1);
                cv_part_1 = createPartRecord(0, "audio/amr", AUDIO_NAME, AUDIO_CID, AUDIO_NAME, null, null);
                break;
            case VIDEO:
                name_1 = VIDEO_NAME;
                smil_text = String.format(SMIL_TEXT_VIDEO, name_1);
                cv_part_1 = createPartRecord(0, "video/mp4", VIDEO_NAME, VIDEO_CID, VIDEO_NAME, null, null);
                break;
        }

        // make MMS record
        ContentValues cvMain = new ContentValues();
        cvMain.put(Mms.THREAD_ID, threadId);

        cvMain.put(Mms.MESSAGE_BOX, msgBoxType);
        cvMain.put(Mms.READ, 1);
        cvMain.put(Mms.DATE, System.currentTimeMillis() / 1000);
        cvMain.put(Mms.SUBJECT, "My subject " + idx);

        cvMain.put(Mms.CONTENT_TYPE, "application/vnd.wap.multipart.related");
        cvMain.put(Mms.MESSAGE_CLASS, "personal");
        cvMain.put(Mms.MESSAGE_TYPE, 132); // Retrive-Conf Mms
        if (null != name_2) {
            cvMain.put(Mms.MESSAGE_SIZE, getSize(name_1) + getSize(name_2) + 512);  // suppose have 512 bytes extra text size
        } else {
            cvMain.put(Mms.MESSAGE_SIZE, getSize(name_1) + 512);  // suppose have 512 bytes extra text size
	 }

        cvMain.put(Mms.PRIORITY, String.valueOf(129));
        cvMain.put(Mms.READ_REPORT, String.valueOf(129));
        cvMain.put(Mms.DELIVERY_REPORT, String.valueOf(129));
        Random random = new Random();
        cvMain.put(Mms.MESSAGE_ID, String.valueOf(random.nextInt(100000)));
        cvMain.put(Mms.TRANSACTION_ID, String.valueOf(random.nextInt(120000)));

        long msgId = 0;
        try {
            msgId = ContentUris.parseId(mContext.getContentResolver().insert(Mms.CONTENT_URI, cvMain));
        } catch (Exception e) {
            Log.e("", "insert pdu record failed", e);
            return;
        }

        // make parts
        ContentValues cvSmil = createPartRecord(-1, "application/smil", "smil.xml", "<siml>", "smil.xml", null, smil_text);
        cvSmil.put(Part.MSG_ID, msgId);

        cv_part_1.put(Part.MSG_ID, msgId);
        if (null != name_2) {
            cv_part_2.put(Part.MSG_ID, msgId);
        }
        ContentValues cv_text_1 = createPartRecord(0, "text/plain", "text_1.txt", "<text_1>", "text_1.txt", null, null);
        cv_text_1.put(Part.MSG_ID, msgId);
        cv_text_1.remove(Part.TEXT);
        cv_text_1.put(Part.TEXT, "slide 1 text");
        cv_text_1.put(Part.CHARSET, "106");
	 ContentValues cv_text_2 = null;
        if (null != name_2) {
	        cv_text_2 = createPartRecord(0, "text/plain", "text_2.txt", "<text_2>", "text_2.txt", null, null);
	        cv_text_2.put(Part.MSG_ID, msgId);
	        cv_text_2.remove(Part.TEXT);
	        cv_text_2.put(Part.TEXT, "slide 2 text");
	        cv_text_2.put(Part.CHARSET, "106");
        }
        // insert parts
        Uri partUri = Uri.parse("content://mms/" + msgId + "/part");
        try {
            mContext.getContentResolver().insert(partUri, cvSmil);

            Uri dataUri_1 = mContext.getContentResolver().insert(partUri, cv_part_1);
            if (!copyData(dataUri_1, name_1)) {
                Log.e("", "write " + name_1 + " failed");
                return;
            }
            mContext.getContentResolver().insert(partUri, cv_text_1);
            if (null != name_2) {
                Uri dataUri_2 = mContext.getContentResolver().insert(partUri, cv_part_2);
                if (!copyData(dataUri_2, name_2)) {
                    Log.e("", "write " + name_2 + " failed");
                    return;
                }
                mContext.getContentResolver().insert(partUri, cv_text_2);
            }
        } catch (Exception e) {
            Log.e("", "insert part failed", e);
            return;
        }

        // to address
        ContentValues cvAddr = new ContentValues();
        cvAddr.put(Addr.MSG_ID, msgId);
        cvAddr.put(Addr.ADDRESS, "703");
        cvAddr.put(Addr.TYPE, "151");
        cvAddr.put(Addr.CHARSET, 106);
        mContext.getContentResolver().insert(Uri.parse("content://mms/" + msgId + "/addr"), cvAddr);

        // from address
        cvAddr.clear();
        cvAddr.put(Addr.MSG_ID, msgId);
        cvAddr.put(Addr.ADDRESS, FROM_NUM + idx);
        cvAddr.put(Addr.TYPE, "137");
        cvAddr.put(Addr.CHARSET, 106);
        mContext.getContentResolver().insert(Uri.parse("content://mms/" + msgId + "/addr"), cvAddr);
    }


    private int getSize(final String name) {
        InputStream is = null;
        int size = 0;

        try {
            int id=mContext.getResources().getIdentifier(name, "raw",  PRELOAD_APP_PACKAGE);
			is = mContext.getResources().openRawResource(id);
            byte[] buffer = new byte[1024];
            for (int len = 0; (len = is.read(buffer)) != -1;)
                size += len;
            return size;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getSize failed to found file?", e);
            return 0;
        } catch (IOException e) {
            Log.e(TAG, "getSize write failed..", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                Log.e(TAG, "getSize close failed...");
            }
        }
        return 0;
    }

    private ContentValues createPartRecord(int seq, String ct, String name, String cid, String cl, String data,
            String text) {
        ContentValues cv = new ContentValues(8);
        cv.put(Part.SEQ, seq);
        cv.put(Part.CONTENT_TYPE, ct);
        cv.put(Part.NAME, name);
        cv.put(Part.CONTENT_ID, cid);
        cv.put(Part.CONTENT_LOCATION, cl);
        if (data != null)
            cv.put(Part._DATA, data);
        if (text != null)
            cv.put(Part.TEXT, text);
        return cv;
    }

    private boolean copyData(final Uri dataUri, final String name) {
        InputStream input = null;
        OutputStream output = null;
        int id=mContext.getResources().getIdentifier(name, "raw",  PRELOAD_APP_PACKAGE);

        try {
            input = mContext.getResources().openRawResource(id);
            output = mContext.getContentResolver().openOutputStream(dataUri);

            byte[] buffer = new byte[1024];
            for (int len = 0; (len = input.read(buffer)) != -1;)
                output.write(buffer, 0, len);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyData failed to found file?", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "copyData write failed..", e);
            return false;
        } finally {
            try {
                if (input != null)
                    input.close();
                if (output != null)
                    output.close();
            } catch (IOException e) {
                Log.e(TAG, "copyData close failed...");
                return false;
            }
        }
        return true;
    }


    enum AttachmentType {
        IMAGE, AUDIO, VIDEO;
    }
//        @Override
//    public void onReceive(Context context, Intent intent) {

 //   }
}
