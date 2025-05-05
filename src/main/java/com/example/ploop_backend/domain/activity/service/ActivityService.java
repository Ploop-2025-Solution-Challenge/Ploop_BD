package com.example.ploop_backend.domain.activity.service;

import com.example.ploop_backend.domain.activity.dto.ActivityStatsResponseDto;
import com.example.ploop_backend.domain.activity.dto.GraphDataDto;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import com.example.ploop_backend.domain.plogging.entity.Route;
import com.example.ploop_backend.domain.plogging.repository.RouteRepository;
import com.example.ploop_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final RouteRepository routeRepository;
    private final UserMissionRepository userMissionRepository;

    public ActivityStatsResponseDto getActivityStats(String range, LocalDate startDate, LocalDate endDate, User user) {
        // range=Y인 경우 날짜 보정
        if (range.equals("Y")) {
            int targetYear = endDate.getYear(); // 또는 startDate.getYear()
            startDate = LocalDate.of(targetYear, 1, 1);
            endDate = LocalDate.of(targetYear, 12, 31);
        }

        // 사용자의 startDate~endDate의 데이터 조회
        List<Route> routes = routeRepository
                .findAllByUserAndStartDateTimeBetween(user, startDate.atStartOfDay(), endDate.atTime(23, 59));

        // 전체 route 데이터를 stream으로 순회하며 합산
        int totalTrash = routes.stream().mapToInt(r -> Optional.ofNullable(r.getTrashCollectedCount()).orElse(0)).sum();
        double totalMiles = routes.stream().mapToDouble(r -> Optional.ofNullable(r.getDistanceMiles()).orElse(0.0)).sum();
        double totalHours = routes.stream().mapToDouble(r -> Optional.ofNullable(r.getTimeDuration()).orElse(0.0)).sum();

        // W, M, Y 값에 따라 데이터를 그룹화
        List<GraphDataDto> graphData = groupByRange(range, routes, startDate);

        int challengeGoal;

        if (range.equals("W") || range.equals("M")) {
            int numberOfWeeks = (int) ChronoUnit.WEEKS.between(startDate, endDate.plusDays(1));
            challengeGoal = numberOfWeeks * 3; // 주당 3개 목표
        } else if (range.equals("Y")) {
            challengeGoal = 12 * 12; // 월당 12개 목표
        } else {
            challengeGoal = 0; // 잘못된 range 값 처리
        }

        // UserMission 테이블에서 기간 내 해당 유저가 isVerified가 true인 미션 개수 조회
        int challengeCompleted = userMissionRepository.countByUserAndIsVerifiedTrueAndCreatedAtBetween(
                user, startDate.atStartOfDay(), endDate.atTime(23, 59));


        return new ActivityStatsResponseDto(
                totalTrash, totalMiles, totalHours, challengeCompleted, challengeGoal, graphData
        );
    }

    private List<GraphDataDto> groupByRange(String range, List<Route> routes, LocalDate startDate) {
        Map<String, Integer> result = new LinkedHashMap<>();

        switch (range) {
            case "W":
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
                for (int i = 0; i < 7; i++) {
                    LocalDate day = startDate.plusDays(i);
                    String label = day.format(formatter).toUpperCase();  // ex: MON
                    int count = (int) routes.stream()
                            .filter(r -> r.getStartDateTime().toLocalDate().equals(day))
                            .mapToInt(r -> Optional.ofNullable(r.getTrashCollectedCount()).orElse(0))
                            .sum();
                    result.put(label, count);
                }
                break;
            case "M":
                for (int w = 1; w <= 4; w++) {
                    LocalDate from = startDate.plusDays((w - 1) * 7);
                    LocalDate to = from.plusDays(6);
                    String label = w + "W";
                    int count = routes.stream()
                            .filter(r -> {
                                LocalDate d = r.getStartDateTime().toLocalDate();
                                return !d.isBefore(from) && !d.isAfter(to);
                            })
                            .mapToInt(r -> Optional.ofNullable(r.getTrashCollectedCount()).orElse(0))
                            .sum();
                    result.put(label, count);
                }
                break;
            case "Y":
                int targetYear = startDate.getYear();
                for (int m = 1; m <= 12; m++) {
                    int month = m;
                    String label = String.valueOf(m);
                    int count = routes.stream()
                            .filter(r -> {
                                LocalDate date = r.getStartDateTime().toLocalDate();
                                return date.getYear() == targetYear && date.getMonthValue() == month;
                            })
                            .mapToInt(r -> Optional.ofNullable(r.getTrashCollectedCount()).orElse(0))
                            .sum();
                    result.put(label, count);
                }
                break;
        }

        return result.entrySet().stream()
                .map(e -> new GraphDataDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

}

