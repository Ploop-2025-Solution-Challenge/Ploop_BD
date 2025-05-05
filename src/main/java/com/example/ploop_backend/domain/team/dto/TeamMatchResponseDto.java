package com.example.ploop_backend.domain.team.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamMatchResponseDto {
    @JsonProperty("user_id_1")
    private Long userId1;

    @JsonProperty("user_id_2")
    private Long userId2;

    private String week;
    private LocalDateTime createdAt;
}

