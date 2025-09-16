package com.example.ploop_backend.domain.route.service;

import com.example.ploop_backend.domain.route.repository.GeoSearchRepository;
import com.example.ploop_backend.dto.map.RouteRecommendRequestDto;
import com.example.ploop_backend.dto.map.RouteRecommendResponseDto;
import com.example.ploop_backend.dto.route.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// DB 반경조회 → AI 호출 → 응답 매핑.
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteRecommendService {

    private final GeoSearchRepository geoSearchRepository;

    // ⚠️ 실제 AI 서버 주소로 변경해 주세요.
    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://13.124.215.254:8082")
            .build();

    private static final int DEFAULT_RADIUS_METERS = 5000; // 반경 5km
    private static final int DEFAULT_LIMIT = 800;          // 쓰레기/통 800개까지, 과도한 페이로드 방지

    // 출발지, 도착지 좌표 → 추천 경로 응답
    @Transactional(readOnly = true)
    public RouteRecommendResponseDto recommend(RouteRecommendRequestDto req) {
        // 1) 반경 내 포인트 조회 (DB)
        List<LatLngDto> binsLatLng  = geoSearchRepository.findBinsWithin(req.getCurrent(), DEFAULT_RADIUS_METERS, DEFAULT_LIMIT);
        List<LatLngDto> trashLatLng = geoSearchRepository.findSpotsWithin(req.getCurrent(), DEFAULT_RADIUS_METERS, DEFAULT_LIMIT);

        // 2) AI 요청 DTO 구성 (id는 일단 일회성으로 생성)
        AiRouteComputeRequestDto aiReq = new AiRouteComputeRequestDto();
        aiReq.setCurrent(req.getCurrent());
        aiReq.setDestination(req.getDestination());
        aiReq.setBins(withIds(binsLatLng, "b"));
        aiReq.setTrash(withIds(trashLatLng, "t"));

        // 3) 백 → AI 호출
        AiRouteComputeResponseDto aiRes = webClient.post()
                .uri("/route/compute")
                .bodyValue(aiReq)
                .retrieve()
                .bodyToMono(AiRouteComputeResponseDto.class)
                .timeout(Duration.ofSeconds(8))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .block();

        try {
            String json = new ObjectMapper().writeValueAsString(aiRes);
            log.info("AI 서버 응답(JSON) = {}", json);
        } catch (Exception e) {
            log.error("AI 응답 로그 변환 실패", e);
        }

        log.info("AI 서버 응답 : {}", aiRes);

        // 4) 프론트 응답으로 매핑
        RouteRecommendResponseDto res = new RouteRecommendResponseDto();
        res.setCurrent(req.getCurrent());
        res.setDestination(req.getDestination());
        if (aiRes == null) {
            res.setSuccess(false);
            res.setMessage("route-ai 응답 없음");
            res.setWaypoints(List.of());
            res.setRoute(new RouteSummaryDto("", 0, "0s"));
            return res;
        }
        if (aiRes.getRoute() == null) {
            res.setSuccess(false);
            res.setMessage("AI 응답에 route 없음");
            res.setWaypoints(aiRes.getWaypoints() != null ? aiRes.getWaypoints() : List.of());
            res.setRoute(new RouteSummaryDto("", 0, "0s"));
            return res;
        }
        res.setSuccess(aiRes.isSuccess());
        res.setMessage(aiRes.getMessage());
        res.setWaypoints(aiRes.getWaypoints());

        RouteSummaryDto routeSummary = new RouteSummaryDto();
        routeSummary.setEncodedPolyline(aiRes.getRoute().getEncodedPolyline());
        routeSummary.setDistanceMeters(aiRes.getRoute().getDistanceMeters());
        routeSummary.setDuration(aiRes.getRoute().getDuration());
        res.setRoute(routeSummary);

        return res;
    }

    // LatLng → GeoPointWithId 변환 (id 미보유시 접두사+증가번호로 생성)
    private List<GeoPointWithIdDto> withIds(List<LatLngDto> pts, String prefix) {
        if (pts == null) return List.of();
        List<GeoPointWithIdDto> out = new ArrayList<>(pts.size());
        AtomicInteger seq = new AtomicInteger(1);
        for (LatLngDto p : pts) {
            GeoPointWithIdDto gp = new GeoPointWithIdDto();
            gp.setId(prefix + seq.getAndIncrement()); // b1, b2 / t1, t2 ...
            gp.setLat(p.getLat());
            gp.setLng(p.getLng());
            out.add(gp);
        }
        return out;
    }
}
