package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.CompanyLookupDto;
import com.socialsecretariat.espacepartage.dto.cbe.CbeApiResponseDto;
import com.socialsecretariat.espacepartage.dto.cbe.CbeCompanyDto;
import com.socialsecretariat.espacepartage.model.Address;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyLookupService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyLookupService.class);

    private final CbeApiService cbeApiService;

    public CompanyLookupService(CbeApiService cbeApiService) {
        this.cbeApiService = cbeApiService;
    }

    /**
     * Find company by BCE number
     * @param bceNumber The BCE number to search for
     * @return CompanyLookupDto with company information
     * @throws EntityNotFoundException if company not found
     * @throws RestClientException if API call fails
     */
    public CompanyLookupDto findByBceNumber(String bceNumber) {
        logger.info("Looking up company by BCE number: {}", bceNumber);
        
        try {
            CbeApiResponseDto response = cbeApiService.getCompanyByNumber(bceNumber);
            
            if (response == null || response.getData() == null) {
                throw new EntityNotFoundException("Company not found with BCE number: " + bceNumber);
            }
            
            CbeCompanyDto cbeCompany = response.getData();
            CompanyLookupDto result = mapToCompanyLookupDto(cbeCompany);
            
            logger.info("Successfully found company: {} with BCE number: {}", result.getName(), bceNumber);
            return result;
            
        } catch (RestClientException e) {
            logger.error("Failed to lookup company with BCE number {}: {}", bceNumber, e.getMessage());
            throw e;
        }
    }

    /**
     * Find company by VAT number
     * VAT numbers are essentially BCE numbers with BE prefix
     * @param vatNumber The VAT number to search for
     * @return CompanyLookupDto with company information
     * @throws EntityNotFoundException if company not found
     * @throws RestClientException if API call fails
     */
    public CompanyLookupDto findByVatNumber(String vatNumber) {
        logger.info("Looking up company by VAT number: {}", vatNumber);
        
        // CBE API accepts both BCE and VAT formats
        return findByBceNumber(vatNumber);
    }

    /**
     * Search companies by name
     * @param name The company name to search for
     * @return List of CompanyLookupDto matching the search criteria
     * @throws RestClientException if API call fails
     */
    public List<CompanyLookupDto> searchByName(String name) {
        logger.info("Searching companies by name: {}", name);
        
        try {
            CbeApiResponseDto response = cbeApiService.searchCompaniesByName(name);
            
            if (response == null || response.getData() == null) {
                logger.info("No companies found for name: {}", name);
                return List.of();
            }
            
            // For search by name, we get a single company result, so we wrap it in a list
            CompanyLookupDto result = mapToCompanyLookupDto(response.getData());
            List<CompanyLookupDto> results = List.of(result);
            
            logger.info("Found {} companies for name: {}", results.size(), name);
            return results;
            
        } catch (RestClientException e) {
            logger.error("Failed to search companies by name {}: {}", name, e.getMessage());
            throw e;
        }
    }

    /**
     * Map CBE API response to our internal DTO
     * @param cbeCompany The CBE company data
     * @return CompanyLookupDto mapped from CBE data
     */
    private CompanyLookupDto mapToCompanyLookupDto(CbeCompanyDto cbeCompany) {
        CompanyLookupDto dto = new CompanyLookupDto();
        
        // Basic company information
        dto.setBceNumber(cbeCompany.getCbeNumber());
        dto.setBceNumberFormatted(cbeCompany.getCbeNumberFormatted());
        dto.setName(cbeCompany.getDenomination());
        dto.setCompanyName(cbeCompany.getDenominationWithLegalForm());
        dto.setLegalForm(cbeCompany.getJuridicalForm());
        dto.setLegalFormShort(cbeCompany.getJuridicalFormShort());
        dto.setStartDate(cbeCompany.getStartDate());
        dto.setStatus(cbeCompany.getStatus());
        
        // Contact information
        if (cbeCompany.getContactInfos() != null) {
            dto.setEmail(cbeCompany.getContactInfos().getEmail());
            dto.setPhoneNumber(cbeCompany.getContactInfos().getPhone());
            dto.setWebsite(cbeCompany.getContactInfos().getWeb());
        }
        
        // Address mapping
        if (cbeCompany.getAddress() != null) {
            Address address = new Address();
            address.setStreet(cbeCompany.getAddress().getStreet());
            address.setNumber(cbeCompany.getAddress().getStreetNumber());
            address.setBox(cbeCompany.getAddress().getBox());
            address.setPostalCode(cbeCompany.getAddress().getPostCode());
            address.setCity(cbeCompany.getAddress().getCity());
            
            dto.setAddress(address);
        }
        
        return dto;
    }
}
