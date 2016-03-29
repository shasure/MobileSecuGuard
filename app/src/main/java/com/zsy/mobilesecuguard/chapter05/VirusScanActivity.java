package com.zsy.mobilesecuguard.chapter05;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsy.mobilesecuguard.R;
import com.zsy.mobilesecuguard.utils.Configure;
import com.zsy.mobilesecuguard.utils.DBUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zsy on 2016/3/25.
 */
public class VirusScanActivity extends Activity implements View.OnClickListener {

    private TextView mLastTimeTV;
    private SharedPreferences mSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_viruscan);
        mSp = getSharedPreferences("config", MODE_PRIVATE);
        DBUtils.copyDB(this, Configure.ANTIVIRUS_DB_NAME);
        initView();

    }

    @Override
    protected void onResume() {
        String scanLog = mSp.getString("lastVirusScan", "您还没有查杀病毒");
        mLastTimeTV.setText(scanLog);
        super.onResume();
    }

    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        mLastTimeTV = (TextView)findViewById(R.id.tv_lastscantime);
        findViewById(R.id.rl_allscanvirus).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.rl_allscanvirus:
                startActivity(new Intent(this, VirusScanSpeedActivity.class));
                break;
        }
    }
}
