package com.example.ploop_backend.domain.user.entity;

import com.example.ploop_backend.domain.user.model.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    private Motivation motivation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_location_preferences", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preference")
    private List<PreferredArea> preferredArea;
}

