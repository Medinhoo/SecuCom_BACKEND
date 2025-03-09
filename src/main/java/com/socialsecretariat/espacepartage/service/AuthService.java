package com.socialsecretariat.espacepartage.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.socialsecretariat.espacepartage.dto.auth.LoginRequest;
import com.socialsecretariat.espacepartage.dto.auth.LoginResponse;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.UserRepository;
import com.socialsecretariat.espacepartage.security.JwtUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final JwtUtils jwtUtils;
        private final UserRepository userRepository;

        public AuthService(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                        UserRepository userRepository) {
                this.authenticationManager = authenticationManager;
                this.jwtUtils = jwtUtils;
                this.userRepository = userRepository;
        }

        public LoginResponse authenticateUser(LoginRequest loginRequest) {
                // Vérification du mot de passe
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                                                loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Utilisation cohérente des méthodes pour générer les tokens
                String jwt = jwtUtils.generateAccessToken(authentication);
                String refreshToken = jwtUtils.generateRefreshToken(authentication);

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                List<String> roles = userDetails.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList());

                User user = userRepository.findByUsername(userDetails.getUsername())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                return new LoginResponse(
                                jwt,
                                refreshToken,
                                user.getId(),
                                roles);
        }

        public LoginResponse refreshToken(String refreshToken) {
                // Valider le refresh token
                if (!jwtUtils.validateRefreshToken(refreshToken)) {
                        throw new RuntimeException("Refresh token is invalid or expired");
                }

                // Extraire le username du refresh token
                String username = jwtUtils.getUserNameFromJwtToken(refreshToken);

                // Chercher l'utilisateur dans la base de données
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

                // Générer un nouveau access token
                String newAccessToken = jwtUtils.generateAccessTokenFromUsername(username);

                // Convertir les rôles en liste de strings
                List<String> roles = user.getRoles().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList());

                // Retourner la réponse avec le nouveau token
                return new LoginResponse(
                                newAccessToken,
                                refreshToken, // On renvoie le même refresh token
                                user.getId(),
                                roles);
        }
}