package com.zsy.mobilesecuguard.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    /**
     * md5加密的算法
     *
     * @param text
     * @return
     */
    public static String encode(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(text.getBytes());
            //convert to hex string
            return bytes2HexString(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //can't reach
            return "";
        }
    }

    private static String bytes2HexString(byte[] result) {
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            int number = b & 0xff;
            String hex = Integer.toHexString(number);
            if (hex.length() == 1) {
                sb.append("0").append(hex);
            } else {
                sb.append(hex);
            }
        }
        return sb.toString();
    }

    /**
     * 计算文件md5
     *
     * @param path
     * @return
     */
    public static String getFileMd5(String path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            byte[] result = digest.digest();
            return bytes2HexString(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
