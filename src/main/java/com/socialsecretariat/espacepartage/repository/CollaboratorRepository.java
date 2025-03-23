package com.socialsecretariat.espacepartage.repository;

import com.socialsecretariat.espacepartage.model.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, UUID> {
    List<Collaborator> findByCompanyId(UUID companyId);

    boolean existsByNationalNumber(String nationalNumber);
}
