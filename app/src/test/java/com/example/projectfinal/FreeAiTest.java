package com.example.projectfinal;

import org.json.JSONObject;
import org.junit.Test;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import static org.junit.Assert.*;

public class FreeAiTest {
    @Test public void referenceAmountsRoundUpWithoutInventingIncome() {
        String context = SavingsProfile.context("12,000", "1000", "3");
        assertTrue(context.contains("333.34 บาท"));
        assertTrue(context.contains("10000.20 บาท"));
        assertTrue(context.contains("ยังไม่ทราบรายได้"));
        assertEquals("12000", SavingsProfile.validateAmount("12,000.00", true));
        assertEquals("0", SavingsProfile.validateAmount("0", true));
    }
    @Test public void rejectsInputsThatWouldCorruptChartOrDivideByZero() {
        for (String amount : Arrays.asList("NaN", "Infinity", "-10", "1e309", "1.234", "100000001", "")) {
            assertThrows(IllegalArgumentException.class, () -> SavingsProfile.validateAmount(amount, true));
        }
        assertThrows(IllegalArgumentException.class, () -> SavingsProfile.validateAmount("0", false));
        for (String days : Arrays.asList("0", "-1", "1.5", "36501", "NaN", "")) {
            assertThrows(IllegalArgumentException.class, () -> SavingsProfile.validateDays(days));
        }
    }
    @Test public void acceptsLocalAddressesAndRejectsCloudOrEmbeddedCredentials() {
        for (String address : Arrays.asList("http://127.0.0.1:11435", "http://localhost:11435",
                "http://10.0.2.2:11435", "http://192.168.1.10:11435", "http://172.16.0.1:11435")) {
            assertEquals("/api/chat", new AiSettings(address, "qwen2.5:7b").chatUrl.encodedPath());
        }
        for (String address : Arrays.asList("https://api.openai.com", "https://ollama.com",
                "http://8.8.8.8", "http://172.32.0.1", "http://user:pass@localhost", "http://localhost/path")) {
            assertThrows(IllegalArgumentException.class, () -> new AiSettings(address, "qwen2.5:7b"));
        }
        assertThrows(IllegalArgumentException.class, () -> new AiSettings(AiSettings.DEFAULT_SERVER, "qwen3:cloud"));
    }
    @Test public void sendsSystemPromptAndHistoryAndReadsProtocolAnswer() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setBody("{\"message\":{\"content\":\"ออมวันละ 100 บาท\"}}"));
            CountDownLatch done = new CountDownLatch(1);
            AtomicReference<String> answer = new AtomicReference<>();
            new LocalAiClient().ask(new AiSettings(server.url("/").toString(), "qwen2.5:7b"), "system-test",
                    Arrays.asList(new LocalAiClient.Turn("user", "เป้าหมาย 3000 บาทใน 30 วัน")), result(done, answer));
            assertTrue(done.await(5, TimeUnit.SECONDS));
            assertEquals("ออมวันละ 100 บาท", answer.get());
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertNotNull(request);
            assertEquals("/api/chat", request.getPath());
            assertNull(request.getHeader("Authorization"));
            JSONObject payload = new JSONObject(request.getBody().readUtf8());
            assertFalse(payload.getBoolean("stream"));
            assertFalse(payload.getBoolean("think"));
            assertEquals("system-test", payload.getJSONArray("messages").getJSONObject(0).getString("content"));
            assertEquals("system", payload.getJSONArray("messages").getJSONObject(0).getString("role"));
            assertEquals("user", payload.getJSONArray("messages").getJSONObject(1).getString("role"));
        }
    }
    @Test public void missingModelAndMalformedResponsesProduceErrorsRatherThanFakeAnswers() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            for (MockResponse response : Arrays.asList(new MockResponse().setResponseCode(404),
                    new MockResponse().setBody("not-json"), new MockResponse().setBody("{\"message\":{\"content\":\"\"}}"))) {
                server.enqueue(response);
                CountDownLatch done = new CountDownLatch(1);
                AtomicReference<String> answer = new AtomicReference<>();
                new LocalAiClient().ask(new AiSettings(server.url("/").toString(), "qwen2.5:7b"), "system",
                        Arrays.asList(new LocalAiClient.Turn("user", "hello")), result(done, answer));
                assertTrue(done.await(5, TimeUnit.SECONDS));
                assertTrue(answer.get().startsWith("ERROR:"));
            }
        }
    }
    private LocalAiClient.Result result(CountDownLatch done, AtomicReference<String> value) {
        return new LocalAiClient.Result() {
            @Override public void onAnswer(String answer) { value.set(answer); done.countDown(); }
            @Override public void onFailure(String reason) { value.set("ERROR:" + reason); done.countDown(); }
        };
    }
}
