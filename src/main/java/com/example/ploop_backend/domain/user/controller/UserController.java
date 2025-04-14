package com.example.ploop_backend.domain.user.controller;

import com.example.ploop_backend.domain.user.entity.User;
import com.example.ploop_backend.dto.user.UpdateUserProfileRequest;
import com.example.ploop_backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateUserProfileRequest request
    ) {
        User updatedUser = userService.updateProfile(user, request);
        return ResponseEntity.ok(updatedUser);
    }

}

