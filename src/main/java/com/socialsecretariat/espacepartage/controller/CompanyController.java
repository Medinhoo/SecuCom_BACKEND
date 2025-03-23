package com.socialsecretariat.espacepartage.controller;

import com.socialsecretariat.espacepartage.dto.CompanyDto;
import com.socialsecretariat.espacepartage.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Company management operations.
 * Provides endpoints for creating, reading, updating, and deleting companies.
 * Access to endpoints is restricted based on user roles (ADMIN and
 * SECRETARIAT).
 */
@RestController
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Creates a new company in the system.
     * Only accessible to administrators.
     * 
     * @param companyDto The company data transfer object containing the company
     *                   details
     * @return The created company as a DTO with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(@Valid @RequestBody CompanyDto companyDto) {
        CompanyDto createdCompany = companyService.createCompany(companyDto);
        return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
    }

    /**
     * Updates an existing company's information.
     * Only accessible to administrators.
     * 
     * @param id         The UUID of the company to update
     * @param companyDto The updated company information
     * @return The updated company as a DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyDto> updateCompany(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyDto companyDto) {
        CompanyDto updatedCompany = companyService.updateCompany(id, companyDto);
        return ResponseEntity.ok(updatedCompany);
    }

    /**
     * Retrieves a company by its ID.
     * Accessible to administrators and secretariat employees.
     * 
     * @param id The UUID of the company to retrieve
     * @return The company details as a DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable UUID id) {
        CompanyDto company = companyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    /**
     * Retrieves all companies in the system.
     * Accessible to administrators and secretariat employees.
     * 
     * @return A list of all companies as DTOs
     */
    @GetMapping
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        List<CompanyDto> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }

    /**
     * Deletes a company from the system.
     * Only accessible to administrators.
     * 
     * @param id The UUID of the company to delete
     * @return HTTP 204 No Content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks if a BCE number already exists in the system.
     * Accessible to administrators and secretariat employees.
     * 
     * @param bceNumber The BCE number to check
     * @return Boolean indicating whether the BCE number exists
     */
    @GetMapping("/check/bce/{bceNumber}")
    public ResponseEntity<Boolean> checkBceNumberExists(@PathVariable String bceNumber) {
        boolean exists = companyService.existsByBceNumber(bceNumber);
        return ResponseEntity.ok(exists);
    }

    /**
     * Checks if an ONSS number already exists in the system.
     * Accessible to administrators and secretariat employees.
     * 
     * @param onssNumber The ONSS number to check
     * @return Boolean indicating whether the ONSS number exists
     */
    @GetMapping("/check/onss/{onssNumber}")
    public ResponseEntity<Boolean> checkOnssNumberExists(@PathVariable String onssNumber) {
        boolean exists = companyService.existsByOnssNumber(onssNumber);
        return ResponseEntity.ok(exists);
    }

    /**
     * Checks if a VAT number already exists in the system.
     * Accessible to administrators and secretariat employees.
     * 
     * @param vatNumber The VAT number to check
     * @return Boolean indicating whether the VAT number exists
     */
    @GetMapping("/check/vat/{vatNumber}")
    public ResponseEntity<Boolean> checkVatNumberExists(@PathVariable String vatNumber) {
        boolean exists = companyService.existsByVatNumber(vatNumber);
        return ResponseEntity.ok(exists);
    }
}
