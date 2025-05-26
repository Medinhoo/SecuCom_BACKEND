package com.socialsecretariat.espacepartage.controller;

import com.socialsecretariat.espacepartage.dto.DimonaDto;
import com.socialsecretariat.espacepartage.dto.CreateDimonaRequest;
import com.socialsecretariat.espacepartage.dto.UpdateDimonaRequest;
import com.socialsecretariat.espacepartage.dto.StatusHistoryDto;
import com.socialsecretariat.espacepartage.dto.auth.MessageResponse;
import com.socialsecretariat.espacepartage.model.Dimona;
import com.socialsecretariat.espacepartage.service.DimonaService;
import com.socialsecretariat.espacepartage.service.StatusHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dimona")
public class DimonaController {
    private final DimonaService dimonaService;
    private final StatusHistoryService statusHistoryService;

    public DimonaController(DimonaService dimonaService, StatusHistoryService statusHistoryService) {
        this.dimonaService = dimonaService;
        this.statusHistoryService = statusHistoryService;
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT') or hasRole('COMPANY')")
    public ResponseEntity<DimonaDto> updateDimona(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDimonaRequest request) {
        DimonaDto dimona = dimonaService.updateDimona(id, request);
        return ResponseEntity.ok(dimona);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECRETARIAT') or hasRole('COMPANY')")
    public ResponseEntity<DimonaDto> updateDimonaStatus(
            @PathVariable UUID id, 
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        try {
            Dimona.Status statusEnum = Dimona.Status.valueOf(status);
            
            // Check if user has COMPANY role and restrict to TO_SEND status only
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isCompanyRole = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_COMPANY"));
            
            if (isCompanyRole && !statusEnum.equals(Dimona.Status.TO_SEND)) {
                throw new IllegalArgumentException("Company role can only change status to TO_SEND");
            }
            
            DimonaDto dimona = dimonaService.updateDimonaStatus(id, statusEnum, reason);
            return ResponseEntity.ok(dimona);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status + ". Valid values are: TO_CONFIRM, TO_SEND, IN_PROGRESS, REJECTED, ACCEPTED");
        }
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<List<StatusHistoryDto>> getDimonaStatusHistory(@PathVariable UUID id) {
        List<StatusHistoryDto> history = statusHistoryService.getDimonaStatusHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/status-history/latest")
    public ResponseEntity<StatusHistoryDto> getLatestDimonaStatusChange(@PathVariable UUID id) {
        StatusHistoryDto latestChange = statusHistoryService.getLatestDimonaStatusChange(id);
        if (latestChange != null) {
            return ResponseEntity.ok(latestChange);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/status-history/count")
    public ResponseEntity<Long> countDimonaStatusChanges(@PathVariable UUID id) {
        long count = statusHistoryService.countDimonaStatusChanges(id);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteDimona(@PathVariable UUID id) {
        dimonaService.deleteDimona(id);
        return ResponseEntity.ok(new MessageResponse("Dimona declaration successfully deleted"));
    }
}
