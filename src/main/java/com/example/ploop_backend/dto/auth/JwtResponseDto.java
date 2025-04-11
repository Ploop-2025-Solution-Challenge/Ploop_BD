package com.example.ploop_backend.dto.auth;

public class JwtResponseDto {
    private String jwt;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String role;

    public JwtResponseDto(String jwt, Long id, String username, String email, String role) {
        this.jwt = jwt;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public JwtResponseDto(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() { // get뒤에 따라서 응답 형식 "jwt": 로 나옴
        return jwt;
    }

    public void setAccessToken(String jwt) {
        this.jwt = jwt;
    }

    /*public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }*/
}
