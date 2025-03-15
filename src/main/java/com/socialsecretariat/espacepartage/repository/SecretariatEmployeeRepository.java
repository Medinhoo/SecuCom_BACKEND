package com.socialsecretariat.espacepartage.repository;

import com.socialsecretariat.espacepartage.model.SecretariatEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SecretariatEmployeeRepository extends JpaRepository<SecretariatEmployee, UUID> {

    // Find employees by secretariat ID
    List<SecretariatEmployee> findBySecretariatId(UUID secretariatId);

    // Find by position
    List<SecretariatEmployee> findByPosition(String position);

    // Find by specialization
    List<SecretariatEmployee> findBySpecialization(String specialization);

    // Find by secretariat ID and position
    List<SecretariatEmployee> findBySecretariatIdAndPosition(UUID secretariatId, String position);

    // Count employees by secretariat ID
    long countBySecretariatId(UUID secretariatId);
}