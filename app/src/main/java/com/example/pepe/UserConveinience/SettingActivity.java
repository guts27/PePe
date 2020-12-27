package com.example.pepe.UserConveinience;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pepe.PreferenceManager;
import com.example.pepe.R;
import com.example.pepe.UserDBHelper;
import com.example.pepe.UserInfo;

import java.util.Locale;

public class SettingActivity extends AppCompatActivity {
    private UserDBHelper dbHelper;

    Integer exe = 0;
    TextToSpeech tts;
    Float VolumeNo;
    Intent intent_setting;
    SQLiteDatabase db;
    Context context;
    Float Speed;
    AudioManager audioManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        final Context context = getApplicationContext();

        final Vibrator vibrator = (Vibrator)getSystemService(context.VIBRATOR_SERVICE);

        VolumeNo = PreferenceManager.getFloat(context, "Volume");
        Speed = PreferenceManager.getFloat(context, "Speed");

        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*VolumeNo), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        intent_setting = new Intent(SettingActivity.this, SettingActivity.class);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(Speed);
                    tts.speak("설정입니다.", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        dbHelper = new UserDBHelper(this);
        db = dbHelper.getWritableDatabase();

        String result = "";
        Cursor cursor = dbHelper.readRecord();
        while (cursor.moveToNext()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow(UserInfo.UserInfoEntry.COLUMN_PhoneNo))+"/";
            result = result + cursor.getString(cursor.getColumnIndexOrThrow(UserInfo.UserInfoEntry.COLUMN_Volume))+"/";
            result = result + cursor.getString(cursor.getColumnIndexOrThrow(UserInfo.UserInfoEntry.COLUMN_Speed));
            }
        Log.d("ghhh", result);
            String[] result2 = result.split("/");

        final EditText phone = (EditText)findViewById(R.id.editTextPhone);
        final TextView volume = (TextView)findViewById(R.id.textView_volume);
        volume.setText(result2[1]);
        final TextView speed = (TextView)findViewById(R.id.textView_speed);
        speed.setText(result2[2]);
        Button plus = (Button)findViewById(R.id.button_plus);
        Button minus = (Button)findViewById(R.id.button_minus);
        Button plus2 = (Button)findViewById(R.id.button_plus2);
        Button minus2 = (Button)findViewById(R.id.button_minus2);
        Button storage = (Button)findViewById(R.id.button_str);


        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer volNum = Integer.valueOf(volume.getText().toString());
                if(volNum != 100){
                    volNum = volNum + 10;
                    String tmp = String.valueOf(volNum);
                    volume.setText(tmp);

                }
            }
        });

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer volNum = Integer.valueOf(volume.getText().toString());
                if(volNum != 0){
                    volNum = volNum - 10;
                    String tmp = String.valueOf(volNum);
                    volume.setText(tmp);
                }
            }
        });


    plus2.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            double speNum = Float.valueOf(speed.getText().toString());
            Log.d("00", String.valueOf(speNum));
            if(speNum < 2.0){
                speNum = speNum + 0.1f;
                String tmp = String.format("%.1f", speNum);
                speed.setText(tmp);
            }
        }
    });

        minus2.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            double speNum = Float.valueOf(speed.getText().toString());
            Log.d("00", String.valueOf(speNum));
            if(speNum > 0.5){
                speNum = speNum - 0.1f;
                String tmp = String.format("%.1f", speNum);
                speed.setText(tmp);
            }
        }
    });

        storage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = phone.getText().toString();
                if(temp!= null){
                    if(temp.length() == 11){
                        db.execSQL("update UserInfo set phone = "+"'"+temp+"'");
                        vibrator.vibrate(1000);
                    }else{
                        tts.speak("잘못 입력하셨습니다.", TextToSpeech.QUEUE_FLUSH, null);
                        Toast.makeText(getApplicationContext(), "잘못 입력하셨습니다.", Toast.LENGTH_LONG);
                    }
                }else{
                    tts.speak("입력된 내용이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(getApplicationContext(), "입력된 내용이 없습니다.", Toast.LENGTH_LONG);
                }

                String temp1 = volume.getText().toString();
                db.execSQL("update UserInfo set volume = "+"'"+temp1+"'");
                temp1 = "0." + temp1;
                PreferenceManager.setFloat(context,"Volume", Float.parseFloat(temp1));
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*VolumeNo), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

                String temp2 = speed.getText().toString();
                db.execSQL("update UserInfo set speed = "+"'"+temp2+"'");
                PreferenceManager.setFloat(context,"Speed", Float.parseFloat(temp2));
                tts.setSpeechRate(Speed);

                tts.speak("저장되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_LONG);
                long[] pattern1 = {0,300,100,300,100,300};
                vibrator.vibrate(pattern1, -1);
                Intent intent_conv = new Intent(SettingActivity.this, ConvenienceActivity.class);
            }
        });

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                db.close();
                SettingActivity.this.finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //////////
    @Override
    protected void onResume() {
        super.onResume();
        if(exe ==1) {
            VolumeNo = PreferenceManager.getFloat(context, "Volume");
            tts.setSpeechRate(Speed);
        }else{
            exe = 1;
        }
    }
    ///////////////
}
