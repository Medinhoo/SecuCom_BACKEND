package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.SocialSecretariatDto;
import com.socialsecretariat.espacepartage.model.SocialSecretariat;
import com.socialsecretariat.espacepartage.repository.SocialSecretariatRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SocialSecretariatService {

    private final SocialSecretariatRepository socialSecretariatRepository;

    public SocialSecretariatService(SocialSecretariatRepository socialSecretariatRepository) {
        this.socialSecretariatRepository = socialSecretariatRepository;
    }

    // Create a new social secretariat
    @Transactional
    public SocialSecretariatDto createSocialSecretariat(SocialSecretariatDto socialSecretariatDto) {
        // Check if company number already exists
        if (socialSecretariatRepository.existsByCompanyNumber(socialSecretariatDto.getCompanyNumber())) {
            throw new IllegalArgumentException("A social secretariat with this company number already exists");
        }

        // Convert DTO to entity
        SocialSecretariat socialSecretariat = convertToEntity(socialSecretariatDto);

        // Save entity
        SocialSecretariat savedSecretariat = socialSecretariatRepository.save(socialSecretariat);

        // Return DTO of saved entity
        return convertToDto(savedSecretariat);
    }

    // Get all social secretariats
    @Transactional(readOnly = true)
    public List<SocialSecretariatDto> getAllSocialSecretariats() {
        return socialSecretariatRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get a social secretariat by ID
    @Transactional(readOnly = true)
    public SocialSecretariatDto getSocialSecretariatById(UUID id) {
        SocialSecretariat socialSecretariat = socialSecretariatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Social secretariat not found with ID: " + id));

        return convertToDto(socialSecretariat);
    }

    // Update a social secretariat
    @Transactional
    public SocialSecretariatDto updateSocialSecretariat(UUID id, SocialSecretariatDto socialSecretariatDto) {
        // Check if social secretariat exists
        SocialSecretariat existingSecretariat = socialSecretariatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Social secretariat not found with ID: " + id));

        // Check if company number is being changed and if new one already exists
        if (!existingSecretariat.getCompanyNumber().equals(socialSecretariatDto.getCompanyNumber()) &&
                socialSecretariatRepository.existsByCompanyNumber(socialSecretariatDto.getCompanyNumber())) {
            throw new IllegalArgumentException("A social secretariat with this company number already exists");
        }

        // Update fields
        existingSecretariat.setName(socialSecretariatDto.getName());
        existingSecretariat.setCompanyNumber(socialSecretariatDto.getCompanyNumber());
        existingSecretariat.setAddress(socialSecretariatDto.getAddress());
        existingSecretariat.setPhone(socialSecretariatDto.getPhone());
        existingSecretariat.setEmail(socialSecretariatDto.getEmail());
        existingSecretariat.setWebsite(socialSecretariatDto.getWebsite());

        // Save updated entity
        SocialSecretariat updatedSecretariat = socialSecretariatRepository.save(existingSecretariat);

        // Return DTO of updated entity
        return convertToDto(updatedSecretariat);
    }

    // Delete a social secretariat
    @Transactional
    public void deleteSocialSecretariat(UUID id) {
        // Check if social secretariat exists
        if (!socialSecretariatRepository.existsById(id)) {
            throw new EntityNotFoundException("Social secretariat not found with ID: " + id);
        }

        // Delete the social secretariat
        socialSecretariatRepository.deleteById(id);
    }

    // Search social secretariats by name
    @Transactional(readOnly = true)
    public List<SocialSecretariatDto> searchSocialSecretariatsByName(String name) {
        return socialSecretariatRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper method to convert entity to DTO
    private SocialSecretariatDto convertToDto(SocialSecretariat socialSecretariat) {
        SocialSecretariatDto dto = new SocialSecretariatDto();
        dto.setId(socialSecretariat.getId());
        dto.setName(socialSecretariat.getName());
        dto.setCompanyNumber(socialSecretariat.getCompanyNumber());
        dto.setAddress(socialSecretariat.getAddress());
        dto.setPhone(socialSecretariat.getPhone());
        dto.setEmail(socialSecretariat.getEmail());
        dto.setWebsite(socialSecretariat.getWebsite());
        return dto;
    }

    // Helper method to convert DTO to entity
    private SocialSecretariat convertToEntity(SocialSecretariatDto dto) {
        SocialSecretariat entity = new SocialSecretariat();
        // Don't set ID for new entities
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setName(dto.getName());
        entity.setCompanyNumber(dto.getCompanyNumber());
        entity.setAddress(dto.getAddress());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setWebsite(dto.getWebsite());
        return entity;
    }
}