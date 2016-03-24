package com.zsy.mobilesecuguard.chapter02.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * 当定义的策略(Policy)事件发生的时候会通知创建的这个设备管理器Receiver。
 * 例如如果用户撤销了设备管理器，receiver执行onDisabled，我们可以在里面实现一些敏感数据的清除
 */
public class MyDeviceAdminReceiver extends DeviceAdminReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}
}
