package com.example.projectfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private LinearLayout settingsLayout;
    private List<SettingItem> settingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        settingsLayout = findViewById(R.id.settingsLayout);

        settingItems = new ArrayList<>();
        settingItems.add(new SettingItem("บัญชีผู้ใช้", R.drawable.ic_launcher_background));
        settingItems.add(new SettingItem("ภาษา", R.drawable.ic_launcher_background));
        settingItems.add(new SettingItem("การแจ้งเตือน", R.drawable.ic_launcher_background));
        settingItems.add(new SettingItem("ข้อมูลเกี่ยวกับแอป", R.drawable.ic_launcher_background));
        settingItems.add(new SettingItem("Home", R.drawable.ic_launcher_background));
        settingItems.add(new SettingItem("ChatBot", R.drawable.ic_launcher_background));

        for (SettingItem item : settingItems) {
            addSettingItem(item);
        }
    }

    private void addSettingItem(SettingItem item) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_setting, settingsLayout, false);
        ImageView settingIcon = view.findViewById(R.id.settingIcon);
        TextView settingTitle = view.findViewById(R.id.settingTitle);
        settingTitle.setText(item.getTitle());
        settingIcon.setImageResource(item.getIconResId());
        view.setOnClickListener(v -> {
            switch (item.getTitle()) {
                case "บัญชีผู้ใช้":
                    Toast.makeText(SettingsActivity.this, "Work in progress", Toast.LENGTH_SHORT).show();
                    break;
                case "ภาษา":
                    Toast.makeText(SettingsActivity.this, "Work in progress",Toast.LENGTH_SHORT).show();
                    break;
                case "การแจ้งเตือน":
                    Toast.makeText(SettingsActivity.this,"Work in progress",Toast.LENGTH_SHORT).show();
                    break;
                case "ข้อมูลเกี่ยวกับแอป":
                    Toast.makeText(SettingsActivity.this,"Work in progress",Toast.LENGTH_SHORT).show();
                    break;
                case "Home":
                    startActivity(new Intent(SettingsActivity.this,HomeActivity.class));
                    break;
                case "ChatBot":
                    startActivity(new Intent(SettingsActivity.this,ChatbotActivity.class));
                    break;
                default:
                    break;
            }
        });
        settingsLayout.addView(view);
    }
}
