package com.zsy.mobilesecuguard.chapter05;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zsy.mobilesecuguard.R;
import com.zsy.mobilesecuguard.chapter05.adapter.ScanVirusAdapter;
import com.zsy.mobilesecuguard.chapter05.db.AntiVirusDao;
import com.zsy.mobilesecuguard.chapter05.entity.ScanAppInfo;
import com.zsy.mobilesecuguard.utils.Configure;
import com.zsy.mobilesecuguard.utils.MD5Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zsy on 2016/3/26.
 */
public class VirusScanSpeedActivity extends Activity implements View.OnClickListener {
    protected static final int SCAN_BENGIN = 100;
    protected static final int SCANNING = 101;
    protected static final int SCAN_FINISH = 102;
    private int total;
    private int process;
    private TextView mProcessTv;
    private PackageManager pm;
    private boolean flag;       //control scan thread
    private boolean isStop;
    private TextView mScanAppTV;
    private Button mCanCelBtn;
    private ImageView mScanningIcon;
    private RotateAnimation rani;
    private ListView mScanListView;
    private ScanVirusAdapter adapter;
    private List<ScanAppInfo> mScanAppInfos = new ArrayList<>();
    private SharedPreferences mSp;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_BENGIN:
                    mScanAppTV.setText("初始化杀毒引擎中...");
                    break;
                case SCANNING:
                    ScanAppInfo info = (ScanAppInfo) msg.obj;
                    mScanAppTV.setText(String.format("正在扫描: %s", info.appName));
                    int speed = msg.arg1;
                    mProcessTv.setText(String.format("%d%%", speed * 100 / total));
                    mScanAppInfos.add(info);
                    adapter.notifyDataSetChanged();
                    mScanListView.setSelection(mScanAppInfos.size());
                    break;
                case SCAN_FINISH:
                    mScanAppTV.setText("扫描完成！");
                    mScanningIcon.clearAnimation();
                    mCanCelBtn.setBackgroundResource(R.drawable.scan_complete);
                    saveScanTime();
            }
        }

        private void saveScanTime() {
            SharedPreferences.Editor editor = mSp.edit();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            currentTime += "上次查杀: ";
            editor.putString("lastVirusScan", currentTime);
            editor.apply();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_virusscanspeed);
        pm = getPackageManager();
        mSp = getSharedPreferences("config", MODE_PRIVATE);
        initView();
        scanVirus();
    }

    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        ((TextView) findViewById(R.id.tv_title)).setText("病毒查杀进度");
        mProcessTv = (TextView) findViewById(R.id.tv_scanprocess);
        mScanAppTV = (TextView) findViewById(R.id.tv_scansapp);
        mCanCelBtn = (Button) findViewById(R.id.btn_canclescan);
        mCanCelBtn.setOnClickListener(this);
        mScanListView = (ListView) findViewById(R.id.lv_scanapps);
        adapter = new ScanVirusAdapter(mScanAppInfos, this);
        mScanListView.setAdapter(adapter);
        mScanningIcon = (ImageView) findViewById(R.id.imgv_scanningicon);
        startAnim();
    }

    private void startAnim() {
        if (rani == null) {
            rani = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }
        rani.setRepeatCount(Animation.INFINITE);
        rani.setDuration(2000);
        mScanningIcon.startAnimation(rani);
    }

    private void scanVirus() {
        flag = true;
        isStop = false;
        process = 0;
        mScanAppInfos.clear();
        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.what = SCAN_BENGIN;
                mHandler.sendMessage(msg);
                List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
                total = installedPackages.size();
                AntiVirusDao dao = new AntiVirusDao(getDatabasePath(Configure.ANTIVIRUS_DB_NAME).getPath());
                Log.i(Configure.TAG, "db路径：" + getDatabasePath(Configure.ANTIVIRUS_DB_NAME).getPath());
                for (PackageInfo info : installedPackages) {
                    if (!flag) {
                        isStop = true;
                        dao.close();
                        return;
                    }
                    String apkpath = info.applicationInfo.sourceDir;
                    Log.i(Configure.TAG, info.packageName + " sourceDir ： " + apkpath);
                    //计算apk特征码
                    String md5info = MD5Utils.getFileMd5(apkpath);
                    String result = dao.checkVirus(md5info);
                    msg = Message.obtain();
                    msg.what = SCANNING;
                    ScanAppInfo scanInfo = new ScanAppInfo();
                    if (result == null) {
                        scanInfo.description = "扫描安全";
                        scanInfo.isVirus = false;
                    } else {
                        scanInfo.description = result;
                        scanInfo.isVirus = true;
                    }
                    process++;
                    scanInfo.packageName = info.packageName;
                    scanInfo.appName = info.applicationInfo.loadLabel(pm).toString();
                    scanInfo.appicon = info.applicationInfo.loadIcon(pm);
                    msg.obj = scanInfo;
                    msg.arg1 = process;
                    mHandler.sendMessage(msg);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                msg = Message.obtain();
                msg.what = SCAN_FINISH;
                mHandler.sendMessage(msg);
                dao.close();
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.btn_canclescan:
                if (process == total & process > 0) {
                    //扫描完成
                    finish();
                } else if (process > 0 & process < total & !isStop) {
                    mScanningIcon.clearAnimation();
                    flag = false;
                    mCanCelBtn.setBackgroundResource(R.drawable.restart_scan_btn);

                } else if (isStop) {
                    startAnim();
                    scanVirus();
                    mCanCelBtn.setBackgroundResource(R.drawable.cancle_scan_btn_selector);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        flag = false;
        super.onDestroy();
    }
}
