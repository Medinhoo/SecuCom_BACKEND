package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.CompanyDto;
import com.socialsecretariat.espacepartage.model.Company;
import com.socialsecretariat.espacepartage.model.Collaborator;
import com.socialsecretariat.espacepartage.model.Dimona;
import com.socialsecretariat.espacepartage.repository.CompanyRepository;
import com.socialsecretariat.espacepartage.repository.DimonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final DimonaRepository dimonaRepository;

    public CompanyService(CompanyRepository companyRepository, DimonaRepository dimonaRepository) {
        this.companyRepository = companyRepository;
        this.dimonaRepository = dimonaRepository;
    }

    public CompanyDto createCompany(CompanyDto companyDto) {
        Company company = new Company();
        BeanUtils.copyProperties(companyDto, company);
        Company savedCompany = companyRepository.save(company);
        CompanyDto savedDto = new CompanyDto();
        BeanUtils.copyProperties(savedCompany, savedDto);
        return savedDto;
    }

    public CompanyDto updateCompany(UUID id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));

        BeanUtils.copyProperties(companyDto, company, "id");
        Company updatedCompany = companyRepository.save(company);
        CompanyDto updatedDto = new CompanyDto();
        BeanUtils.copyProperties(updatedCompany, updatedDto);
        return updatedDto;
    }

    public CompanyDto getCompanyById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));

        CompanyDto companyDto = new CompanyDto();
        BeanUtils.copyProperties(company, companyDto);
        return companyDto;
    }

    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(company -> {
                    CompanyDto dto = new CompanyDto();
                    BeanUtils.copyProperties(company, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCompany(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
        
        // First, delete all dimona records associated with the company's collaborators
        for (Collaborator collaborator : company.getCollaborators()) {
            List<Dimona> dimonas = dimonaRepository.findByCollaboratorId(collaborator.getId());
            if (!dimonas.isEmpty()) {
                dimonaRepository.deleteAll(dimonas);
            }
        }
        
        // Also delete dimona records directly associated with the company
        List<Dimona> companyDimonas = dimonaRepository.findByCompanyId(id);
        if (!companyDimonas.isEmpty()) {
            dimonaRepository.deleteAll(companyDimonas);
        }
        
        // Now delete the company (which will cascade delete the collaborators)
        companyRepository.deleteById(id);
    }

    public boolean existsByBceNumber(String bceNumber) {
        return companyRepository.existsByBceNumber(bceNumber);
    }

    public boolean existsByOnssNumber(String onssNumber) {
        return companyRepository.existsByOnssNumber(onssNumber);
    }

    public boolean existsByVatNumber(String vatNumber) {
        return companyRepository.existsByVatNumber(vatNumber);
    }
}
