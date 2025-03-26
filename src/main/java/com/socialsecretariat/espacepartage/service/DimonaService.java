package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.DimonaDto;
import com.socialsecretariat.espacepartage.dto.CreateDimonaRequest;
import com.socialsecretariat.espacepartage.model.Dimona;
import com.socialsecretariat.espacepartage.model.Collaborator;
import com.socialsecretariat.espacepartage.model.Company;
import com.socialsecretariat.espacepartage.repository.DimonaRepository;
import com.socialsecretariat.espacepartage.repository.CollaboratorRepository;
import com.socialsecretariat.espacepartage.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DimonaService {
    private final DimonaRepository dimonaRepository;
    private final CollaboratorRepository collaboratorRepository;
    private final CompanyRepository companyRepository;

    public DimonaService(DimonaRepository dimonaRepository,
            CollaboratorRepository collaboratorRepository,
            CompanyRepository companyRepository) {
        this.dimonaRepository = dimonaRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.companyRepository = companyRepository;
    }

    public DimonaDto createDimona(CreateDimonaRequest request) {
        Collaborator collaborator = collaboratorRepository.findById(request.getCollaboratorId())
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found"));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        Dimona dimona = new Dimona();
        BeanUtils.copyProperties(request, dimona);
        dimona.setCollaborator(collaborator);
        dimona.setCompany(company);
        dimona.setStatus("TO_SEND");

        Dimona savedDimona = dimonaRepository.save(dimona);
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
