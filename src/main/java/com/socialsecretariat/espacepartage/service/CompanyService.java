package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.CompanyDto;
import com.socialsecretariat.espacepartage.model.Company;
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
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
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

    public void deleteCompany(UUID id) {
        if (!companyRepository.existsById(id)) {
            throw new EntityNotFoundException("Company not found with id: " + id);
        }
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
