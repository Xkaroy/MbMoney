package com.example.projectfinal;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mediaPlayer = MediaPlayer.create(this, R.raw.eaglescream);
        mediaPlayer.start();
        Button homeButton = findViewById(R.id.button1);
        Button chatButton = findViewById(R.id.button2);
        Button processButton = findViewById(R.id.button3);
        Button settingButton = findViewById(R.id.button4);


        homeButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this,HomeActivity.class);
            startActivity(intent);
        });

        chatButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this,ChatbotActivity.class);
            startActivity(intent);
        });

        processButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this,ProcessActivity.class);
            startActivity(intent);
        });

        settingButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this,SettingsActivity.class);
            startActivity(intent);
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
