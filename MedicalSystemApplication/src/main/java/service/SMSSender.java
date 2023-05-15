package service;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SMSSender {

    private static final String BASE_URL = "https://89ezk3.api.infobip.com";
    private static final String API_KEY = "App 458fa4411bdcefec3111d796a266dd51-045ac7e8-4438-4e6c-b906-26219dddcabc";
    private static final String MEDIA_TYPE = "application/json";
    private static final String SENDER = "MedicalSystemInfoSMS";

    public void sendMessage(String recipientPhone, String messageText) throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        String bodyJson = String.format("{\"messages\":[{\"from\":\"%s\",\"destinations\":[{\"to\":\"%s\"}],\"text\":\"%s\"}]}",
                SENDER,
                recipientPhone,
                messageText
        );

        MediaType mediaType = MediaType.parse(MEDIA_TYPE);
        RequestBody body = RequestBody.create(mediaType, bodyJson);
        Request request = prepareHttpRequest(body);
        client.newCall(request).execute();
    }

    private static Request prepareHttpRequest(RequestBody body) {
        return new Request.Builder()
                .url(String.format("%s/sms/2/text/advanced", BASE_URL))
                .method("POST", body)
                .addHeader("Authorization", API_KEY)
                .addHeader("Content-Type", MEDIA_TYPE)
                .addHeader("Accept", MEDIA_TYPE)
                .build();
    }

}