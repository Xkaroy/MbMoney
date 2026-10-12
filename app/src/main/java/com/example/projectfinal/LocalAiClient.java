package com.example.projectfinal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class LocalAiClient {
    public static final class Turn {
        public final String role;
        public final String content;

        public Turn(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public interface Result {
        void onAnswer(String answer);
        void onFailure(String reason);
    }

    private final OkHttpClient http;

    public LocalAiClient() {
        this(new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS).callTimeout(190, TimeUnit.SECONDS).build());
    }

    LocalAiClient(OkHttpClient http) {
        this.http = http;
    }

    public Call ask(AiSettings settings, String systemPrompt, List<Turn> history, Result result) {
        try {
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            // Keep complete user/assistant pairs plus the newest user turn.
            int start = history.size() <= 17 ? 0 : history.size() - 17;
            for (int i = start; i < history.size(); i++) {
                Turn turn = history.get(i);
                messages.put(new JSONObject().put("role", turn.role).put("content", turn.content));
            }
            JSONObject payload = new JSONObject().put("model", settings.model)
                    .put("messages", messages).put("stream", false).put("think", false)
                    .put("options", new JSONObject().put("temperature", 0.3)
                            .put("num_predict", 768).put("num_ctx", 4096));
            Request request = new Request.Builder().url(settings.chatUrl)
                    .post(RequestBody.create(payload.toString(), MediaType.get("application/json; charset=utf-8")))
                    .build();
            Call call = http.newCall(request);
            call.enqueue(new Callback() {
                @Override public void onFailure(Call failedCall, IOException exception) {
                    if (!failedCall.isCanceled()) {
                        result.onFailure("เชื่อมต่อ AI ไม่ได้ ตรวจว่าคอมเปิด Ollama อยู่และที่อยู่ถูกต้อง "
                                + "ถ้าใช้ USB ให้เปิดการเชื่อมต่อด้วย adb reverse แล้วลองส่งอีกครั้ง");
                    }
                }

                @Override public void onResponse(Call completedCall, Response response) {
                    try (Response closeable = response) {
                        ResponseBody body = closeable.body();
                        if (!closeable.isSuccessful()) {
                            result.onFailure(closeable.code() == 404
                                    ? "ยังไม่มีโมเดลนี้ในเครื่อง ให้ดาวน์โหลดด้วย ollama pull " + settings.model
                                    : "Ollama ตอบกลับผิดพลาด (" + closeable.code() + ") กรุณาลองใหม่");
                            return;
                        }
                        if (body == null) throw new IOException("Empty response");
                        String answer = new JSONObject(body.string()).getJSONObject("message")
                                .getString("content").trim();
                        if (answer.isEmpty()) throw new IOException("Empty answer");
                        result.onAnswer(answer);
                    } catch (IOException | JSONException exception) {
                        result.onFailure("AI ส่งคำตอบที่อ่านไม่ได้ กรุณาลองส่งข้อความอีกครั้ง");
                    }
                }
            });
            return call;
        } catch (JSONException exception) {
            result.onFailure("เตรียมข้อความไม่สำเร็จ กรุณาลองใหม่");
            return null;
        }
    }
}
