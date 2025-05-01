package com.example.ploop_backend.domain.mission.entity;

import com.example.ploop_backend.domain.mission.model.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;            // 미션 이름
    private String description;     // 상세 설명

    @Enumerated(EnumType.STRING)
    private Category category;      // 쓰레기 유형 등
}


