package com.zsy.mobilesecuguard.chapter09;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsy.mobilesecuguard.R;
import com.zsy.mobilesecuguard.chapter09.fragment.AppLockFragment;
import com.zsy.mobilesecuguard.chapter09.fragment.AppUnLockFragment;

import java.util.ArrayList;
import java.util.List;

/***
 * 程序锁
 **/
public class AppLockActivity extends FragmentActivity implements OnClickListener {

    private ViewPager mAppViewPager;
    List<Fragment> mFragments = new ArrayList<>();
    private TextView mLockTV;
    private TextView mUnLockTV;
    private View slideLockView;
    private View slideUnLockView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_applock);
        initView();
        initListener();
    }

    private void initListener() {
        mAppViewPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                if (arg0 == 0) {
                    slideUnLockView.setBackgroundResource(R.drawable.slide_view);
                    slideLockView.setBackgroundColor(getResources().getColor(R.color.transparent));
                    //未加锁
                    mLockTV.setTextColor(getResources().getColor(R.color.black));
                    mUnLockTV.setTextColor(getResources().getColor(R.color.bright_red));
                } else {
                    slideLockView.setBackgroundResource(R.drawable.slide_view);
                    slideUnLockView.setBackgroundColor(getResources().getColor(R.color.transparent));
                    //已加锁
                    mLockTV.setTextColor(getResources().getColor(R.color.bright_red));
                    mUnLockTV.setTextColor(getResources().getColor(R.color.black));
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

    }

    private void initView() {
        findViewById(R.id.rl_titlebar).setBackgroundColor(getResources().getColor(R.color.bright_red));
        ImageView mLeftImgv = (ImageView) findViewById(R.id.imgv_leftbtn);
        ((TextView) findViewById(R.id.tv_title)).setText("程序锁");
        mLeftImgv.setOnClickListener(this);
        mLeftImgv.setImageResource(R.drawable.back);
        mAppViewPager = (ViewPager) findViewById(R.id.vp_applock);
        mLockTV = (TextView) findViewById(R.id.tv_lock);
        mUnLockTV = (TextView) findViewById(R.id.tv_unlock);
        mLockTV.setOnClickListener(this);
        mUnLockTV.setOnClickListener(this);
        slideLockView = findViewById(R.id.view_slide_lock);
        slideUnLockView = findViewById(R.id.view_slide_unlock);
        //ViewPager Fragment
        AppUnLockFragment unLock = new AppUnLockFragment();
        AppLockFragment lock = new AppLockFragment();
        mFragments.add(unLock);
        mFragments.add(lock);
        mAppViewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgv_leftbtn:
                finish();
                break;
            case R.id.tv_lock:
                mAppViewPager.setCurrentItem(1);
                break;
            case R.id.tv_unlock:
                mAppViewPager.setCurrentItem(0);
                break;
        }

    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int arg0) {
            return mFragments.get(arg0);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}
