package com.zsy.mobilesecuguard.chapter08.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zsy.mobilesecuguard.chapter08.db.TrafficDao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by zsy on 2016/3/28.
 */
public class TrafficMonitoringService extends Service {
    private long mOldRxBytes;
    private long mOldTxBytes;
    private TrafficDao dao;
    private SharedPreferences msp;
    private long usedFlow;
    boolean flag = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mOldRxBytes = TrafficStats.getMobileRxBytes();
        mOldTxBytes = TrafficStats.getMobileTxBytes();
        dao = new TrafficDao(this);
        msp = getSharedPreferences("config", MODE_PRIVATE);
        mThread.start();
    }

    private Thread mThread = new Thread() {
        @Override
        public void run() {
            while (flag) {
                try {
                    Thread.sleep(2000 * 60);  //两分钟执行一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateTodayGPRS();
            }
        }

        private void updateTodayGPRS() {
            //获取已经使用了的流量
            usedFlow = msp.getLong("usedflow", 0);
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            if (calendar.DAY_OF_MONTH == 1 && calendar.HOUR_OF_DAY == 0 && calendar.MINUTE < 1 && calendar.SECOND < 30) {
                usedFlow = 0;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dataString = sdf.format(date);
            long mobileGPRS = dao.getMobileGPRS(dataString);    //获取今天的流量
            long mobileRxBytes = TrafficStats.getMobileRxBytes();
            long mobileTxBytes = TrafficStats.getMobileTxBytes();
            //新产生的流量
            long newGprs = (mobileRxBytes + mobileTxBytes) - mOldRxBytes - mOldTxBytes;
            mOldTxBytes = mobileTxBytes;
            mOldRxBytes = mobileRxBytes;
            if (newGprs < 0) {
                //网络切换过
                newGprs = mobileRxBytes + mobileTxBytes;
            }
            if (mobileGPRS == -1) {
                dao.insertTodayGPRS(newGprs);
            } else {
                if (mobileGPRS < 0) {
                    mobileGPRS = 0;
                }
                dao.UpdateTodayGPRS(mobileGPRS + newGprs);
            }
            usedFlow += newGprs;
            SharedPreferences.Editor editor = msp.edit();
            editor.putLong("usedflow", usedFlow);
            editor.apply();
        }
    };

    @Override
    public void onDestroy() {
        if(mThread !=null && !mThread.isInterrupted()){
            flag = false;
            mThread.interrupt();
            mThread = null;
        }
        super.onDestroy();
    }
}
