package com.socialsecretariat.espacepartage.controller;

import com.socialsecretariat.espacepartage.dto.CollaboratorDto;
import com.socialsecretariat.espacepartage.service.CollaboratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/collaborators")
@RequiredArgsConstructor
public class CollaboratorController {

    private final CollaboratorService collaboratorService;

    @GetMapping
    public ResponseEntity<List<CollaboratorDto>> getAllCollaborators() {
        return ResponseEntity.ok(collaboratorService.getAllCollaborators());
    }

    @PostMapping
    public ResponseEntity<CollaboratorDto> createCollaborator(@Valid @RequestBody CollaboratorDto collaboratorDto) {
        return ResponseEntity.ok(collaboratorService.createCollaborator(collaboratorDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollaboratorDto> updateCollaborator(@PathVariable UUID id,
            @Valid @RequestBody CollaboratorDto collaboratorDto) {
        return ResponseEntity.ok(collaboratorService.updateCollaborator(id, collaboratorDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollaboratorDto> getCollaborator(@PathVariable UUID id) {
        return ResponseEntity.ok(collaboratorService.getCollaborator(id));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<CollaboratorDto>> getCollaboratorsByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(collaboratorService.getCollaboratorsByCompany(companyId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollaborator(@PathVariable UUID id) {
        collaboratorService.deleteCollaborator(id);
        return ResponseEntity.noContent().build();
    }
}
