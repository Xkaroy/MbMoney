package com.example.projectfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class MainActivity_a extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;

    private EditText usernameEditText, passwordEditText;
    private TextView usernameErrorTextView, passwordErrorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_a);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        usernameErrorTextView = findViewById(R.id.usernameErrorTextView);
        passwordErrorTextView = findViewById(R.id.passwordErrorTextView);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            usernameErrorTextView.setVisibility(View.GONE);
            passwordErrorTextView.setVisibility(View.GONE);

            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            boolean isValid = true;

            if (username.isEmpty()) {
                usernameErrorTextView.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (password.isEmpty()) {
                passwordErrorTextView.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (isValid) {
                Intent intent = new Intent(MainActivity_a.this, PersonalInformationActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity_a.this, "กรุณาใส่ข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show();
            }
        });

        TextView forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity_a.this, "Work in progress", Toast.LENGTH_SHORT).show();
            }
        });

        Button googleLoginButton = findViewById(R.id.googleLoginButton);
        googleLoginButton.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 101);
        });

        Button facebookLoginButton = findViewById(R.id.facebookLoginButton);
        facebookLoginButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity_a.this, "Working in Progress", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult();
            if (account != null) {
                Intent intent = new Intent(MainActivity_a.this,PersonalInformationActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Google Sign-In failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
