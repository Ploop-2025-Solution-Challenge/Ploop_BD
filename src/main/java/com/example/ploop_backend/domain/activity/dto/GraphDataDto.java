package com.example.ploop_backend.domain.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GraphDataDto {
    private String label;
    private int trashCount;
}
