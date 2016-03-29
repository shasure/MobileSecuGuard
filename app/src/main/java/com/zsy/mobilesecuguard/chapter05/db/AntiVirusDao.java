package com.zsy.mobilesecuguard.chapter05.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by zsy on 2016/3/26.
 */
public class AntiVirusDao {
    private String dbPath;
    private SQLiteDatabase db;

    public AntiVirusDao(String dbPath) {
        this.dbPath = dbPath;
        db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public String checkVirus(String md5) {
        String desc = null;
        Cursor cursor = db.query("datable", new String[]{"desc"}, "md5 = ?", new String[]{md5}, null, null, null);
        if (cursor.moveToNext()) {
            desc = cursor.getString(0);
        }
        cursor.close();
        return desc;
    }

    public void close(){
        db.close();
    }
}
