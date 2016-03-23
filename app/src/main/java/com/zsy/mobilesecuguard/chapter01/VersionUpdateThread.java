package com.zsy.mobilesecuguard.chapter01;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.util.LogUtils;
import com.zsy.mobilesecuguard.HomeActivity;
import com.zsy.mobilesecuguard.R;
import com.zsy.mobilesecuguard.chapter01.entity.VersionEntity;
import com.zsy.mobilesecuguard.chapter01.utils.DownLoadUtils;
import com.zsy.mobilesecuguard.chapter01.utils.MyUtils;
import com.zsy.mobilesecuguard.utils.Configure;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by zsy on 2016/3/12.
 * check and update app version
 */
public class VersionUpdateThread implements Runnable {
    private static final int MESSAGE_NET_EEOR = 101;
    private static final int MESSAGE_IO_EEOR = 102;
    private static final int MESSAGE_JSON_EEOR = 103;
    private static final int MESSAGE_SHOEW_DIALOG = 104;
    protected static final int MESSAGE_ENTERHOME = 105;

    private String mVersion;    //local version
    private Activity context;    //splash activity
    private VersionEntity versionEntity;    //Server version info
    private ProgressDialog mProgressDialog;

    private Handler handler;
    private Handler activityHandler;

    /**
     * enter homeActivity
     */
    private void enterHome() {
        handler.sendEmptyMessageAtTime(MESSAGE_ENTERHOME, 2000);
    }

    public VersionUpdateThread(String mVersion, Activity context, Handler activityHandler) {
        this.mVersion = mVersion;
        this.context = context;
        this.activityHandler = activityHandler;
    }

    /**
     * obtain the latest version on server
     */
    public void getServerVersion() {
        try {
            LogUtils.d("in getServerVersion");
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 2000);
            HttpConnectionParams.setSoTimeout(client.getParams(), 2000);
            HttpGet httpGet = new HttpGet(Configure.URL_UPDATEINFO);
            HttpResponse httpRes = client.execute(httpGet);
            if (httpRes.getStatusLine().getStatusCode() == 200) {
                HttpEntity httpEntity = httpRes.getEntity();
                String jsonRawData = EntityUtils.toString(httpEntity, "utf-8");
                JSONObject jsonObject = new JSONObject(jsonRawData);
                this.versionEntity = new VersionEntity();
                versionEntity.setVersionName(jsonObject.getString("code"));
                versionEntity.setDescription(jsonObject.getString("des"));
                versionEntity.setApkDownloadUrl(jsonObject.getString("apkurl"));
                if (!mVersion.equals(versionEntity.getVersionName())) {
                    //diffrent version
                    LogUtils.d("发送message_show_dialog");
                    handler.sendEmptyMessage(MESSAGE_SHOEW_DIALOG);
                }
            }
        } catch (JSONException e) {
            handler.sendEmptyMessage(MESSAGE_JSON_EEOR);
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            handler.sendEmptyMessage(MESSAGE_NET_EEOR);
            e.printStackTrace();
        } catch (IOException e) {
            handler.sendEmptyMessage(MESSAGE_IO_EEOR);
            e.printStackTrace();
        }
    }

    private void showUpdateDialog(final VersionEntity versionEntity) {
        //create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("检查到新版本:" + versionEntity.getVersionName());
        builder.setMessage(versionEntity.getDescription());
        builder.setIcon(R.drawable.ic_launcher);

        builder.setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initProgressDialog();
                downloadNewApk(versionEntity.getApkDownloadUrl());
            }
        });

        builder.setNegativeButton("暂不升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();   //dismiss() will release the memory of dialog
                enterHome();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                enterHome();
            }
        });
        builder.show();
    }

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(context);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setMessage("准备下载...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();
            }
        };
        activityHandler.post(r);
    }

    private void downloadNewApk(String apkDownloadUrl) {
        final String apkLocalUrl = "/mnt/sdcard/MobileSecuGuard2.0.apk";
        DownLoadUtils downLoadUtils = new DownLoadUtils();
        downLoadUtils.downapk(apkDownloadUrl, apkLocalUrl, new DownLoadUtils.DownloadCallBack() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                mProgressDialog.dismiss();
                LogUtils.d("下载成功");
                MyUtils.installAPK(context, apkLocalUrl);
                try {
                    Looper.myLooper().quit();       //quit loop
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.setMessage("下载失败");
                    }
                };
                activityHandler.post(r);
                LogUtils.d("下载失败");
                mProgressDialog.dismiss();
                enterHome();
            }

            @Override
            public void onLoading(final long total, final long current, final boolean isUploading) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.setMax((int) total);
                        mProgressDialog.setProgress((int) current);
                        mProgressDialog.setMessage("正在下载...");
                    }
                };
                activityHandler.post(r);
            }
        });
    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_IO_EEOR:
                    Toast.makeText(context, "IO Exception", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case MESSAGE_JSON_EEOR:
                    Toast.makeText(context, "JSON Parse Error", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case MESSAGE_NET_EEOR:
                    Toast.makeText(context, "network exception", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case MESSAGE_SHOEW_DIALOG:
                    showUpdateDialog(versionEntity);
                    break;
                case MESSAGE_ENTERHOME:
                    Intent intent = new Intent(context, HomeActivity.class);
                    context.startActivityForResult(intent, 2);
                    try {
                        Looper.myLooper().quit();       //quit loop
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void run() {
        Log.d("mobile_tag", "run:" + Thread.currentThread().getName());
        Looper.prepare();
        handler = new MyHandler();
        LogUtils.d("after new MyHander");
        //下面两个语句不能颠倒，原因估计是looper里是个for(;;)死循环，要先getServerVersion发送消息，然后loop
        getServerVersion();
        Looper.loop();
    }
}
