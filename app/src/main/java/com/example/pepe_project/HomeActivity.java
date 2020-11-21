package com.example.pepe_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button recognition= (Button)findViewById(R.id.button_recognition);
        recognition.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent_recognition = new Intent(HomeActivity.this, RecognitionActivity.class);
                startActivity(intent_recognition);
            }
        });

    }
}