package com.example.projectfinal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PayActivity extends AppCompatActivity {

    private TextView tvAmount;
    private RadioGroup radioGroupPaymentMethods;
    private RadioButton radioBankTransfer, radioQRCode;
    private Button btnConfirmPayment;
    private ImageView ivQRCode;
    private TextView tvBank1, tvAccount1, tvBank2, tvAccount2, tvBank3, tvAccount3;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        tvAmount = findViewById(R.id.tv_amount);
        radioGroupPaymentMethods = findViewById(R.id.radioGroupPaymentMethods);
        radioBankTransfer = findViewById(R.id.radioBankTransfer);
        radioQRCode = findViewById(R.id.radioQRCode);
        ivQRCode = findViewById(R.id.iv_qr_code);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);
        tvBank1 = findViewById(R.id.tv_bank1);
        tvAccount1 = findViewById(R.id.tv_account1);
        tvBank2 = findViewById(R.id.tv_bank2);
        tvAccount2 = findViewById(R.id.tv_account2);
        tvBank3 = findViewById(R.id.tv_bank3);
        tvAccount3 = findViewById(R.id.tv_account3);

        tvAmount.setText("price: ฿Free");

        radioGroupPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioQRCode) {
                ivQRCode.setVisibility(View.VISIBLE);
                tvBank1.setVisibility(View.GONE);
                tvAccount1.setVisibility(View.GONE);
                tvBank2.setVisibility(View.GONE);
                tvAccount2.setVisibility(View.GONE);
                tvBank3.setVisibility(View.GONE);
                tvAccount3.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioBankTransfer) {
                ivQRCode.setVisibility(View.GONE);
                tvBank1.setVisibility(View.VISIBLE);
                tvAccount1.setVisibility(View.VISIBLE);
                tvBank2.setVisibility(View.VISIBLE);
                tvAccount2.setVisibility(View.VISIBLE);
                tvBank3.setVisibility(View.VISIBLE);
                tvAccount3.setVisibility(View.VISIBLE);
            }
        });

        btnConfirmPayment.setOnClickListener(v -> {
            int selectedId = radioGroupPaymentMethods.getCheckedRadioButtonId();

            if (selectedId == R.id.radioBankTransfer) {
                processBankTransfer();
                navigateToConfirmation();
            } else if (selectedId == R.id.radioQRCode) {
                processQRCodePayment();
                navigateToConfirmation();
            } else {
                showError("กรุณาเลือกช่องทางการจ่าย");
            }
        });
    }

    private void processBankTransfer() {
        showSuccess("การโอนผ่านธนาคารเสร็จสมบูรณ์");
    }

    private void processQRCodePayment() {
        showSuccess("การจ่ายเงินผ่าน QR Code เสร็จสมบูรณ์");
    }

    private void showSuccess(String message) {
        System.out.println(message);
    }

    private void showError(String message) {
        System.out.println(message);
    }

    private void navigateToConfirmation() {
        Intent intent = new Intent(PayActivity.this,ChatbotActivity.class);
        startActivity(intent);
        finish();
    }
}
