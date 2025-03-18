package com.socialsecretariat.espacepartage.repository;

import com.socialsecretariat.espacepartage.model.CompanyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyContactRepository extends JpaRepository<CompanyContact, UUID> {
    List<CompanyContact> findByCompanyId(UUID companyId);
}
