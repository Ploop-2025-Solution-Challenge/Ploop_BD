package com.example.ploop_backend.domain.auth.service;

import com.example.ploop_backend.domain.user.entity.User;
import io.jsonwebtoken.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {
    // JWT 토큰을 생성 및 검증 서비스 클래스

    @Value("${jwt.secret}")
    private String secret;// 실제 환경에선 환경변수로

    public String generateJwtToken(User user) { //User 객체 정보를 받아 JWT 생성
        return Jwts.builder()
                .setSubject(user.getEmail()) //토큰의 고유한 subject(주체)
                .claim("userId", user.getId()) // 커스텀 정보 추가
                .claim("role", user.getRole().name()) // 사용자 권한
                .setIssuedAt(new Date()) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1일 유효(만료시간)
                .signWith(SignatureAlgorithm.HS256, secret) // 서명 알고리즘과 비밀키 설정
                .compact(); // 문자열 형태의 JWT 반환
    }

    public Claims parseJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret) // 서명 검증을 위한 비밀키 설정
                .parseClaimsJws(token) // 위 비밀키를 가지고 파싱 및 검증
                .getBody();
    }

}

