package com.example.ploop_backend.domain.mission.dto;

import com.example.ploop_backend.domain.mission.entity.MissionVerification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class MissionVerificationDto {
    private Long id;
    private String imageUrl;
    private Boolean verified;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;

    public static MissionVerificationDto from(MissionVerification entity) {
        return MissionVerificationDto.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .verified(entity.getIsVerified())
                .submittedAt(entity.getSubmittedAt())
                .verifiedAt(entity.getVerifiedAt())
                .build();
    }
}

