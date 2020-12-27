package com.example.pepe.Recognition;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.pepe.HomeActivity;
import com.example.pepe.PreferenceManager;
import com.example.pepe.R;
import com.example.pepe.UserConveinience.ConvenienceActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Camera extends AppCompatActivity {

    private Button btnCapture;
    private TextureView textureView;
    //Check state orientation of output image
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraID;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;

    Intent intent_recognition;

    //save to FILE
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    ///////////////////
    TextToSpeech tts;
    Float VolumeNo,Speed;
    Context context;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        context = getApplicationContext();
        vibrator = (Vibrator)getSystemService(context.VIBRATOR_SERVICE);
        VolumeNo = PreferenceManager.getFloat(context, "Volume");
        Speed = PreferenceManager.getFloat(context,"Speed");

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                    tts.setSpeechRate(Speed);
                    //tts.speak("안녕하세요. 저는 페페 입니다. 원하시는 기능을 선택해주세요.", TextToSpeech.QUEUE_FLUSH, null);
                    tts.speak("카메라가 켜졌습니다. 볼륨 높임을 누르면 사진이 촬영 됩니다.", TextToSpeech.QUEUE_FLUSH, null);

                }
            }
        });

        intent_recognition = new Intent(Camera.this, ImageAnalysisActivity.class);
        textureView = (TextureView)findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        btnCapture = (Button)findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(150);
                tts.speak("촬영되었습니다.",TextToSpeech.QUEUE_FLUSH,null);
                takePicture();
            }
        });

    }

    private void takePicture() {
        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            //Capture image with custom size
//            int width = 1920;
//            int height = 1080;
            int width = 640;
            int height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            file = new File(Environment.getExternalStorageDirectory() + "/" + UUID.randomUUID().toString() + ".jpg");
            intent_recognition.putExtra("imgssss", file.toString());
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                        e.printStackTrace();

                    } finally {
                        {
                            if (image != null)
                                image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException{
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                        startActivity(intent_recognition);
                        Camera.this.finish();

                    }finally {
                        if(outputStream!=null)
                            outputStream.close();
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureLisner = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(Camera.this, "Saves "+file, Toast.LENGTH_SHORT).show();
                    //createCameraPreview();


                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(), captureLisner, mBackgroundHandler);
                    }catch (CameraAccessException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);
        }catch (CameraAccessException e)
        {
            e.printStackTrace();
        }

    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(1000, 600);
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(Camera.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }


    private void updatePreview(){
        if(cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        //captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);

        }catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraID = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Check realtime permission if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CAMERA_PERMISSION);
                return;

            }
            manager.openCamera(cameraID,stateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"you can't use camera withiut permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
        VolumeNo = PreferenceManager.getFloat(context, "Volume");
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*VolumeNo), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        Speed = PreferenceManager.getFloat(context, "Speed");
        tts.setSpeechRate(Speed);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /////////////////////////////////////////
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                vibrator.vibrate(150);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*VolumeNo), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                takePicture();
                tts.speak("촬영되었습니다.",TextToSpeech.QUEUE_FLUSH,null);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                double temp = VolumeNo + 0.15;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*temp), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                vibrator.vibrate(150);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*VolumeNo), AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                takePicture();
                tts.speak("촬영되었습니다.",TextToSpeech.QUEUE_FLUSH,null);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

