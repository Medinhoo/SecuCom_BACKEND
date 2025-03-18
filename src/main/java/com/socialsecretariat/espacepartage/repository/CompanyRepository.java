package com.socialsecretariat.espacepartage.repository;

import com.socialsecretariat.espacepartage.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findByBceNumber(String bceNumber);

    Optional<Company> findByOnssNumber(String onssNumber);

    Optional<Company> findByVatNumber(String vatNumber);

    boolean existsByBceNumber(String bceNumber);

    boolean existsByOnssNumber(String onssNumber);

    boolean existsByVatNumber(String vatNumber);
}
