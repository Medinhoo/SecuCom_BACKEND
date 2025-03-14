package com.socialsecretariat.espacepartage.controller;

import com.socialsecretariat.espacepartage.dto.SocialSecretariatDto;
import com.socialsecretariat.espacepartage.service.SocialSecretariatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/socialSecretariat")
public class SocialSecretariatController {

    private final SocialSecretariatService socialSecretariatService;

    public SocialSecretariatController(SocialSecretariatService socialSecretariatService) {
        this.socialSecretariatService = socialSecretariatService;
    }

    // Create a new social secretariat
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SocialSecretariatDto> createSocialSecretariat(
            @Valid @RequestBody SocialSecretariatDto socialSecretariatDto) {
        SocialSecretariatDto createdSecretariat = socialSecretariatService
                .createSocialSecretariat(socialSecretariatDto);
        return new ResponseEntity<>(createdSecretariat, HttpStatus.CREATED);
    }

    // Get all social secretariats
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIAT')")
    public ResponseEntity<List<SocialSecretariatDto>> getAllSocialSecretariats() {
        List<SocialSecretariatDto> secretariats = socialSecretariatService.getAllSocialSecretariats();
        return ResponseEntity.ok(secretariats);
    }

    // Get a social secretariat by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIAT')")
    public ResponseEntity<SocialSecretariatDto> getSocialSecretariatById(@PathVariable UUID id) {
        SocialSecretariatDto secretariat = socialSecretariatService.getSocialSecretariatById(id);
        return ResponseEntity.ok(secretariat);
    }

    // Update a social secretariat
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SocialSecretariatDto> updateSocialSecretariat(
            @PathVariable UUID id,
            @Valid @RequestBody SocialSecretariatDto socialSecretariatDto) {
        SocialSecretariatDto updatedSecretariat = socialSecretariatService.updateSocialSecretariat(id,
                socialSecretariatDto);
        return ResponseEntity.ok(updatedSecretariat);
    }

    // Delete a social secretariat
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSocialSecretariat(@PathVariable UUID id) {
        socialSecretariatService.deleteSocialSecretariat(id);
        return ResponseEntity.noContent().build();
    }

    // Search social secretariats by name
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARIAT')")
    public ResponseEntity<List<SocialSecretariatDto>> searchSocialSecretariats(
            @RequestParam String name) {
        List<SocialSecretariatDto> secretariats = socialSecretariatService.searchSocialSecretariatsByName(name);
        return ResponseEntity.ok(secretariats);
    }
}