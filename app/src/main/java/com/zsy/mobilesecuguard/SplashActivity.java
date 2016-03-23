package com.zsy.mobilesecuguard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.lidroid.xutils.util.LogUtils;
import com.zsy.mobilesecuguard.chapter01.utils.MyUtils;
import com.zsy.mobilesecuguard.chapter01.VersionUpdateThread;


public class SplashActivity extends AppCompatActivity {
    
    private String mVersion;        //local version

    private TextView mVersionTV;    //textview on splashActivity

    private Handler activityHandler = new Handler();    //used to updateUI through handler.post(runnable)

    public int test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);    //use this will show an empty activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mVersion = MyUtils.getVersion(getApplicationContext());
        initView();     //show app local version on splashActivity
        Log.d("mobile_tag", "main:" + Thread.currentThread().getName());
        //获取server version
        new Thread(new VersionUpdateThread(mVersion, SplashActivity.this, activityHandler)).start();

        LogUtils.d("SplashActivity end");
    }

    private void initView() {
        mVersionTV = (TextView)findViewById(R.id.tv_splash_version);
        mVersionTV.setText("版本号" + mVersion);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.d("in onAcrivityResult");
        switch (requestCode){
            case 1:     //start install activity
                if (resultCode == RESULT_CANCELED){
                    LogUtils.d("install activity return cancel");
                    startActivity(new Intent(this, HomeActivity.class));
                }
                if (resultCode == RESULT_OK){
                    LogUtils.d("install activity return ok");
//                    finish();
                }
                break;
            case 2:         //start homeactivity
                LogUtils.d("enterhome activity return");
                finish();
                break;
        }
    }
}
