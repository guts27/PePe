package com.example.pepe_project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CameraActivity extends AppCompatActivity{ //implements View.OnClickListener{
    TextToSpeech tts;
    final static int TAKE_PICTURE = 1;
    final private static String TAG = "PEPE";

    //다른 Activity에서 접근할 변수 선언
    public static Context context_main; // context 변수 선언

    //경로 변수와 요청변수 생성
    final static int REQUEST_TAKE_PHOTO = 1;
    public String mCurrentPhotoPath;

    //버튼 연결
    Button button_camera; //카메라 버튼 누르면 카메라 불러오고 촬영함, 저장 누르는거 자동화 안됨.
    ImageView iv_photo;
    Button test; //저장하고 나와서 test버튼 누르면 OCR기능 수행하는 ImageAnalysisActivity로 넘어가서 문자 뽑아옴. 이 부분 다른 버튼(볼륨 업 키)등으로 대체해야할듯
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //다른 Activity와 변수를 공유하기 위한 것
        context_main = this;

        //버튼에 레이아웃 연결
        button_camera = (Button)findViewById(R.id.camera_on_button);
        iv_photo = (ImageView)findViewById(R.id.iv_photo);

        test = (Button)findViewById(R.id.testbutton);

        //저장하고 나와서 test버튼 누르면 OCR기능 수행하는 ImageAnalysisActivity로 넘어가서 문자 뽑아옴. 이 부분 다른 버튼(볼륨 업 키)등으로 대체해야할듯
        test.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                Intent intent_recognition = new Intent(CameraActivity.this, ImageAnalysisActivity.class);

                intent_recognition.putExtra("img", mCurrentPhotoPath);
                startActivity(intent_recognition);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
                Log.d(TAG, "권한 설정 완료");
            } else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        button_camera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.camera_on_button:
                        dispatchTakePictureIntent();
                        break;
                }
            }
        });

        tts = new TextToSpeech(CameraActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(0.7f);
                    tts.speak("버튼을 누르면 카메라 기능이 수행됩니다. 확인하고 싶은 정보를 촬영해주세요",TextToSpeech.QUEUE_FLUSH,null);
                    //        tts.speak("상품을 촬영해주세요",TextToSpeech.QUEUE_FLUSH,null);
                    //이걸 사용하면 말하고, QUEUE_FLUSH는 말하는 도중 다른 음성메세지가 시작되면 끊고 말하는 옵션임
                }
            }
        });
    }

    //권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionResult");
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
    Log.d(TAG, "Permission : " + permissions[0] + "was " + grantResults[0]);
        }
    }

    //카메라로 촬영한 영상을 가져오는 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        try{
            switch (requestCode){
                case  REQUEST_TAKE_PHOTO: {
                    if(resultCode == RESULT_OK){

                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap;
                        if(Build.VERSION.SDK_INT>=29){
                            ImageDecoder.Source source=ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));
                            try {
                                bitmap = ImageDecoder.decodeBitmap(source);
                                if (bitmap != null) { iv_photo.setImageBitmap(bitmap);}
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        } else{
                            try{
                                bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
                                if(bitmap != null){ iv_photo.setImageBitmap(bitmap); }
                            }catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception error){
            error.printStackTrace();
        }
    }

    //사진 촬영 후 썸네일만 띄워줌. 이미지 파일로 저장해야 함
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        String imageFileName="JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );


        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // 카메라 인텐트 실행하는 부분
    private  void dispatchTakePictureIntent(){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;


            try { photoFile = createImageFile();}
            catch (IOException ex){}
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.pepe_project.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}