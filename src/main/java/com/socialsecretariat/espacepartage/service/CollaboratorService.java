package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.CollaboratorDto;
import com.socialsecretariat.espacepartage.model.Collaborator;
import com.socialsecretariat.espacepartage.model.Dimona;
import com.socialsecretariat.espacepartage.repository.CollaboratorRepository;
import com.socialsecretariat.espacepartage.repository.CompanyRepository;
import com.socialsecretariat.espacepartage.repository.DimonaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CollaboratorService {

    private final CollaboratorRepository collaboratorRepository;
    private final CompanyRepository companyRepository;
    private final DimonaRepository dimonaRepository;

    public CollaboratorDto createCollaborator(CollaboratorDto dto) {
        if (collaboratorRepository.existsByNationalNumber(dto.getNationalNumber())) {
            throw new IllegalArgumentException("A collaborator with this national number already exists");
        }

        Collaborator collaborator = new Collaborator();
        updateCollaboratorFromDto(collaborator, dto);
        collaborator.setCreatedAt(LocalDate.now());
        collaborator.setUpdatedAt(LocalDate.now());

        return toDto(collaboratorRepository.save(collaborator));
    }

    public CollaboratorDto updateCollaborator(UUID id, CollaboratorDto dto) {
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found"));

        if (!collaborator.getNationalNumber().equals(dto.getNationalNumber()) &&
                collaboratorRepository.existsByNationalNumber(dto.getNationalNumber())) {
            throw new IllegalArgumentException("A collaborator with this national number already exists");
        }

        updateCollaboratorFromDto(collaborator, dto);
        collaborator.setUpdatedAt(LocalDate.now());

        return toDto(collaboratorRepository.save(collaborator));
    }

    public CollaboratorDto getCollaborator(UUID id) {
        return toDto(collaboratorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collaborator not found")));
    }

    public List<CollaboratorDto> getCollaboratorsByCompany(UUID companyId) {
        return collaboratorRepository.findByCompanyId(companyId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCollaborator(UUID id) {
        if (!collaboratorRepository.existsById(id)) {
            throw new EntityNotFoundException("Collaborator not found");
        }
        
        // First delete all associated dimona records
        List<Dimona> dimonas = dimonaRepository.findByCollaboratorId(id);
        if (!dimonas.isEmpty()) {
            dimonaRepository.deleteAll(dimonas);
        }
        
        // Then delete the collaborator
        collaboratorRepository.deleteById(id);
    }

    public List<CollaboratorDto> getAllCollaborators() {
        return collaboratorRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private void updateCollaboratorFromDto(Collaborator collaborator, CollaboratorDto dto) {
        collaborator.setLastName(dto.getLastName());
        collaborator.setFirstName(dto.getFirstName());
        collaborator.setNationality(dto.getNationality());
        collaborator.setBirthDate(dto.getBirthDate());
        collaborator.setBirthPlace(dto.getBirthPlace());
        collaborator.setGender(dto.getGender());
        collaborator.setLanguage(dto.getLanguage());
        collaborator.setCivilStatus(dto.getCivilStatus());
        collaborator.setCivilStatusDate(dto.getCivilStatusDate());
        collaborator.setPartnerName(dto.getPartnerName());
        collaborator.setPartnerBirthDate(dto.getPartnerBirthDate());
        collaborator.setDependents(dto.getDependents());
        collaborator.setAddress(dto.getAddress());
        collaborator.setNationalNumber(dto.getNationalNumber());
        collaborator.setServiceEntryDate(dto.getServiceEntryDate());
        collaborator.setType(dto.getType());
        collaborator.setJobFunction(dto.getJobFunction());
        collaborator.setContractType(dto.getContractType());
        collaborator.setWorkRegime(dto.getWorkRegime());
        collaborator.setWorkDurationType(dto.getWorkDurationType());
        collaborator.setTypicalSchedule(dto.getTypicalSchedule());
        collaborator.setSalary(dto.getSalary());
        collaborator.setJointCommittee(dto.getJointCommittee());
        collaborator.setTaskDescription(dto.getTaskDescription());
        collaborator.setExtraLegalBenefits(dto.getExtraLegalBenefits());
        collaborator.setIban(dto.getIban());
        collaborator.setEstablishmentUnitAddress(dto.getEstablishmentUnitAddress());
        collaborator.setCompany(companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found")));
    }

    private CollaboratorDto toDto(Collaborator collaborator) {
        CollaboratorDto dto = new CollaboratorDto();
        dto.setId(collaborator.getId());
        dto.setLastName(collaborator.getLastName());
        dto.setFirstName(collaborator.getFirstName());
        dto.setNationality(collaborator.getNationality());
        dto.setBirthDate(collaborator.getBirthDate());
        dto.setBirthPlace(collaborator.getBirthPlace());
        dto.setGender(collaborator.getGender());
        dto.setLanguage(collaborator.getLanguage());
        dto.setCivilStatus(collaborator.getCivilStatus());
        dto.setCivilStatusDate(collaborator.getCivilStatusDate());
        dto.setPartnerName(collaborator.getPartnerName());
        dto.setPartnerBirthDate(collaborator.getPartnerBirthDate());
        dto.setDependents(collaborator.getDependents());
        dto.setAddress(collaborator.getAddress());
        dto.setNationalNumber(collaborator.getNationalNumber());
        dto.setServiceEntryDate(collaborator.getServiceEntryDate());
        dto.setType(collaborator.getType());
        dto.setJobFunction(collaborator.getJobFunction());
        dto.setContractType(collaborator.getContractType());
        dto.setWorkRegime(collaborator.getWorkRegime());
        dto.setWorkDurationType(collaborator.getWorkDurationType());
        dto.setTypicalSchedule(collaborator.getTypicalSchedule());
        dto.setSalary(collaborator.getSalary());
        dto.setJointCommittee(collaborator.getJointCommittee());
        dto.setTaskDescription(collaborator.getTaskDescription());
        dto.setExtraLegalBenefits(collaborator.getExtraLegalBenefits());
        dto.setIban(collaborator.getIban());
        dto.setCompanyId(collaborator.getCompany().getId());
        dto.setEstablishmentUnitAddress(collaborator.getEstablishmentUnitAddress());
        dto.setCreatedAt(collaborator.getCreatedAt());
        dto.setUpdatedAt(collaborator.getUpdatedAt());
        return dto;
    }
}
