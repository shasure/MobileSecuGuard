package com.zsy.mobilesecuguard.chapter08.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zsy.mobilesecuguard.chapter07.utils.SystemInfoUtils;
import com.zsy.mobilesecuguard.chapter08.service.TrafficMonitoringService;
import com.zsy.mobilesecuguard.utils.Configure;

/**
 * Created by zsy on 2016/3/28.
 */
public class TrafficBootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(Configure.TAG, getClass().getName() + "start after boot");
        if (!SystemInfoUtils.isServiceRunning(context, "com.zsy.mobilesecuguard.chapter08.service.TrafficMonitoringService")) {
            context.startService(new Intent(context, TrafficMonitoringService.class));
        }
    }
}
