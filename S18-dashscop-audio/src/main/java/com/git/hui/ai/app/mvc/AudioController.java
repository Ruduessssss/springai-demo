package com.git.hui.ai.app.mvc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * 使用 OpenAI 兼容接口合成语音。
 */
@RestController
public class AudioController {
    private final RestClient restClient;
    private final String model;
    private final String voice;

    public AudioController(@Value("${MODEL_BASE_URL}") String baseUrl,
                           @Value("${MODEL_API_KEY}") String apiKey,
                           @Value("${MODEL_SPEECH_NAME:tts-1}") String model,
                           @Value("${MODEL_SPEECH_VOICE:alloy}") String voice) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
                .build();
        this.model = model;
        this.voice = voice;
    }

    @GetMapping(value = "/audio", produces = "audio/wav")
    public byte[] toAudio(@RequestParam String text) throws IOException {
        byte[] audio = restClient.post()
                .uri("/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("model", model, "input", text, "voice", voice, "response_format", "wav"))
                .retrieve()
                .body(byte[].class);
        if (audio == null || audio.length == 0) {
            throw new IOException("语音合成接口返回了空音频");
        }
        Files.write(Path.of("downloaded_audio.wav"), audio);
        return audio;
    }
}
