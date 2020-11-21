package com.example.pepe_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RecognitionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

//        button_camera /Intent camera = new Intent(RecognitionActivity.this, CameraActivity.class);
//        button_nfc / Intent nfc = new Intent(RecognitionActivity.this, NFCReadActivity.class);
        Button button_camera= (Button)findViewById(R.id.button_camera);
        button_camera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent_recognition = new Intent(RecognitionActivity.this, CameraActivity.class);
                startActivity(intent_recognition);
            }
        });
    }

}
