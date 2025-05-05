package com.example.ploop_backend.external.gemini;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;


@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    /*public String generateMotivation() {
        String prompt = "Give me a short motivational sentence for someone who is out plogging (jogging while picking up trash). Keep it under 20 words.";

        try {
            GeminiRequest request = new GeminiRequest(prompt);

            GeminiResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/gemini-2.0-flash:generateContent")
                        .queryParam("key", apiKey).build())
                    .header("Content-Type", "application/json") // Google Gemini API 는 header 설정 필요
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            return response.getText();
        } catch (Exception e) {
            return "error "; //Keep going strong! You're cleaning the world, one step at a time.🌍
        }
    }*/

    public String generateMotivation() {
        String prompt = "Give me a short motivational sentence for someone who is out plogging (jogging while picking up trash). Keep it under 20 words.";

        try {
            GeminiRequest request = new GeminiRequest(prompt);

            log.info("📤 Gemini 요청: {}", request); // 요청 로그

            GeminiResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/gemini-2.0-flash:generateContent")
                            .queryParam("key", apiKey).build())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        log.error("❌ Gemini 응답 에러 상태: {}", clientResponse.statusCode());
                        return clientResponse.createException();
                    })
                    .bodyToMono(GeminiResponse.class)
                    .doOnNext(r -> log.info("✅ Gemini 응답 수신: {}", r.getText()))
                    .doOnError(e -> log.error("❌ Gemini 처리 중 예외 발생", e))
                    .block();

            String result = response != null ? response.getText() : null;
            log.info("📥 Gemini 응답 수신: {}", result);

            // 👉 줄바꿈 문자 제거
            return result != null ? result.replace("\n", " ").trim() : prompt;


        } catch (Exception e) {
            return "Default: Keep going strong! You're cleaning the world, one step at a time.";
        }
    }

}
