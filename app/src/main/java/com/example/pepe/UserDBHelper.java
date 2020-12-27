package com.example.pepe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class UserDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "database";
    public static final int DATABASE_VERSION = 1;

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(UserInfo.UserInfoEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // 단순히 데이터를 삭제하고 다시 시작하는 정책이 적용될 경우
        sqLiteDatabase.execSQL(UserInfo.UserInfoEntry.SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }

    void insertRecord(String phoneno, String volume, String speed) {
        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserInfo.UserInfoEntry.COLUMN_PhoneNo, phoneno);
        values.put(UserInfo.UserInfoEntry.COLUMN_Volume, volume);
        values.put(UserInfo.UserInfoEntry.COLUMN_Speed, speed);

        db.insert(UserInfo.UserInfoEntry.TABLE_NAME, null, values);
    }

    public Cursor readRecord() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                UserInfo.UserInfoEntry.COLUMN_PhoneNo,
                UserInfo.UserInfoEntry.COLUMN_Volume,
                UserInfo.UserInfoEntry.COLUMN_Speed
        };

       String sortOrder = UserInfo.UserInfoEntry.COLUMN_Speed + " DESC";

        Cursor cursor = db.query(
                UserInfo.UserInfoEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        return cursor;
    }
}
