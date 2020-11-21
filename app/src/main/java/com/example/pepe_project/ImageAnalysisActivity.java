package com.example.pepe_project;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pepe_project.kakao.Ocr;
import com.example.pepe_project.kakao.Result;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.speech.tts.TextToSpeech;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class ImageAnalysisActivity extends AppCompatActivity {

    //다른 Activity에서 접근할 수 있도록 context 변수 선언
    public static Context context_main; // context 변수 선언
    final private static String TAG = "PEPE";

    //이미지 분석에 사용할 가장 최근에 촬영한 사진의 이름
    public String targetPhoto = ((CameraActivity)CameraActivity.context_main).mCurrentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageanalysis);
        Intent intent = getIntent();
        String imgs = intent.getStringExtra("img");

        Bitmap srcBmp = BitmapFactory.decodeFile(imgs);



        int iWidth   = 520;   // 축소시킬 너비

        int iHeight  = 520;   // 축소시킬 높이

        float fWidth  = srcBmp.getWidth();

        float fHeight = srcBmp.getHeight();



// 원하는 널이보다 클 경우의 설정

        if(fWidth > iWidth) {

            float mWidth = (float) (fWidth / 100);

            float fScale = (float) (iWidth / mWidth);

            fWidth *= (fScale / 100);

            fHeight *= (fScale / 100);

// 원하는 높이보다 클 경우의 설정

        }else if (fHeight > iHeight) {

            float mHeight = (float) (fHeight / 100);

            float fScale = (float) (iHeight / mHeight);

            fWidth *= (fScale / 100);

            fHeight *= (fScale / 100);

        }



        FileOutputStream fosObj = null;

        try {

            // 리사이즈 이미지 동일파일명 덮어 씌우기 작업

            Bitmap resizedBmp = Bitmap.createScaledBitmap(srcBmp, (int)fWidth, (int)fHeight, true);

            fosObj = new FileOutputStream(imgs);

            resizedBmp.compress(Bitmap.CompressFormat.JPEG, 100, fosObj);

        } catch (Exception e){

            ;

        } finally {

            try {
                fosObj.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                fosObj.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }




        Log.d("path",imgs);





//        Intent intent_c_next = new Intent(ImageAnalysisActivity.this, C_NextAcitivity.class);

        //다른 Activity에서 접근할 수 있도록 선언
        context_main = this;


        //GET_naver("https://dapi.kakao.com/v2/vision/text/ocr");
        POSTUpload("https://dapi.kakao.com/v2/vision/text/ocr",imgs);
    }



    private void POSTUpload(String url_upload, String filepath) {
        RequestParams params = new RequestParams();

        //File sdcard = Environment.getExternalStorageDirectory();
// to this path add a new directory path
        //File dir = new File(sdcard.getAbsolutePath() + "/download/");
// create this directory if not already created
// create the file in which we will write the contents
        File files = new File(filepath);
        Log.d("log",files.getAbsolutePath());
        Log.d("log:size = ", String.valueOf(files.length()));



        try {
            params.put("image", files);
            params.setForceMultipartEntityContentType(true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.setMaxConnections(100);
        client.addHeader("Authorization", "KakaoAK bdd55a1090f22ceea0b8fc66da6b3073"); // NCP Maps User Key ID

        client.post(url_upload, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("Succ", String.valueOf(statusCode));
                Log.d("Succ", String.valueOf(responseBody));

                Gson gson = new Gson();
                Ocr ocrresult = new Ocr();
                ocrresult = gson.fromJson(new String(responseBody), Ocr.class);

                List<Result> result = ocrresult.getResult();

                for(int i = 0 ; i < result.size();i++) {
                    Result res = new Result();
                    res = result.get(i);
                    List<String> recognitionWords = res.getRecognitionWords();
                    for (int j = 0; j < recognitionWords.size(); j++) {
                        String te = recognitionWords.get(j);
                        Log.d("result", te); //te에 결과 저장됨 이거 tts로 읽어주면 됩니다.
                    }
                }
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("Fail", String.valueOf(statusCode));

                Log.d("Fail", String.valueOf(responseBody));
            }
        });
    }
}
