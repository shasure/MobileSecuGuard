package com.zsy.mobilesecuguard.chapter04;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zsy.mobilesecuguard.R;
import com.zsy.mobilesecuguard.chapter04.adapter.AppManagerAdapter;
import com.zsy.mobilesecuguard.chapter04.entity.AppInfo;
import com.zsy.mobilesecuguard.chapter04.utils.AppInfoParser;

import java.util.ArrayList;
import java.util.List;


public class AppManagerActivity extends Activity implements OnClickListener {
    /**
     * 手机剩余内存TextView
     */
    private TextView mPhoneMemoryTV;
    /**
     * 展示SD卡剩余内存TextView
     */
    private TextView mSDMemoryTV;
    private ListView mListView;
    private List<AppInfo> appInfos = new ArrayList<AppInfo>();
    ;
    private List<AppInfo> userAppInfos = new ArrayList<AppInfo>();
    private List<AppInfo> systemAppInfos = new ArrayList<AppInfo>();
    private AppManagerAdapter adapter;
    /**
     * 接收应用程序卸载成功的广播
     */
    private UninstallRececiver receciver;
    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 10:
                    if (adapter == null) {
                        adapter = new AppManagerAdapter(userAppInfos, systemAppInfos, AppManagerActivity.this);
                    }
                    mListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                case 15:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private TextView mAppNumTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_app_manager);
        //注册广播
        receciver = new UninstallRececiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(receciver, intentFilter);
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(ContextCompat.getColor(this, R.color.bright_yellow));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("软件管家");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        mPhoneMemoryTV = (TextView) findViewById(R.id.tv_phonememory_appmanager);
        mSDMemoryTV = (TextView) findViewById(R.id.tv_sdmemory_appmanager);
        mAppNumTV = (TextView) findViewById(R.id.tv_appnumber);
        mListView = (ListView) findViewById(R.id.lv_appmanager);
        //拿到手机剩余内存和SD卡剩余内存
        getMemoryFromPhone();
        initData();
        initListener();
    }

    //设置item点击监听器和scroll监听器
    private void initListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (adapter != null) {
                    new Thread() {
                        public void run() {
                            AppInfo mappInfo = (AppInfo) adapter.getItem(position);
                            //记住当前条目的状态
                            boolean flag = mappInfo.isSelected;
                            //先将集合中所有条目的AppInfo变为未选中状态
                            for (AppInfo appInfo : userAppInfos) {
                                appInfo.isSelected = false;
                            }
                            for (AppInfo appInfo : systemAppInfos) {
                                appInfo.isSelected = false;
                            }
                            if (mappInfo != null) {
                                //如果已经选中，则变为未选中
                                if (flag) {
                                    mappInfo.isSelected = false;
                                } else {
                                    mappInfo.isSelected = true;
                                }
                                mHandler.sendEmptyMessage(15);
                            }
                        }
                    }.start();
                }
            }
        });

        mListView.setOnScrollListener(new OnScrollListener() {

            /*
            switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                // 手指触屏拉动准备滚动，只触发一次        顺序: 1
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                // 持续滚动开始，只触发一次                顺序: 2
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                // 整个滚动事件结束，只触发一次            顺序: 4
                break;
            default:
                break;
            }
             */
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            // 一直在滚动中，多次触发                          顺序: 3
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem >= userAppInfos.size() + 1) {
                    mAppNumTV.setText(String.format("系统程序：%d个", systemAppInfos.size()));
                } else {
                    mAppNumTV.setText(String.format("用户程序：%d个", userAppInfos.size()));
                }
            }
        });
    }

    //获取App信息，并设置adapter
    private void initData() {
        new Thread() {
            public void run() {
                appInfos.clear();
                userAppInfos.clear();
                systemAppInfos.clear();
                appInfos.addAll(AppInfoParser.getAppInfos(AppManagerActivity.this));
                for (AppInfo appInfo : appInfos) {
                    //如果是用户App
                    if (appInfo.isUserApp) {
                        userAppInfos.add(appInfo);
                    } else {
                        systemAppInfos.add(appInfo);
                    }
                }
                mHandler.sendEmptyMessage(10);
            }
        }.start();

    }

    /**
     * 拿到手机和SD卡剩余内存
     */
    private void getMemoryFromPhone() {
        long avail_sd = Environment.getExternalStorageDirectory().getFreeSpace();
        long avail_rom = Environment.getDataDirectory().getFreeSpace();
        //格式化内存
        String str_avail_sd = Formatter.formatFileSize(this, avail_sd);
        String str_avail_rom = Formatter.formatFileSize(this, avail_rom);
        mPhoneMemoryTV.setText(String.format("剩余手机内存：%s", str_avail_rom));
        mSDMemoryTV.setText(String.format("剩余SD卡内存：%s", str_avail_sd));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_leftbtn:
                finish();
                break;
        }
    }

    /***
     * 接收应用程序卸载的广播
     *
     * @author admin
     */
    class UninstallRececiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //收到广播重新载入数据
            initData();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receciver);
        receciver = null;
        super.onDestroy();
    }
}
