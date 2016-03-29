package com.zsy.mobilesecuguard.chapter08;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.zsy.mobilesecuguard.R;
import com.zsy.mobilesecuguard.chapter07.utils.SystemInfoUtils;
import com.zsy.mobilesecuguard.chapter08.db.TrafficDao;
import com.zsy.mobilesecuguard.chapter08.service.TrafficMonitoringService;
import com.zsy.mobilesecuguard.utils.Configure;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by zsy on 2016/3/27.
 */
public class TrafficMonitoringActivity extends Activity implements View.OnClickListener {

    private SharedPreferences msp;
    private Button mCorrectFlowBtn;
    private TextView mTotalTV;
    private TextView mUsedTV;
    private TextView mToDayTV;
    private TrafficDao dao;
    private ImageView mRemindIMGV;
    private TextView mRemindTV;
    private CorrectFlowReceiver receiver = null;

    public static final int SMS_CHARACTER_LIMIT = 160;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_trafficmonitoring);
        msp = getSharedPreferences("config", MODE_PRIVATE);
        boolean flag = msp.getBoolean("isset_operator", false);
        if (!flag) {    //启动运营商设置
            startActivity(new Intent(this, OperatorSetActivity.class));
            finish();
        }
        if (!SystemInfoUtils.isServiceRunning(this, "com.zsy.mobilesecuguard.chapter08.service.TrafficMonitoring Service")) {
            startService(new Intent(this, TrafficMonitoringService.class)); //开启流量监控服务
        }
        initView();
        registReceiver();
        initData();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(ContextCompat.getColor(this, R.color.light_green));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("流量监控");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        mCorrectFlowBtn = (Button) findViewById(R.id.btn_correction_flow);
        mCorrectFlowBtn.setOnClickListener(this);
        mTotalTV = (TextView) findViewById(R.id.tv_month_totalgprs);
        mUsedTV = (TextView) findViewById(R.id.tv_month_usedgprs);
        mToDayTV = (TextView) findViewById(R.id.tv_today_gprs);
        mRemindIMGV = (ImageView) findViewById(R.id.imgv_traffic_remind);
        mRemindTV = (TextView) findViewById(R.id.tv_traffic_remind);
    }

    private void initData() {
        long totalflow = msp.getLong("totalflow", 0);
        long usedflow = msp.getLong("usedflow", 0);
        if (totalflow > 0 && usedflow >= 0) {
            float scale = usedflow / totalflow;
            if (scale > 0.9) {
                mRemindIMGV.setEnabled(false);
                mRemindTV.setText("您的套餐流量即将用完");
            } else {
                mRemindIMGV.setEnabled(true);
                mRemindTV.setText("本月流量充足请放心使用");
            }
        }
        mTotalTV.setText("本月流量:" + Formatter.formatFileSize(this, totalflow));
        mUsedTV.setText("本月已用:" + Formatter.formatFileSize(this, usedflow));
        dao = new TrafficDao(this);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = sdf.format(date);
        long mobileGPRS = dao.getMobileGPRS(dateString);
        if (mobileGPRS < 0)
            mobileGPRS = 0;
        mToDayTV.setText("本日已用:" + Formatter.formatFileSize(this, mobileGPRS));
    }

    private void registReceiver() {
        receiver = new CorrectFlowReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver, filter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.btn_correction_flow:
                // 首先判断是哪个运营商，
                int i = msp.getInt("operator", 0);
                SmsManager smsManager = SmsManager.getDefault();
                switch (i) {
                    case 0:
                        // 没有设置运营商
                        Toast.makeText(this, "您还没有设置运营商信息", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        // 中国移动
                        break;
                    case 2:
                        // 中国联通
                        // 发送LLCX至10010
                        // 获取系统默认的短信管理器
                        smsManager.sendTextMessage("10010", null, "1071", null, null);
                        break;
                    case 3:
                        // 中国电信
                        break;
                }
        }
    }

    private class CorrectFlowReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            String address = smsMessages[0].getDisplayOriginatingAddress();
            String body = mergeMessageBodies(smsMessages);
            Log.i(Configure.TAG, address + "   " + body);
            // 以下短信分割只针对联通3G用户
            if (!address.equals("10010")) {
                return;
            }
            // 本月剩余流量
            long thisMonthleft = 0;
            // 本月已用流量
            long used = 0;
            // 本月超出流量
            long beyond = 0;
            // 上月剩余
            long lastMonthRemain = 0;
            //regex
            Pattern pattern = Pattern.compile("(.*?)(\\d+\\.\\d+)([a-zA-Z]{1,2})");
            Matcher matcher = pattern.matcher(body);
            while (matcher.find()) {
                Log.i(Configure.TAG, matcher.group(0));
                Log.i(Configure.TAG, matcher.group(1));
                Log.i(Configure.TAG, matcher.group(2));
                Log.i(Configure.TAG, matcher.group(3));
                if (matcher.group(1).contains("本月产生的总流量")) {
                    used = bytesNum(matcher.group(2), matcher.group(3));
                }
                if (matcher.group(1).contains("套餐内剩余流量")) {
                    thisMonthleft = bytesNum(matcher.group(2), matcher.group(3));
                }
                if (matcher.group(1).contains("结转剩余流量")) {
                    lastMonthRemain = bytesNum(matcher.group(2), matcher.group(3));
                }
            }

            SharedPreferences.Editor edit = msp.edit();
            edit.putLong("totalflow", used + thisMonthleft + lastMonthRemain);
            edit.putLong("usedflow", used + beyond);
            edit.apply();
            mTotalTV.setText("本月流量："
                    + Formatter.formatFileSize(context, (used + thisMonthleft + lastMonthRemain)));
            mUsedTV.setText("本月已用："
                    + Formatter.formatFileSize(context, (used + beyond)));
        }

        /**
         * 计算有多少Bytes
         *
         * @param num
         * @param unit
         * @return
         */
        private long bytesNum(String num, String unit) {
            float fNum = Float.parseFloat(num);
            long numBytes = 0;
            if (unit.equals("B")) {
                numBytes = (long) fNum;
            }
            if (unit.equals("KB")) {
                numBytes = (long) (fNum * Math.pow(2, 10));
            }
            if (unit.equals("MB")) {
                numBytes = (long) (fNum * Math.pow(2, 20));
            }
            if (unit.equals("GB")) {
                numBytes = (long) (fNum * Math.pow(2, 30));
            }
            return numBytes;
        }


    }

    private String mergeMessageBodies(SmsMessage[] smsMessages) {
        if (smsMessages.length == 1)
            return smsMessages[0].getDisplayMessageBody();
        else {
            StringBuilder sb = new StringBuilder(SMS_CHARACTER_LIMIT * smsMessages.length);
            for (SmsMessage message : smsMessages) {
                sb.append(message.getDisplayMessageBody());
            }
            return sb.toString();
        }
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }
}
