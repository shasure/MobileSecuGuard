package com.zsy.mobilesecuguard.chapter01.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.lidroid.xutils.util.LogUtils;
import com.zsy.mobilesecuguard.utils.PathUtils;

import java.io.File;

/**
 * Created by zsy on 2016/3/12.
 */
public class MyUtils {
    /**
     * get app local version. in Gradle versionCode and versionName attributes are in build.gradle not manifest
     * @param context
     * @return
     */
    public static String getVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void installAPK(Activity activity, String apkLocalUrl) {
        //implicit intent
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(new File(apkLocalUrl)),
                "application/vnd.android.package-archive");
        LogUtils.d(PathUtils.SDCARD_PATH);
        activity.startActivityForResult(intent, 1);
    }
}
