// 📁 MissionVerificationService.java
package com.example.ploop_backend.domain.mission.service;

import com.example.ploop_backend.domain.mission.entity.MissionVerification;
import com.example.ploop_backend.domain.mission.entity.UserMission;
import com.example.ploop_backend.domain.mission.repository.MissionVerificationRepository;
import com.example.ploop_backend.domain.mission.repository.UserMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MissionVerificationService {

    private final MissionVerificationRepository missionVerificationRepository;
    private final UserMissionRepository userMissionRepository;

    // 유저가 사진 제출 시 호출
    //
    public MissionVerification createVerification(Long userMissionId, String imageUrl) {
        UserMission userMission = userMissionRepository.findById(userMissionId).orElseThrow();

        MissionVerification verification = MissionVerification.builder()
                .userMission(userMission)
                .imageUrl(imageUrl)
                .isVerified(null) // 아직 AI 결과 미정
                .submittedAt(LocalDateTime.now()) // 제출 시간 기록
                .build();

        return missionVerificationRepository.save(verification);
    }

    // AI가 결과를 판별해서 미션 인증 처리
    public void applyAIResult(Long verificationId, boolean isVerified) {
        MissionVerification verification = missionVerificationRepository.findById(verificationId)
                .orElseThrow();

        verification.setIsVerified(isVerified);
        verification.setVerifiedAt(LocalDateTime.now());
        missionVerificationRepository.save(verification);

        if (isVerified) {
            UserMission um = verification.getUserMission();
            um.setIsVerified(true);
            userMissionRepository.save(um);
        }
    }
}
