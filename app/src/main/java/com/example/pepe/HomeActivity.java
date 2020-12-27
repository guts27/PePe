package com.example.pepe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.pepe.Recognition.Camera;
import com.example.pepe.Recognition.CameraActivity;
import com.example.pepe.UserConveinience.ConvenienceActivity;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class HomeActivity extends AppCompatActivity {
    private UserDBHelper dbHelper;
    Integer count = 0;
    Intent intent_recognition, intent_convenience, intent_rec;
    TextToSpeech tts;
    Float VolumeNo,Speed;
    String volume;
    Context context;
    Vibrator vibrator;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        context = getApplicationContext();

        vibrator = (Vibrator)getSystemService(context.VIBRATOR_SERVICE);

        intent_recognition = new Intent(HomeActivity.this, Camera.class);
        intent_convenience= new Intent(getApplicationContext(), ConvenienceActivity.class);
        intent_rec = new Intent(HomeActivity.this, CameraActivity.class);


                dbHelper = new UserDBHelper(this);
        Cursor cursor = dbHelper.readRecord();
        String result = "";
        if(cursor.getCount() == 0){
            dbHelper.insertRecord("112", "70","0.8");
            volume = "70";
            String temp = "0." + volume;
            VolumeNo = Float.valueOf(temp);
            String temp2 = "0.8";
            Speed = Float.valueOf(temp2);
        }else {
            while (cursor.moveToNext()) {
                String tmp1 = cursor.getString(cursor.getColumnIndexOrThrow(UserInfo.UserInfoEntry.COLUMN_PhoneNo));
                String tmp2 = cursor.getString(cursor.getColumnIndexOrThrow(UserInfo.UserInfoEntry.COLUMN_Volume));
                String tmp3 = cursor.getString(cursor.getColumnIndexOrThrow(UserInfo.UserInfoEntry.COLUMN_Speed));
                result += tmp1 + "/" + tmp2+"/"+tmp3;
            }
            String[] result2 = result.split("/");
            volume = result2[1];
            String temp = "0." + volume;
            VolumeNo = Float.valueOf(temp);
            String temp2 =result2[2];
            Speed = Float.valueOf(temp2);
        }
        cursor.close();
        PreferenceManager.setFloat(context, "Volume",VolumeNo);
        PreferenceManager.setFloat(context, "Speed", Speed);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(Speed);
                    //tts.speak("안녕하세요. 저는 페페 입니다. 원하시는 기능을 선택해주세요.", TextToSpeech.QUEUE_FLUSH, null);
                    tts.speak("안녕하세요. 저는 페페 입니다. 볼륨 높임 버튼을 누르면 어떠한 기능이 있는지 알 수 있습니다. 기능을 선택하신 후 볼륨 낮춤 버튼을 누르면 기능이 선택됩니다.", TextToSpeech.QUEUE_FLUSH, null);

                }
            }
        });

        Button recognition = (Button) findViewById(R.id.button_recognition);
        recognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(150);
                tts.speak("상품 및 메뉴인식으로 이동", TextToSpeech.QUEUE_FLUSH, null);
                if(Build.VERSION.SDK_INT >= 21) {
                    startActivity(intent_recognition);
                }else{
                    startActivity(intent_rec);
                }
            }
        });

        Button basket = (Button) findViewById(R.id.button_easytouch);
        basket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long[] pattern = {0,150,50,150};
                vibrator.vibrate(pattern, -1);
                tts.speak("케이비페이로 이동", TextToSpeech.QUEUE_FLUSH, null);
                payment();
            }
        });

        Button convenience = (Button) findViewById(R.id.button_convenience);
        convenience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.speak("사용자편의로 이동", TextToSpeech.QUEUE_FLUSH, null);
                startActivity(intent_convenience);

                long[] pattern = {0,150,50,150,50,150};
                vibrator.vibrate(pattern, -1);
            }
        });

        permission_check();


    }

