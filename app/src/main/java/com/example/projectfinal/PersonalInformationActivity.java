package com.example.projectfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PersonalInformationActivity extends AppCompatActivity {

    private EditText etName, etBirthday, etEmail;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information);

        etName = findViewById(R.id.et_name);
        etBirthday = findViewById(R.id.et_birthday);
        etEmail = findViewById(R.id.et_email);
        btnNext = findViewById(R.id.next_page);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {

                    Intent intent = new Intent(PersonalInformationActivity.this, PersonalInformationActivity2.class);
                    intent.putExtra("name", etName.getText().toString());
                    intent.putExtra("birthday", etBirthday.getText().toString());
                    intent.putExtra("email", etEmail.getText().toString());
                    startActivity(intent);

                }
            }
        });
    }

    private boolean validateInput() {

        if (etName.getText().toString().isEmpty()) {
            etName.setError("Please enter your name");
            return false;
        }

        if (etBirthday.getText().toString().isEmpty()) {
            etBirthday.setError("Please enter your birthday");
            return false;
        }

        if (etEmail.getText().toString().isEmpty()) {
            etEmail.setError("Please enter your email");
            return false;
        }

        String email = etEmail.getText().toString();
        if (!email.contains("@") || !email.contains(".")) {
            etEmail.setError("Please enter a valid email address");
            return false;
        }

        return true;
    }
}
