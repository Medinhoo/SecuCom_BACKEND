package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.DimonaDto;
import com.socialsecretariat.espacepartage.dto.CreateDimonaRequest;
import com.socialsecretariat.espacepartage.dto.UpdateDimonaRequest;
import com.socialsecretariat.espacepartage.model.Dimona;
import com.socialsecretariat.espacepartage.model.Collaborator;
import com.socialsecretariat.espacepartage.model.Company;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.DimonaRepository;
import com.socialsecretariat.espacepartage.repository.CollaboratorRepository;
import com.socialsecretariat.espacepartage.repository.CompanyRepository;
import com.socialsecretariat.espacepartage.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DimonaService {
    private final DimonaRepository dimonaRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final StatusHistoryService statusHistoryService;

    public DimonaDto createDimona(CreateDimonaRequest request) {
        Collaborator collaborator = collaboratorRepository.findById(request.getCollaboratorId())
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found"));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        Dimona dimona = new Dimona();
        BeanUtils.copyProperties(request, dimona);
        dimona.setCollaborator(collaborator);
        dimona.setCompany(company);
        dimona.setStatus(Dimona.Status.TO_SEND);

        Dimona savedDimona = dimonaRepository.save(dimona);
        
        // Record status history for initial status
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);
            
            if (currentUser != null) {
                statusHistoryService.recordDimonaStatusChange(
                    savedDimona.getId(),
                    null, // No previous status for creation
                    savedDimona.getStatus().toString(),
                    "Création de la déclaration DIMONA",
                    currentUser
                );
            }
        }
        
        // Send notification about DIMONA creation
        String collaboratorName = collaborator.getFirstName() + " " + collaborator.getLastName();
        notificationService.notifyDimonaCreated(
            savedDimona.getId(),
            collaboratorName,
            company.getId(),
            null // We'll need to get the current user ID from the security context in a real implementation
        );
        
        return convertToDto(savedDimona);
    }

    public DimonaDto updateDimona(UUID id, UpdateDimonaRequest request) {
        Dimona dimona = dimonaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dimona not found"));
        
        // Update the dimona fields
        dimona.setType(request.getType());
        dimona.setEntryDate(request.getEntryDate());
        dimona.setExitDate(request.getExitDate());
        dimona.setExitReason(request.getExitReason());
        dimona.setOnssReference(request.getOnssReference());
        dimona.setErrorMessage(request.getErrorMessage());
        
        Dimona savedDimona = dimonaRepository.save(dimona);
        
        // Record update in status history
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);
            
            if (currentUser != null) {
                statusHistoryService.recordDimonaStatusChange(
                    savedDimona.getId(),
                    savedDimona.getStatus().toString(),
                    savedDimona.getStatus().toString(),
                    "Mise à jour des informations de la déclaration DIMONA",
                    currentUser
                );
            }
        }
        
        return convertToDto(savedDimona);
    }

    public DimonaDto updateDimonaStatus(UUID id, Dimona.Status newStatus) {
        return updateDimonaStatus(id, newStatus, null);
    }

    public DimonaDto updateDimonaStatus(UUID id, Dimona.Status newStatus, String changeReason) {
        Dimona dimona = dimonaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dimona not found"));
        
        Dimona.Status oldStatus = dimona.getStatus();
        dimona.setStatus(newStatus);
        
        Dimona savedDimona = dimonaRepository.save(dimona);
        
        // Record status history if status actually changed
        if (oldStatus != newStatus) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                String username = authentication.getName();
                User currentUser = userRepository.findByUsername(username).orElse(null);
                
                if (currentUser != null) {
                    String reason = changeReason != null ? changeReason : 
                        String.format("Changement de statut de %s vers %s", oldStatus, newStatus);
                    
                    statusHistoryService.recordDimonaStatusChange(
                        savedDimona.getId(),
                        oldStatus.toString(),
                        newStatus.toString(),
                        reason,
                        currentUser
                    );
                }
            }
            
            // Send notification about status change
            String collaboratorName = dimona.getCollaborator().getFirstName() + " " + dimona.getCollaborator().getLastName();
            notificationService.notifyDimonaStatusChanged(
                savedDimona.getId(),
                collaboratorName,
                newStatus.toString(),
                dimona.getCompany().getId()
            );
        }
        
        return convertToDto(savedDimona);
    }

    public DimonaDto getDimona(UUID id) {
        return dimonaRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("Dimona not found"));
    }

    public List<DimonaDto> getAllDimonas() {
        return dimonaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<DimonaDto> getDimonasByCollaborator(UUID collaboratorId) {
        return dimonaRepository.findByCollaboratorId(collaboratorId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<DimonaDto> getDimonasByCompany(UUID companyId) {
        return dimonaRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void deleteDimona(UUID id) {
        if (!dimonaRepository.existsById(id)) {
            throw new EntityNotFoundException("Dimona not found");
        }
        dimonaRepository.deleteById(id);
    }

    private DimonaDto convertToDto(Dimona dimona) {
        DimonaDto dto = new DimonaDto();
        BeanUtils.copyProperties(dimona, dto);
        dto.setCollaboratorId(dimona.getCollaborator().getId());
        dto.setCompanyId(dimona.getCompany().getId());
        return dto;
    }

}
