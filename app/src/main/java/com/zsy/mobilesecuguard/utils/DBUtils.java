package com.zsy.mobilesecuguard.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zsy on 2016/3/28.
 */
public class DBUtils {
    public static void copyDB(final Context context, final String dbName) {
        new Thread() {
            @Override
            public void run() {
                //file路径  /data/user/0/com.zsy.mobilesecuguard/databases/antivirus.db
                File file = context.getDatabasePath(dbName);
                if (file.exists() && file.length() > 0) {
                    Log.i(Configure.TAG, "数据库已存在");
                    return;
                }
                try {
                    InputStream is = context.getAssets().open(dbName);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
