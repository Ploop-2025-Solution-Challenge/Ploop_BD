package com.example.ploop_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    // 홈 컨트롤러
    // 메인 페이지를 위한 컨트롤러
    // 현재는 단순히 "Hello, World!"를 반환하는 API로 설정
    // 추후에 메인 페이지에 필요한 기능을 추가할 예정

    @GetMapping("/")
    public String home() {
        System.out.println("🔥 [로그] / 요청됨");

        return "서버 잘 돌아가고 있어요!";
    }
}
