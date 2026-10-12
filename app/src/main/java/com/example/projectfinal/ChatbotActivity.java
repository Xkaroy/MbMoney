package com.example.projectfinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;

public class ChatbotActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView aiStatus;
    private EditText messageEditText;
    private ImageButton sendButton;
    private final List<Message> messageList = new ArrayList<>();
    private final List<LocalAiClient.Turn> history = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private final LocalAiClient aiClient = new LocalAiClient();
    private Call activeCall;
    private boolean busy;
    private String pendingInput = "";
    private String monthlyExpenses = "";
    private String savingsGoal = "";
    private String duration = "";
    private int step;
    private String prompt;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        recyclerView = findViewById(R.id.recycler_view);
        aiStatus = findViewById(R.id.ai_status);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);
        findViewById(R.id.welcome_text).setVisibility(View.GONE);
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        prompt = readPrompt();
        SharedPreferences profile = getSharedPreferences("UserData", MODE_PRIVATE);
        monthlyExpenses = profile.getString("monthlyExpenses", "");
        savingsGoal = profile.getString("savingsGoal", "");
        duration = profile.getString("duration", "");
        if (hasValidProfile()) step = 4;
        if (savedInstanceState != null) {
            monthlyExpenses = savedInstanceState.getString("expenses", monthlyExpenses);
            savingsGoal = savedInstanceState.getString("goal", savingsGoal);
            duration = savedInstanceState.getString("days", duration);
            step = savedInstanceState.getInt("step", step);
            ArrayList<String> roles = savedInstanceState.getStringArrayList("roles");
            ArrayList<String> contents = savedInstanceState.getStringArrayList("contents");
            if (roles != null && contents != null && roles.size() == contents.size()) {
                for (int i = 0; i < roles.size(); i++) history.add(new LocalAiClient.Turn(roles.get(i), contents.get(i)));
            }
            messageEditText.setText(savedInstanceState.getString("draft", ""));
        }
        if (step == 4 && hasValidProfile()) {
            addToChat(summary(), Message.SENT_BY_BOT);
            for (LocalAiClient.Turn turn : history) {
                addToChat(turn.content, turn.role.equals("user") ? Message.SENT_BY_ME : Message.SENT_BY_BOT);
            }
            if (history.isEmpty()) addToChat("พิมพ์ ‘ช่วยวางแผนออมเงิน’ เพื่อเริ่มคุยกับ AI ฟรี หรือกดตั้งค่า AI เพื่อเชื่อมต่อคอม", Message.SENT_BY_BOT);
        } else {
            step = Math.min(step, 2);
            askQuestion();
        }
        sendButton.setOnClickListener(view -> sendMessage());
        findViewById(R.id.ai_settings_button).setOnClickListener(view -> showAiSettings());
        findViewById(R.id.homeButton).setOnClickListener(view -> {
            if (!hasValidProfile()) {
                Toast.makeText(this, "กรอกข้อมูลการออมให้ครบก่อนดูกราฟ", Toast.LENGTH_SHORT).show();
                return;
            }
            saveProfile();
            startActivity(new Intent(this, ProcessActivity.class));
        });
        updateStatus("AI ฟรีในเครื่อง • ต้องเปิด Ollama บนคอม");
    }

    private void sendMessage() {
        if (busy) return;
        String input = messageEditText.getText().toString().trim();
        if (input.isEmpty()) return;
        if (input.length() > 2000) {
            messageEditText.setError("ส่งข้อความไม่เกิน 2,000 ตัวอักษรต่อครั้ง");
            return;
        }
        if (step < 4) {
            try {
                if (step == 0) monthlyExpenses = SavingsProfile.validateAmount(input, true);
                else if (step == 1) savingsGoal = SavingsProfile.validateAmount(input, false);
                else duration = SavingsProfile.validateDays(input);
            } catch (IllegalArgumentException exception) {
                messageEditText.setError(exception.getMessage());
                return;
            }
            addToChat(input, Message.SENT_BY_ME);
            messageEditText.setText("");
            step++;
            if (step == 3) {
                step = 4;
                saveProfile();
                addToChat(summary(), Message.SENT_BY_BOT);
                requestAi("ช่วยวางแผนออมเงินตามข้อมูลนี้ พร้อมถามข้อมูลที่จำเป็นเพิ่มเติม");
            } else askQuestion();
        } else requestAi(input);
    }

    private void requestAi(String input) {
        AiSettings settings;
        try {
            settings = settings();
            if (prompt == null) {
                updateStatus("อ่านคำสั่งของ AI ไม่สำเร็จ กรุณาเปิดแอปใหม่");
                return;
            }
        } catch (IllegalArgumentException exception) {
            updateStatus(exception.getMessage());
            showAiSettings();
            return;
        }
        addToChat(input, Message.SENT_BY_ME);
        pendingInput = input;
        messageEditText.setText("");
        setBusy(true);
        updateStatus("AI กำลังตอบ… ครั้งแรกอาจใช้เวลารอโหลดโมเดล");
        List<LocalAiClient.Turn> requestHistory = new ArrayList<>(history);
        requestHistory.add(new LocalAiClient.Turn("user", input));
        activeCall = aiClient.ask(settings, prompt + "\n\n" + SavingsProfile.context(monthlyExpenses, savingsGoal, duration),
                requestHistory, new LocalAiClient.Result() {
                    @Override public void onAnswer(String answer) {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            history.add(new LocalAiClient.Turn("user", input));
                            history.add(new LocalAiClient.Turn("assistant", answer));
                            while (history.size() > 16) {
                                history.remove(0);
                                history.remove(0);
                            }
                            addToChat(answer, Message.SENT_BY_BOT);
                            pendingInput = "";
                            setBusy(false);
                            updateStatus("ตอบโดย AI ในคอม • ไม่มีค่า API");
                        });
                    }
                    @Override public void onFailure(String reason) {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            setBusy(false);
                            updateStatus(reason);
                            messageEditText.setText(input);
                            messageEditText.setSelection(messageEditText.length());
                            pendingInput = "";
                        });
                    }
                });
    }

    private AiSettings settings() {
        SharedPreferences preferences = getSharedPreferences("LocalAi", MODE_PRIVATE);
        return new AiSettings(preferences.getString("server", AiSettings.DEFAULT_SERVER), preferences.getString("model", AiSettings.DEFAULT_MODEL));
    }

    private void showAiSettings() {
        if (busy) return;
        SharedPreferences preferences = getSharedPreferences("LocalAi", MODE_PRIVATE);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, padding, padding, padding);
        TextView explanation = new TextView(this);
        explanation.setText("ใช้ Ollama ฟรี ไม่ต้องมี API key\nUSB: http://127.0.0.1:11435 (adb reverse)\nEmulator: http://10.0.2.2:11435\nWi-Fi: ใส่ IP คอมและเปิด Ollama แบบ LAN\nข้อมูลแชตจะส่งไปยังคอมที่คุณระบุ");
        form.addView(explanation);
        EditText server = new EditText(this);
        server.setSingleLine(true);
        server.setHint("ที่อยู่คอมสำหรับ AI");
        server.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        server.setText(preferences.getString("server", AiSettings.DEFAULT_SERVER));
        form.addView(server);
        EditText model = new EditText(this);
        model.setSingleLine(true);
        model.setHint("ชื่อโมเดลที่ดาวน์โหลดไว้");
        model.setText(preferences.getString("model", AiSettings.DEFAULT_MODEL));
        form.addView(model);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("ตั้งค่า AI ฟรี").setView(form)
                .setPositiveButton("บันทึก", null).setNegativeButton("ยกเลิก", null)
                .setNeutralButton("กรอกข้อมูลออมใหม่", (ignored, which) -> {
                    monthlyExpenses = "";
                    savingsGoal = "";
                    duration = "";
                    step = 0;
                    history.clear();
                    messageList.clear();
                    messageAdapter.notifyDataSetChanged();
                    messageEditText.setText("");
                    getSharedPreferences("UserData", MODE_PRIVATE).edit().remove("monthlyExpenses").remove("savingsGoal").remove("duration").apply();
                    askQuestion();
                }).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try {
                AiSettings validated = new AiSettings(server.getText().toString(), model.getText().toString());
                preferences.edit().putString("server", validated.chatUrl.newBuilder().encodedPath("/").build().toString())
                        .putString("model", validated.model).apply();
                updateStatus("บันทึกแล้ว • ส่งข้อความเพื่อคุยกับ AI");
                dialog.dismiss();
            } catch (IllegalArgumentException exception) {
                server.setError(exception.getMessage());
            }
        }));
        dialog.show();
    }

    private void askQuestion() {
        String[] questions = {"ค่าใช้จ่ายต่อเดือนของคุณกี่บาท? (กรอก 0 ได้)", "เป้าหมายเงินออมของคุณกี่บาท?", "ต้องการออมให้ถึงเป้าหมายภายในกี่วัน?"};
        addToChat(questions[step], Message.SENT_BY_BOT);
    }

    private boolean hasValidProfile() {
        try {
            SavingsProfile.context(monthlyExpenses, savingsGoal, duration);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String summary() {
        return "สรุปข้อมูลของคุณ\nค่าใช้จ่ายต่อเดือน: " + monthlyExpenses + " บาท\nเป้าหมายเงินออม: " + savingsGoal + " บาท\nระยะเวลา: " + duration + " วัน";
    }

    private void saveProfile() {
        getSharedPreferences("UserData", MODE_PRIVATE).edit().putString("monthlyExpenses", monthlyExpenses)
                .putString("savingsGoal", savingsGoal).putString("duration", duration).apply();
    }

    private String readPrompt() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.mbmoney_system_prompt), StandardCharsets.UTF_8))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) text.append(line).append('\n');
            return text.toString();
        } catch (IOException exception) {
            return null;
        }
    }

    private void setBusy(boolean value) {
        busy = value;
        sendButton.setEnabled(!value);
        messageEditText.setEnabled(!value);
        findViewById(R.id.ai_settings_button).setEnabled(!value);
    }

    private void updateStatus(String text) { aiStatus.setText(text); }

    private void addToChat(String text, String sender) {
        messageList.add(new Message(text, sender));
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putString("expenses", monthlyExpenses);
        state.putString("goal", savingsGoal);
        state.putString("days", duration);
        state.putInt("step", step);
        state.putString("draft", busy ? pendingInput : messageEditText.getText().toString());
        ArrayList<String> roles = new ArrayList<>();
        ArrayList<String> contents = new ArrayList<>();
        for (LocalAiClient.Turn turn : history) { roles.add(turn.role); contents.add(turn.content); }
        state.putStringArrayList("roles", roles);
        state.putStringArrayList("contents", contents);
    }

    @Override protected void onDestroy() {
        if (activeCall != null) activeCall.cancel();
        super.onDestroy();
    }
}
