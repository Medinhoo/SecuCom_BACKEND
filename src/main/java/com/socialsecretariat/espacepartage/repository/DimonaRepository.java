package com.socialsecretariat.espacepartage.repository;

import com.socialsecretariat.espacepartage.model.Dimona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface DimonaRepository extends JpaRepository<Dimona, UUID> {
    List<Dimona> findByCollaboratorId(UUID collaboratorId);

    List<Dimona> findByCompanyId(UUID companyId);
}
