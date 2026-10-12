package com.example.projectfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ChooseProgramActivity extends AppCompatActivity {

    private Button btProgram1, btProgram2, btProgram3, btProgram4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooseprogram);

        btProgram1 = findViewById(R.id.btProgram1);
        btProgram2 = findViewById(R.id.btProgram2);
        btProgram3 = findViewById(R.id.btProgram3);
        btProgram4 = findViewById(R.id.btProgram4);

        btProgram1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProgramPage(1);
            }
        });

        btProgram2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProgramPage(2);
            }
        });

        btProgram3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProgramPage(3);
            }
        });

        btProgram4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProgramPage(4);
            }
        });
    }

    private void openProgramPage(int programNumber) {
        Intent intent;
        switch (programNumber) {
            case 1:
                intent = new Intent(ChooseProgramActivity.this,PayActivity4.class);
                break;
            case 2:
                intent = new Intent(ChooseProgramActivity.this,PayActivity3.class);
                break;
            case 3:
                intent = new Intent(ChooseProgramActivity.this,PayActivity2.class);
                break;
            case 4:
                intent = new Intent(ChooseProgramActivity.this,PayActivity.class);
                break;
            default:
                return;
        }
        startActivity(intent);
    }
}
