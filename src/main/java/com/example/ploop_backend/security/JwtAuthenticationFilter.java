package com.example.ploop_backend.security;

import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.domain.user.repository.UserRepository;
import com.example.ploop_backend.domain.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // 사용자 JWT 검사 및 인증 객체 등록(인증 등록)
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // 필터가 적용되지 않을 URL 설정
    // /api/auth/** 및 /auth/google/redirect 경로는 필터를 적용하지 않음
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth") || path.startsWith("/auth/google/redirect");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JwtFilter] Authorization 헤더가 없거나 형식이 잘못됨. 필터 통과");
            filterChain.doFilter(request, response); // JWT가 없으면 다음 필터로 넘김
            return;
        }

        String jwt = authHeader.substring(7); // "Bearer " 제외
        Claims claims = jwtService.parseJwtToken(jwt);
        String email = claims.getSubject();

        System.out.println("[JwtFilter] JWT 필터 실행됨 ✅ 토큰: " + jwt);
        System.out.println("[JwtFilter] 파싱된 사용자 이메일: " + email);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        System.out.println("[JwtFilter] ❌ 이메일로 사용자 찾기 실패: " + email);
                        return new RuntimeException("User not found");
                    });

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())) // 권한 필요 시 넣기
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("[JwtFilter] ✅ 인증 객체 등록 완료: " + user.getEmail());
        } else {
            System.out.println("[JwtFilter] ⚠️ 이미 인증되어 있음 또는 이메일 없음");
        }
        filterChain.doFilter(request, response);
        }

    }



