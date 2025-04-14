package com.example.ploop_backend.domain.user.entity;

import com.example.ploop_backend.domain.user.model.Gender;
import com.example.ploop_backend.domain.user.model.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String googleId;
    private String name;

    @Column(nullable = true)
    private String nickname;

    @Column(nullable = true)
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = true)
    private String country;

    @Column(nullable = true)
    private String region;

    private String picture;

    @Enumerated(EnumType.STRING)
    private Role role;
}

