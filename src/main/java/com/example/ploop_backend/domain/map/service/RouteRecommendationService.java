package com.example.ploop_backend.domain.map.service;

import com.example.ploop_backend.domain.map.entity.RouteRecommendation;
import com.example.ploop_backend.domain.map.entity.TrashSpot;
import com.example.ploop_backend.domain.map.repository.RouteRecommendationRepository;
import com.example.ploop_backend.domain.map.repository.TrashSpotRepository;
import com.example.ploop_backend.dto.map.RouteRecommendationResponseDto;
import com.example.ploop_backend.external.gemini.GeminiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteRecommendationService {

    private final TrashSpotRepository trashSpotRepository;
    private final RouteRecommendationRepository routeRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public RouteRecommendationResponseDto recommendRoute(Long userId, double minLat, double maxLat, double minLng, double maxLng) {

        // "오늘" 이미 추천된 경로가 있다면 반환
        Optional<RouteRecommendation> existing = routeRepository.findByUserIdAndDate(userId, LocalDate.now());

        if (existing.isPresent()) {
            try {
                String json = existing.get().getRecommendationRouteJson();
                List<List<Double>> parsedRoute = objectMapper.readValue(json, new TypeReference<>() {});
                String motivation = geminiService.generateMotivation();
                return new RouteRecommendationResponseDto(parsedRoute, motivation);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("경로 파싱 실패", e);
            }
        }

        // bounds 내 trashspot 조회
        List<TrashSpot> spots = trashSpotRepository.findWithinBounds(minLat, maxLat, minLng, maxLng);
        if (spots.size() < 3) {
            throw new RuntimeException("TrashSpot이 충분하지 않습니다.");
        }

        // 시작점 랜덤 선택
        Collections.shuffle(spots);
        TrashSpot start = spots.get(0);

        // 거리 기반 정렬
        List<TrashSpot> sorted = spots.stream()
                .filter(s -> !s.equals(start))
                .sorted(Comparator.comparingDouble(s -> distance(start, s)))
                .limit(5) // 가까운 5개 중 일부만
                .collect(Collectors.toList());

        // 랜덤하게 2~5개 선택
        Collections.shuffle(sorted);
        int count = new Random().nextInt(4) + 2; // 2~5개
        List<TrashSpot> finalRoute = new ArrayList<>();
        finalRoute.add(start);
        finalRoute.addAll(sorted.subList(0, Math.min(count, sorted.size())));

        // 경로 좌표 리스트 만들기
        List<List<Double>> route = finalRoute.stream()
                .map(s -> List.of(s.getLatitude(), s.getLongitude()))
                .toList();

        // JSON 직렬화해서 DB 저장
        String routeJson;
        try {
            routeJson = objectMapper.writeValueAsString(route);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }

        // Gemini 동기부여 문구 생성
        String motivation = geminiService.generateMotivation();

        // 저장
        RouteRecommendation entity = RouteRecommendation.builder()
                .userId(userId)
                .recommendationRouteJson(routeJson)
                .date(LocalDate.now())
                .build();

        routeRepository.save(entity);

        return new RouteRecommendationResponseDto(route, motivation);
    }

    // 유클리디안 거리 계산
    private double distance(TrashSpot a, TrashSpot b) {
        double dx = a.getLatitude() - b.getLatitude();
        double dy = a.getLongitude() - b.getLongitude();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
