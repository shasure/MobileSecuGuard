package com.zsy.mobilesecuguard.chapter01.entity;

/**
 * Created by zsy on 2016/3/12.
 */
public class VersionEntity {
    private String versionName;
    private String description;
    private String apkDownloadUrl;

    public String getVersionName() {
        return versionName;
    }

    public String getDescription() {
        return description;
    }

    public String getApkDownloadUrl() {
        return apkDownloadUrl;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setApkDownloadUrl(String apkDownloadUrl) {
        this.apkDownloadUrl = apkDownloadUrl;
    }
}
