package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.cbe.CbeApiResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CbeApiService {

    private static final Logger logger = LoggerFactory.getLogger(CbeApiService.class);

    @Value("${cbe.api.base-url:https://cbeapi.be/api/v1}")
    private String baseUrl;

    @Value("${cbe.api.token:}")
    private String token;

    private final RestTemplate restTemplate;

    public CbeApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get company information by CBE/VAT number
     * @param cbeNumber The CBE or VAT number (supports formats: 1234567890, BE1234567890, 1234.567.890)
     * @return CbeApiResponseDto containing company data
     * @throws RestClientException if API call fails
     */
    public CbeApiResponseDto getCompanyByNumber(String cbeNumber) {
        validateTokenConfiguration();
        
        String url = baseUrl + "/company/" + cbeNumber;
        
        logger.info("Calling CBE API for company number: {}", cbeNumber);
        
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<CbeApiResponseDto> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                CbeApiResponseDto.class
            );
            
            logger.info("Successfully retrieved company data for number: {}", cbeNumber);
            return response.getBody();
            
        } catch (RestClientException e) {
            logger.error("Error calling CBE API for company number {}: {}", cbeNumber, e.getMessage());
            throw e;
        }
    }

    /**
     * Search companies by name
     * @param name The company name to search for
     * @return CbeApiResponseDto containing matching companies
     * @throws RestClientException if API call fails
     */
    public CbeApiResponseDto searchCompaniesByName(String name) {
        String url = baseUrl + "/company/search?name=" + name;
        
        logger.info("Searching CBE API for company name: {}", name);
        
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<CbeApiResponseDto> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                CbeApiResponseDto.class
            );
            
            logger.info("Successfully searched companies by name: {}", name);
            return response.getBody();
            
        } catch (RestClientException e) {
            logger.error("Error searching CBE API for company name {}: {}", name, e.getMessage());
            throw e;
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    private void validateTokenConfiguration() {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("CBE API token is not configured. Please set the 'cbe.api.token' property.");
        }
    }
}
