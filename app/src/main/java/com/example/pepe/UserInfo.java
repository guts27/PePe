package com.example.pepe;

import android.provider.BaseColumns;

public class UserInfo {
    private UserInfo() {
    }

    public static class UserInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "UserInfo";
        public static final String COLUMN_PhoneNo = "phone";
        public static final String COLUMN_Volume = "volume";
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_PhoneNo + " TEXT," +
                        COLUMN_Volume + " TEXT)";
        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
