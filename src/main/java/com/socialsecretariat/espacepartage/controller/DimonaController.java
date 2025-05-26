package com.socialsecretariat.espacepartage.controller;

import com.socialsecretariat.espacepartage.dto.DimonaDto;
import com.socialsecretariat.espacepartage.dto.CreateDimonaRequest;
import com.socialsecretariat.espacepartage.dto.auth.MessageResponse;
import com.socialsecretariat.espacepartage.model.Dimona;
import com.socialsecretariat.espacepartage.service.DimonaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dimona")
public class DimonaController {
    private final DimonaService dimonaService;

    public DimonaController(DimonaService dimonaService) {
        this.dimonaService = dimonaService;
    }

    @PostMapping
    public ResponseEntity<DimonaDto> createDimona(@Valid @RequestBody CreateDimonaRequest request) {
        DimonaDto dimona = dimonaService.createDimona(request);
        return new ResponseEntity<>(dimona, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DimonaDto> getDimona(@PathVariable UUID id) {
        DimonaDto dimona = dimonaService.getDimona(id);
        return ResponseEntity.ok(dimona);
    }

    @GetMapping
    public ResponseEntity<List<DimonaDto>> getAllDimonas() {
        List<DimonaDto> dimonas = dimonaService.getAllDimonas();
        return ResponseEntity.ok(dimonas);
    }

    @GetMapping("/collaborator/{collaboratorId}")
    public ResponseEntity<List<DimonaDto>> getDimonasByCollaborator(@PathVariable UUID collaboratorId) {
        List<DimonaDto> dimonas = dimonaService.getDimonasByCollaborator(collaboratorId);
        return ResponseEntity.ok(dimonas);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<DimonaDto>> getDimonasByCompany(@PathVariable UUID companyId) {
        List<DimonaDto> dimonas = dimonaService.getDimonasByCompany(companyId);
        return ResponseEntity.ok(dimonas);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT')")
    public ResponseEntity<DimonaDto> updateDimonaStatus(@PathVariable UUID id, @RequestParam String status) {
        try {
            Dimona.Status statusEnum = Dimona.Status.valueOf(status);
            DimonaDto dimona = dimonaService.updateDimonaStatus(id, statusEnum);
            return ResponseEntity.ok(dimona);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status + ". Valid values are: TO_CONFIRM, TO_SEND, IN_PROGRESS, REJECTED, ACCEPTED");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteDimona(@PathVariable UUID id) {
        dimonaService.deleteDimona(id);
        return ResponseEntity.ok(new MessageResponse("Dimona declaration successfully deleted"));
    }
}
