package com.zsy.mobilesecuguard.chapter02.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zsy.mobilesecuguard.App;
import com.zsy.mobilesecuguard.utils.Configure;


/** 监听开机的广播该类，主要用于检查SIM卡是否被更换，如果被更换则发送短信给安全号码 */
public class SimBootCompleteReciever extends BroadcastReceiver {

	private static final String TAG = SimBootCompleteReciever.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(Configure.TAG, getClass().getName() + " start after boot");
		((App) context.getApplicationContext()).correctSIM();//初始化
	}

}
