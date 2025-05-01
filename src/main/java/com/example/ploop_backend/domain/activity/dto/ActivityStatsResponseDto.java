package com.example.ploop_backend.domain.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ActivityStatsResponseDto {
    private int totalTrash;
    private double totalMiles;
    private double totalHours;
    private int challengeCompleted;
    private int challengeGoal;
    private List<GraphDataDto> graphData;
}
