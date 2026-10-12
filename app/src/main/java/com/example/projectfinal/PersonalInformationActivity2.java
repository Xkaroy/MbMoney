package com.example.projectfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PersonalInformationActivity2 extends AppCompatActivity {

    private EditText etId, etAddress, etPhone, etContact, etIncome;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_information2);

        etId = findViewById(R.id.et_id);
        etAddress = findViewById(R.id.et_address);
        etPhone = findViewById(R.id.et_phone);
        etContact = findViewById(R.id.et_contact);
        etIncome = findViewById(R.id.et_income);

        btnConfirm = findViewById(R.id.confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {

                    Toast.makeText(PersonalInformationActivity2.this, "Form submitted successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PersonalInformationActivity2.this, ChooseProgramActivity.class);
                    intent.putExtra("id", etId.getText().toString());
                    intent.putExtra("address", etAddress.getText().toString());
                    intent.putExtra("phone", etPhone.getText().toString());
                    intent.putExtra("contact", etContact.getText().toString());
                    intent.putExtra("income", etIncome.getText().toString());
                    startActivity(intent);

                }
            }
        });

        String name = getIntent().getStringExtra("name");
        String birthday = getIntent().getStringExtra("birthday");
        String email = getIntent().getStringExtra("email");

        Toast.makeText(this, "Received: " + name + ", " + birthday + ", " + email, Toast.LENGTH_SHORT).show();
    }

    private boolean validateInput() {
        // Check if the ID is entered
        if (etId.getText().toString().isEmpty()) {
            etId.setError("Please enter your ID number");
            return false;
        }

        if (etAddress.getText().toString().isEmpty()) {
            etAddress.setError("Please enter your address");
            return false;
        }

        if (etPhone.getText().toString().isEmpty()) {
            etPhone.setError("Please enter your phone number");
            return false;
        }

        if (etContact.getText().toString().isEmpty()) {
            etContact.setError("Please enter third-party contact info");
            return false;
        }

        if (etIncome.getText().toString().isEmpty()) {
            etIncome.setError("Please enter your monthly income");
            return false;
        }

        try {
            Double.parseDouble(etIncome.getText().toString());
        } catch (NumberFormatException e) {
            etIncome.setError("Please enter a valid income amount");
            return false;
        }

        return true;
    }
}
