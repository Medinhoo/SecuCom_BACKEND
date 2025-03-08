package com.secretariatsocial.espacepartage.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.secretariatsocial.espacepartage.model.User;
import com.secretariatsocial.espacepartage.model.User.Role;
import com.secretariatsocial.espacepartage.service.UserService;
import com.secretariatsocial.espacepartage.dto.UserDto;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        // Convertir l'entité User en DTO pour ne pas exposer les données sensibles
        UserDto userDto = convertToDto(user);

        return ResponseEntity.ok(userDto);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(this::convertToDto)
                .toList());
    }

    // Méthode utilitaire pour convertir User en UserDto
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        // Conversion des rôles enum en chaînes de caractères
        List<String> roleNames = new ArrayList<>();
        for (Role role : user.getRoles()) {
            roleNames.add(role.name()); // Utilisation directe de .name() sur l'enum
        }
        dto.setRoles(roleNames);

        return dto;
    }
}