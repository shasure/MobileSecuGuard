package com.zsy.mobilesecuguard.chapter03.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.zsy.mobilesecuguard.chapter03.db.BlackNumberDao;

/**
 * Created by zsy on 2016/3/24.
 */
public class InterceptSmsReciever extends BroadcastReceiver {

    private static final int SMS_CHARACTER_LIMIT = 160;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        boolean BlackNumStatus = sp.getBoolean("BlackNumStatus", true);
        //未开启拦截
        if (BlackNumStatus) {
            return;
        }
        SmsMessage[] smsMessage = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        String sender = smsMessage[0].getDisplayOriginatingAddress();
        String body = mergeMessageBodies(smsMessage);
        if (sender.startsWith("+86")) {
            sender = sender.substring(3, sender.length());
        }
        BlackNumberDao dao = new BlackNumberDao(context);
        int mode = dao.getBlackContactMode(sender);
        if (mode == 2 || mode == 3) {
            abortBroadcast();
        }
    }

    private static String mergeMessageBodies(SmsMessage[] messageParts) {
        if (messageParts.length == 1) {
            return messageParts[0].getDisplayMessageBody();
        } else {
            StringBuilder sb = new StringBuilder(SMS_CHARACTER_LIMIT * messageParts.length);
            for (SmsMessage messagePart : messageParts) {
                sb.append(messagePart.getDisplayMessageBody());
            }
            return sb.toString();
        }
    }
}
