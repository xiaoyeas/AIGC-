package com.aigc.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;

    private Long userId;

    private String username;

    private Long expiresIn;

    public LoginResponse(String token, Long userId, String username, Long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.expiresIn = expiresIn;
    }
}