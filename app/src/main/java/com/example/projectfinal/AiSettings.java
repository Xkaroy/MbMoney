package com.example.projectfinal;

import okhttp3.HttpUrl;

/** Only local Ollama servers and local models are accepted: no paid/cloud API. */
public final class AiSettings {
    public static final String DEFAULT_SERVER = "http://127.0.0.1:11435";
    public static final String DEFAULT_MODEL = "qwen2.5:7b";
    public final HttpUrl chatUrl;
    public final String model;

    public AiSettings(String server, String model) {
        HttpUrl url = HttpUrl.parse(server.trim());
        if (url == null || !url.scheme().equals("http") || !isLocalHost(url.host())
                || !url.username().isEmpty() || !url.password().isEmpty()
                || !url.encodedPath().equals("/") || url.query() != null || url.fragment() != null) {
            throw new IllegalArgumentException("ใช้ที่อยู่ Ollama ในเครื่องหรือเครือข่ายบ้าน เช่น http://192.168.1.10:11435");
        }
        String localModel = model.trim();
        if (!localModel.matches("[A-Za-z0-9][A-Za-z0-9._:/-]{0,79}")
                || localModel.toLowerCase(java.util.Locale.ROOT).contains("cloud")) {
            throw new IllegalArgumentException("เลือกโมเดลที่ดาวน์โหลดไว้ใน Ollama เช่น qwen2.5:7b โดยไม่ใช้โมเดล cloud");
        }
        this.chatUrl = url.newBuilder().addPathSegments("api/chat").build();
        this.model = localModel;
    }

    private static boolean isLocalHost(String host) {
        if (host.equals("localhost") || host.equals("::1")) return true;
        String[] octets = host.split("\\.");
        if (octets.length != 4) return false;
        int[] address = new int[4];
        try {
            for (int i = 0; i < 4; i++) {
                address[i] = Integer.parseInt(octets[i]);
                if (address[i] < 0 || address[i] > 255) return false;
            }
        } catch (NumberFormatException exception) {
            return false;
        }
        return address[0] == 127 || address[0] == 10
                || (address[0] == 192 && address[1] == 168)
                || (address[0] == 172 && address[1] >= 16 && address[1] <= 31);
    }
}
