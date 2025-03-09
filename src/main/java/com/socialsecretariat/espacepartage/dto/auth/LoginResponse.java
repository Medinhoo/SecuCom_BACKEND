package com.socialsecretariat.espacepartage.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private List<String> roles;

    @JsonIgnore
    private String refreshToken;

    public LoginResponse(String token, String refreshToken, Long id, List<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.roles = roles;
        this.type = "Bearer";
    }
}