///////////////////////////////

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                double temp = VolumeNo + 0.15;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*temp), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                button_selection();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*VolumeNo), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                count++;
                if(count == 4){
                    count = 1;
                }
                voice_guidance();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void voice_guidance(){

        switch (count){
            case 1:
                tts.speak("상품 및 메뉴 인식",TextToSpeech.QUEUE_FLUSH, null);
                break;
            case 2:
                tts.speak("케이비페이 열기",TextToSpeech.QUEUE_FLUSH, null);
                break;
            case 3:
                tts.speak("사용자 편의 기능",TextToSpeech.QUEUE_FLUSH, null);
                break;

        }

    }

    public void button_selection(){
        tts.speak("이동", TextToSpeech.QUEUE_FLUSH, null);

        switch (count) {
            case 1:
                vibrator.vibrate(150);
                if(Build.VERSION.SDK_INT >= 21) {
                    startActivity(intent_recognition);
                }else{
                    startActivity(intent_rec);
                }
                break;
            case 2:
                long[] pattern = {0,150,50,150};
                vibrator.vibrate(pattern, -1);
                payment();
                break;
            case 3:
                long[] pattern2 = {0,150,50,150,50,150};
                vibrator.vibrate(pattern2, -1);
                startActivity(intent_convenience);
                break;
        }
    }

    //////////////////////

    public void payment(){

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> apps;
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        apps = packageManager.queryIntentActivities(intent, 0);

        try{
            for(int i =0 ; i<apps.size(); i++){
                if(apps.get(i).activityInfo.packageName.startsWith("com.kbcard.cxh.appcard")){
                    //tts.speak("케이비페이가 열렸습니다.",TextToSpeech.QUEUE_FLUSH,null);
                    ComponentName componentName = new ComponentName("com.kbcard.cxh.appcard","com.kbcard.cxh.appcard.screen.payment.PaymentControlActivity");
                    Intent intent_open = new Intent(Intent.ACTION_MAIN);
                    intent_open.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent_open.setComponent(componentName);
                    startActivity(intent_open);

                }
            }
        }catch (Exception e){

        }
//        String packagename = "";
//        PackageManager pkgm = context.getPackageManager();
//        Intent intent = new Intent(Intent.ACTION_MAIN, null);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        List<ResolveInfo> AppInfos = pkgm.queryIntentActivities(intent, 0);
//        for (ResolveInfo info : AppInfos) {
//            ActivityInfo ai = info.activityInfo;
//            int resId = ai.applicationInfo.icon; //App. Icon
//        }
//            if(Arrays.asList(AppInfos).contains("kbcard")){
//                tts.speak("케이비페이가 열렸습니다.",TextToSpeech.QUEUE_FLUSH,null);
//                ComponentName componentName = new ComponentName("com.kbcard.cxh.appcard","com.kbcard.cxh.appcard.screen.payment.PaymentControlActivity");
//                Intent intent_open = new Intent(Intent.ACTION_MAIN);
//                intent_open.addCategory(Intent.CATEGORY_LAUNCHER);
//                intent_open.setComponent(componentName);
//                startActivity(intent_open);
//            }else{
//                tts.speak("케이비페이를 가지고 있지않습니다. 설치후 다시 시도해 주세요.", TextToSpeech.QUEUE_FLUSH, null);
//            }
//

    }

    /////////////////
    public void permission_check(){

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS}, 1);

            }
        }

                if (!checkServicesStatus()) {
                    showDialogForLocationServiceSetting();
                }else {
                    checkRunTimePermission();
                }


    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[3])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[4])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[5])) {
                    tts.speak("권한 거부되었습니다. 앱을 다시 실행하여 권한을 허용해주세요.",TextToSpeech.QUEUE_FLUSH, null);

                    Toast.makeText(HomeActivity.this, "권한 거부되었습니다. 앱을 다시 실행하여 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {
                    tts.speak("권한이 거부되었습니다. 설정(앱 정보)에서 권한을 허용해야 합니다. ",TextToSpeech.QUEUE_FLUSH, null);

                    Toast.makeText(HomeActivity.this, "권한이 거부되었습니다. 설정(앱 정보)에서 권한을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int hasCallPermission = ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.CALL_PHONE);
        int hasSMSPermission = ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.SEND_SMS);
        int hasCameraPermission = ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.CAMERA);
        int hasWritePermission = ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED&&
                hasCallPermission == PackageManager.PERMISSION_GRANTED&&
                hasSMSPermission == PackageManager.PERMISSION_GRANTED&&
                hasCameraPermission == PackageManager.PERMISSION_GRANTED&&
                hasWritePermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)





        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, REQUIRED_PERMISSIONS[0])&&
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, REQUIRED_PERMISSIONS[1])&&
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, REQUIRED_PERMISSIONS[2])&&
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, REQUIRED_PERMISSIONS[3])&&
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, REQUIRED_PERMISSIONS[4])&&
                    ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, REQUIRED_PERMISSIONS[5])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(HomeActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(HomeActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkServicesStatus()) {
                    if (checkServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;

            case RESULT_OK:
                checkRunTimePermission();
                break;

        }
    }

    public boolean checkServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

/////////



    //////////
    @Override
    protected void onResume() {
        super.onResume();
        VolumeNo = PreferenceManager.getFloat(context, "Volume");
        Speed = PreferenceManager.getFloat(context, "Speed");
        tts.setSpeechRate(Speed);
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*VolumeNo), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
    ///////////////
}