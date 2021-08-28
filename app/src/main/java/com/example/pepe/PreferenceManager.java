package com.example.pepe;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {


    public static final String PREFERENCES_NAME = "rebuild_preference";



        private static final String DEFAULT_VALUE_STRING = "";

        private static final boolean DEFAULT_VALUE_BOOLEAN = false;

        private static final int DEFAULT_VALUE_INT = -1;

        private static final long DEFAULT_VALUE_LONG = -1L;

        private static final float DEFAULT_VALUE_FLOAT = -1F;



        private static SharedPreferences getPreferences(Context context) {

            return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        }



        /**
         * String 값 저장
         * @param context
         * @param key
         * @param value
         */

        public static void setString(Context context, String key, String value) {

            SharedPreferences prefs = getPreferences(context);

            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(key, value);

            editor.commit();

        }

    /**
     * float 값 저장
     * @param context
     * @param key
     * @param value
     */

    public static void setFloat(Context context, String key, float value) {

        SharedPreferences prefs = getPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putFloat(key, value);

        editor.commit();

    }



        /**
         * String 값 로드
         * @param context
         * @param key
         * @return
         */

        public static String getString(Context context, String key) {

            SharedPreferences prefs = getPreferences(context);

            String value = prefs.getString(key, DEFAULT_VALUE_STRING);

            return value;

        }


    /**
     * float 값 로드
     * @param context
     * @param key
     * @return
     */

    public static float getFloat(Context context, String key) {

        SharedPreferences prefs = getPreferences(context);

        float value = prefs.getFloat(key, DEFAULT_VALUE_FLOAT);

        return value;

    }


        /**
         * 키 값 삭제
         * @param context
         * @param key
         */

        public static void removeKey(Context context, String key) {

            SharedPreferences prefs = getPreferences(context);

            SharedPreferences.Editor edit = prefs.edit();

            edit.remove(key);

            edit.commit();

        }



        /**
         * 모든 저장 데이터 삭제
         * @param context
         */

        public static void clear(Context context) {

            SharedPreferences prefs = getPreferences(context);

            SharedPreferences.Editor edit = prefs.edit();

            edit.clear();

            edit.commit();

        }
}
