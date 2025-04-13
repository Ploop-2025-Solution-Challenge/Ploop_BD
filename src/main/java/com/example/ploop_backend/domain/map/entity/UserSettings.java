package com.example.ploop_backend.domain.map.entity;

import com.example.ploop_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 유저 ID와 동일 (1:1)

    @OneToOne
    @MapsId
    private User user;

    private boolean isTrashBinVisible;
}

