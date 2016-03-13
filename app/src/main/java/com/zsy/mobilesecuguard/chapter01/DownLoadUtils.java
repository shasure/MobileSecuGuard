package com.zsy.mobilesecuguard.chapter01;


import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;

/**
 * Created by zsy on 2016/3/12.
 */
public class DownLoadUtils {
    /**
     * @param url
     * @param targerFile
     * @param downloadCallBack this is a callback function
     */
    public void downapk(String url, String targerFile, final DownloadCallBack downloadCallBack) {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.download(url, targerFile, new RequestCallBack<File>() {
            @Override
            public void onSuccess(ResponseInfo<File> responseInfo) {
                downloadCallBack.onSuccess(responseInfo);
            }

            @Override
            public void onFailure(HttpException e, String s) {
                downloadCallBack.onFailure(e, s);
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                downloadCallBack.onLoading(total, current, isUploading);
            }
        });
    }


    interface DownloadCallBack {
        void onSuccess(ResponseInfo<File> responseInfo);

        void onFailure(HttpException e, String s);

        void onLoading(long total, long current, boolean isUploading);
    }


}
