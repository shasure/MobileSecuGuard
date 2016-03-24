package com.zsy.mobilesecuguard.chapter03.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.view.menu.MenuItemWrapperICS;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import com.zsy.mobilesecuguard.chapter03.db.BlackNumberDao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by zsy on 2016/3/24.
 */
public class InterceptCallReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        boolean blackNumStatus = sp.getBoolean("BlackNumStatus", true);
        if (blackNumStatus) {
            return;
        }
        BlackNumberDao dao = new BlackNumberDao(context);
        //非outgoing_call
        if (!intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String mIncomingNumber = "";
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch (tMgr.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING:
                    mIncomingNumber = intent.getStringExtra("incoming_number");
                    int blackContackMode = dao.getBlackContactMode(mIncomingNumber);
                    if (blackContackMode == 1 || blackContackMode == 3) {
                        //监视呼叫记录，如果产生了呼叫记录就删除
                        Uri uri = Uri.parse("content://call_log/calls");
                        ContentResolver cr = context.getContentResolver();
                        cr.registerContentObserver(uri, true, new CallLogObserver(new Handler(), mIncomingNumber, context, uri));
                        //挂断电话
                        endCall(context);
                    }
                    break;
            }
        }

    }

    private void endCall(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class clazz = telephonyManager.getClass();
            Method method = clazz.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            Object iTelephonyObj = method.invoke(telephonyManager);
            Class cls = iTelephonyObj.getClass();
            Method methodEndCall = cls.getDeclaredMethod("endCall");
            methodEndCall.invoke(iTelephonyObj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }

    private class CallLogObserver extends ContentObserver {

        private String incomingNumber;
        private Context context;
        private Uri uri;

        public CallLogObserver(Handler handler, String mIncomingNumber, Context context, Uri uri) {
            super(handler);
            this.incomingNumber = mIncomingNumber;
            this.context = context;
            this.uri = uri;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            context.getContentResolver().unregisterContentObserver(this);
            deleteCallLog(incomingNumber, context, uri);
        }
    }

    private void deleteCallLog(String incomingNumber, Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"_id"}, "number=?",
                new String[]{incomingNumber}, "_id desc limit 1");
        if (cursor != null && cursor.moveToNext()) {
            String id = cursor.getString(0);
            resolver.delete(uri, "_id = ?", new String[]{id});
            cursor.close();
        }
    }
}
