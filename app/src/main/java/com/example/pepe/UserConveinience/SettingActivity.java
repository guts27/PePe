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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        final Context context = getApplicationContext();

        final Vibrator vibrator = (Vibrator)getSystemService(context.VIBRATOR_SERVICE);

        VolumeNo = PreferenceManager.getFloat(context, "Volume");
        Speed = PreferenceManager.getFloat(context, "Speed");

        intent_setting = new Intent(SettingActivity.this, SettingActivity.class);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(Speed);
                }
            }
        });

        dbHelper = new UserDBHelper(this);
        db = dbHelper.getWritableDatabase();

        String result = "";
        Cursor cursor = dbHelper.readRecord();
        while (cursor.moveToNext()) {
            Log.d("dfdfdfdfdf001", String.valueOf(cursor.getCount()));
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
        Button phnstr = (Button)findViewById(R.id.button_phnstr);
        Button plus = (Button)findViewById(R.id.button_plus);
        Button minus = (Button)findViewById(R.id.button_minus);
        Button volstr = (Button)findViewById(R.id.button_volstr);
        Button plus2 = (Button)findViewById(R.id.button_plus2);
        Button minus2 = (Button)findViewById(R.id.button_minus2);
        Button spestr = (Button)findViewById(R.id.button_spestr);

        phnstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = phone.getText().toString();
                Log.d("ddd", temp);
                if(temp!= null){
                    if(temp.length() == 11){
                        db.execSQL("update UserInfo set phone = "+"'"+temp+"'");
                        vibrator.vibrate(1000);
                        tts.speak("저장되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                        Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_LONG);
                    }else{
                        tts.speak("잘못 입력하셨습니다.", TextToSpeech.QUEUE_FLUSH, null);
                        Toast.makeText(getApplicationContext(), "잘못 입력하셨습니다.", Toast.LENGTH_LONG);
                    }
                }else{
                    tts.speak("입력된 내용이 없습니다.", TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(getApplicationContext(), "입력된 내용이 없습니다.", Toast.LENGTH_LONG);
                }
            }
        });

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

        volstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = volume.getText().toString();
                db.execSQL("update UserInfo set volume = "+"'"+temp+"'");
                long[] pattern = {0,700,200,700};
                vibrator.vibrate(pattern, -1);
                tts.speak("저장되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
                temp = "0." + temp;
                PreferenceManager.setFloat(context,"Volume", Float.parseFloat(temp));
                Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_LONG);
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

        spestr.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String temp = speed.getText().toString();
            db.execSQL("update UserInfo set speed = "+"'"+temp+"'");
            long[] pattern = {0,700,200,700,200,700};
            vibrator.vibrate(pattern, -1);
            tts.speak("저장되었습니다.", TextToSpeech.QUEUE_FLUSH, null);
            PreferenceManager.setFloat(context,"Speed", Float.parseFloat(temp));
            Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_LONG);
        }
    });

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                db.close();
                SettingActivity.this.finish();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    //////////
    @Override
    protected void onResume() {
        super.onResume();
        if(exe ==1) {
            VolumeNo = PreferenceManager.getFloat(context, "Volume");
            Log.d("resume", String.valueOf(VolumeNo));
        }else{
            exe = 1;
        }
    }
    ///////////////
}
