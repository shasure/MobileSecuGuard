package com.zsy.mobilesecuguard;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.zsy.mobilesecuguard.chapter01.adapter.HomeAdapter;
import com.zsy.mobilesecuguard.chapter02.LostFindActivity;
import com.zsy.mobilesecuguard.chapter02.dialogs.InterPasswordDialog;
import com.zsy.mobilesecuguard.chapter02.dialogs.SetUpPasswordDialog;
import com.zsy.mobilesecuguard.chapter02.receiver.MyDeviceAdminReceiver;
import com.zsy.mobilesecuguard.chapter02.utils.MD5Utils;
import com.zsy.mobilesecuguard.chapter03.SecurityPhoneActivity;

/**
 * Created by zsy on 2016/3/12.
 */
public class HomeActivity extends Activity {

    /**
     * 声明GridView 该控件类似ListView
     */
    private GridView gv_home;
    /**
     * 存储手机防盗密码的sp
     */
    private SharedPreferences msharedPreferences;
    /**
     * 设备管理员
     */
    private DevicePolicyManager policyManager;
    /**
     * 申请权限
     */
    private ComponentName componentName;
    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        msharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        //初始化GridView
        gv_home = (GridView) findViewById(R.id.gv_home);
        gv_home.setAdapter(new HomeAdapter(HomeActivity.this));
        //设置条目的点击事件
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //parent是gridview，view是每个item的view，position是item位置，
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        if (isSetUpPassword()) {
                            showInterPswdDialog();
                        } else {
                            showSetUpPswdDialog();
                        }
                        break;
                    case 1:
                        startActivity(SecurityPhoneActivity.class);
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                }
            }
        });
        //每次启动HomeActivity都会看看有没有申请设备管理员权限
        //DevicePolicyManager manages policies for one or more DeviceAdminReceiver instances
        policyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        //获取target policy
        componentName = new ComponentName(this, MyDeviceAdminReceiver.class);
        //判断如果没有权限就申请
        boolean active = policyManager.isAdminActive(componentName);
        if (!active) {
            //获取管理员
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "获取管理员权限，用于远程锁屏和清除数据");
            startActivity(intent);
        }
    }

    /***
     * 弹出设置密码对话框
     */
    private void showSetUpPswdDialog() {
        final SetUpPasswordDialog setUpPasswordDialog = new SetUpPasswordDialog(HomeActivity.this);
        setUpPasswordDialog.setCallBack(new SetUpPasswordDialog.MyCallBack() {

            @Override
            public void ok() {
                String firstPwsd = setUpPasswordDialog.mFirstPWDET.getText().toString().trim();
                String affirmPwsd = setUpPasswordDialog.mAffirmET.getText().toString().trim();
                if (!TextUtils.isEmpty(firstPwsd) && !TextUtils.isEmpty(affirmPwsd)) {
                    if (firstPwsd.equals(affirmPwsd)) {
                        // 两次密码一致,存储密码
                        savePswd(affirmPwsd);
                        setUpPasswordDialog.dismiss();
                        // 显示输入密码对话框
                        showInterPswdDialog();
                    } else {
                        Toast.makeText(HomeActivity.this, "两次密码不一致！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void cancle() {
                setUpPasswordDialog.dismiss();
            }
        });
        setUpPasswordDialog.setCancelable(true);
        setUpPasswordDialog.show();
    }

    /**
     * 弹出输入密码对话框
     */
    private void showInterPswdDialog() {
        final String password = getPassword();
        final InterPasswordDialog mInPswdDialog = new InterPasswordDialog(HomeActivity.this);
        mInPswdDialog.setCallBack(new InterPasswordDialog.MyCallBack() {
            @Override
            public void confirm() {
                if (TextUtils.isEmpty(mInPswdDialog.getPassword())) {
                    Toast.makeText(HomeActivity.this, "密码不能为空！", Toast.LENGTH_SHORT).show();
                } else if (password.equals(MD5Utils.encode(mInPswdDialog
                        .getPassword()))) {
                    // 进入防盗主界面
                    mInPswdDialog.dismiss();
                    startActivity(LostFindActivity.class);
                } else {
                    // 对话框消失，弹出土司
                    mInPswdDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "密码有误，请重新输入！", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void cancel() {
                mInPswdDialog.dismiss();
            }
        });
        mInPswdDialog.setCancelable(true);
        // 让对话框显示
        mInPswdDialog.show();
    }


    /**
     * 保存密码
     */
    private void savePswd(String affirmPwsd) {
        SharedPreferences.Editor edit = msharedPreferences.edit();
        edit.putString("PhoneAntiTheftPWD", MD5Utils.encode(affirmPwsd));
        edit.commit();
    }

    /**
     * 获取密码
     */
    private String getPassword() {
        String password = msharedPreferences.getString("PhoneAntiTheftPWD", null);
        if (TextUtils.isEmpty(password)) {
            return "";
        }
        return password;
    }

    /**
     * 判断用户是否设置过手机防盗
     */
    private boolean isSetUpPassword() {
        String password = msharedPreferences.getString("PhoneAntiTheftPWD", null);
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        return true;
    }

    /**
     * 使用HomeActivity开启新activity,不关闭HomeActivity
     */
    public void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    /**
     * double click back button to exit
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
