package com.git.hui.springai;

import org.slf4j.Logger;
import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 基于大模型的语音识别
 *
 * @author YiHui
 * @date 2026/2/25
 */
@RestController
public class AudioTransactionController {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(AudioTransactionController.class);
    private final TranscriptionModel transcriptionModel;

    public AudioTransactionController(Environment environment) {
        // Spring AI 1.1 appends /v1/audio/transcriptions internally.
        String baseUrl = environment.getRequiredProperty("MODEL_BASE_URL").replaceFirst("/v1/?$", "");
        OpenAiAudioApi audioApi = OpenAiAudioApi.builder()
                .apiKey(environment.getRequiredProperty("MODEL_API_KEY"))
                .baseUrl(baseUrl)
                .build();
        transcriptionModel = new OpenAiAudioTranscriptionModel(audioApi,
                OpenAiAudioTranscriptionOptions.builder()
                        .model(environment.getProperty("MODEL_TRANSCRIPTION_NAME", "whisper-1"))
                        .build());
    }

    @Value("classpath:/test.mp3")
    private Resource resource;

    /**
     * 使用OpenAI风格的语言识别
     *
     * @return
     * @throws IOException
     */
    @GetMapping(path = "translateAudio")
    @ResponseBody
    public Object translateAudio() throws IOException {
        AudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource, options);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        log.info("response -> {}", response.getResult());
        return response.getResult().getOutput();
    }

    @GetMapping(path = "translateAudioV2")
    @ResponseBody
    public Object translateAudioV2() throws IOException {
        AudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.JSON)
                .language("zh")
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource, options);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        log.info("response -> {}", response.getResult());
        return response.getResult().getOutput();
    }
}
