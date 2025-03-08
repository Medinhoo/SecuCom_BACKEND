package com.secretariatsocial.espacepartage.controller;

import com.secretariatsocial.espacepartage.dto.MessageResponse;
import com.secretariatsocial.espacepartage.dto.auth.LoginRequest;
import com.secretariatsocial.espacepartage.dto.auth.LoginResponse;
import com.secretariatsocial.espacepartage.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenDurationMs;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.authenticateUser(loginRequest);

        // Convertir millisecondes en secondes pour maxAge (qui utilise des secondes)
        int maxAgeSecs = (int) (refreshTokenDurationMs / 1000);

        // Créer un cookie sécurisé pour le refresh token
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true) // Activer en production avec HTTPS
                .sameSite("Strict")
                .maxAge(maxAgeSecs) // Utiliser la valeur de configuration
                .path("/api/auth/refresh") // Restreint au chemin de rafraîchissement
                .build();

        // Supprimer le refresh token de la réponse JSON
        loginResponse.setRefreshToken(null);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request) {

        // Récupérer le refresh token du cookie
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        // Si pas de refresh token dans les cookies, vérifier dans le body
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token is required");
        }

        // Rafraîchir le token
        LoginResponse response = authService.refreshToken(refreshToken);

        // Convertir millisecondes en secondes
        int maxAgeSecs = (int) (refreshTokenDurationMs / 1000);

        // Créer un nouveau cookie pour le refresh token (mise à jour)
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // Activer en production avec HTTPS
                .sameSite("Strict")
                .maxAge(maxAgeSecs) // Utiliser la même valeur de configuration
                .path("/api/auth/refresh")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    /**
     * Endpoint pour déconnecter l'utilisateur en supprimant le cookie refresh token
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            // Créer un cookie vide avec âge 0 en utilisant ResponseCookie
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(true) // Même valeur que lors de la création
                    .sameSite("Strict") // Même valeur que lors de la création
                    .maxAge(0) // 0 pour supprimer
                    .path("/api/auth/refresh") // Même chemin que lors de la création
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(new MessageResponse("Déconnexion réussie!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Erreur lors de la déconnexion: " + e.getMessage()));
        }
    }
}