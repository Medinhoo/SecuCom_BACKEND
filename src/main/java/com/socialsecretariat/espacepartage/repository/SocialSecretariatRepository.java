package com.socialsecretariat.espacepartage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialsecretariat.espacepartage.model.SocialSecretariat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SocialSecretariatRepository extends JpaRepository<SocialSecretariat, UUID> {

    // Find by name (case-insensitive)
    Optional<SocialSecretariat> findByNameIgnoreCase(String name);

    // Find by company number
    Optional<SocialSecretariat> findByCompanyNumber(String companyNumber);

    // Check if exists by company number
    boolean existsByCompanyNumber(String companyNumber);

    // Search by name containing
    List<SocialSecretariat> findByNameContainingIgnoreCase(String name);

    // Find by email
    Optional<SocialSecretariat> findByEmail(String email);
}