package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.CompanyDto;
import com.socialsecretariat.espacepartage.model.Company;
import com.socialsecretariat.espacepartage.model.Collaborator;
import com.socialsecretariat.espacepartage.model.Dimona;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.CompanyRepository;
import com.socialsecretariat.espacepartage.repository.DimonaRepository;
import com.socialsecretariat.espacepartage.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final DimonaRepository dimonaRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CompanyService(CompanyRepository companyRepository, DimonaRepository dimonaRepository, 
                         UserRepository userRepository, NotificationService notificationService) {
        this.companyRepository = companyRepository;
        this.dimonaRepository = dimonaRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public CompanyDto createCompany(CompanyDto companyDto) {
        Company company = new Company();
        BeanUtils.copyProperties(companyDto, company);
        Company savedCompany = companyRepository.save(company);
        CompanyDto savedDto = new CompanyDto();
        BeanUtils.copyProperties(savedCompany, savedDto);
        return savedDto;
    }

    public CompanyDto updateCompany(UUID id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));

        // Check if company was incomplete before update
        boolean wasIncomplete = !isCompanyComplete(company);

        BeanUtils.copyProperties(companyDto, company, "id");
        Company updatedCompany = companyRepository.save(company);

        // Check if company is now complete after update
        boolean isNowComplete = isCompanyComplete(updatedCompany);

        // If company was incomplete before and is now complete
        if (wasIncomplete && isNowComplete) {
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                String username = authentication.getName();
                User currentUser = userRepository.findByUsername(username).orElse(null);
                
                if (currentUser != null) {
                    // Update user's account status to ACTIVE if it was PENDING
                    if (currentUser.getAccountStatus() == User.AccountStatus.PENDING) {
                        currentUser.setAccountStatus(User.AccountStatus.ACTIVE);
                        userRepository.save(currentUser);
                    }

                    // Create notification for company completion
                    notificationService.notifyCompanyCompleted(
                        updatedCompany.getId(), 
                        updatedCompany.getName(), 
                        currentUser.getId()
                    );
                }
            }
        }

        CompanyDto updatedDto = new CompanyDto();
        BeanUtils.copyProperties(updatedCompany, updatedDto);
        return updatedDto;
    }

    public CompanyDto getCompanyById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));

        CompanyDto companyDto = new CompanyDto();
        BeanUtils.copyProperties(company, companyDto);
        return companyDto;
    }

    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(company -> {
                    CompanyDto dto = new CompanyDto();
                    BeanUtils.copyProperties(company, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCompany(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
        
        // First, delete all dimona records associated with the company's collaborators
        for (Collaborator collaborator : company.getCollaborators()) {
            List<Dimona> dimonas = dimonaRepository.findByCollaboratorId(collaborator.getId());
            if (!dimonas.isEmpty()) {
                dimonaRepository.deleteAll(dimonas);
            }
        }
        
        // Also delete dimona records directly associated with the company
        List<Dimona> companyDimonas = dimonaRepository.findByCompanyId(id);
        if (!companyDimonas.isEmpty()) {
            dimonaRepository.deleteAll(companyDimonas);
        }
        
        // Now delete the company (which will cascade delete the collaborators)
        companyRepository.deleteById(id);
    }

    public boolean existsByBceNumber(String bceNumber) {
        return companyRepository.existsByBceNumber(bceNumber);
    }

    public boolean existsByOnssNumber(String onssNumber) {
        return companyRepository.existsByOnssNumber(onssNumber);
    }

    public boolean existsByVatNumber(String vatNumber) {
        return companyRepository.existsByVatNumber(vatNumber);
    }

    /**
     * Check if all required fields of a company are completed (non-null and non-empty)
     */
    private boolean isCompanyComplete(Company company) {
        return company.getName() != null && !company.getName().trim().isEmpty() &&
               company.getPhoneNumber() != null && !company.getPhoneNumber().trim().isEmpty() &&
               company.getEmail() != null && !company.getEmail().trim().isEmpty() &&
               company.getIBAN() != null && !company.getIBAN().trim().isEmpty() &&
               company.getSecurityFund() != null && !company.getSecurityFund().trim().isEmpty() &&
               company.getWorkAccidentInsurance() != null && !company.getWorkAccidentInsurance().trim().isEmpty() &&
               company.getBceNumber() != null && !company.getBceNumber().trim().isEmpty() &&
               company.getOnssNumber() != null && !company.getOnssNumber().trim().isEmpty() &&
               company.getLegalForm() != null && !company.getLegalForm().trim().isEmpty() &&
               company.getCompanyName() != null && !company.getCompanyName().trim().isEmpty() &&
               company.getCreationDate() != null &&
               company.getVatNumber() != null && !company.getVatNumber().trim().isEmpty() &&
               company.getWorkRegime() != null && !company.getWorkRegime().trim().isEmpty() &&
               company.getSalaryReduction() != null && !company.getSalaryReduction().trim().isEmpty() &&
               company.getActivitySector() != null && !company.getActivitySector().trim().isEmpty() &&
               company.getJointCommittees() != null && !company.getJointCommittees().isEmpty() &&
               company.getCategory() != null && !company.getCategory().trim().isEmpty() &&
               company.getWorkCalendar() != null && !company.getWorkCalendar().trim().isEmpty() &&
               company.getCollaborationStartDate() != null &&
               company.getSubscriptionFormula() != null && !company.getSubscriptionFormula().trim().isEmpty() &&
               company.getDeclarationFrequency() != null && !company.getDeclarationFrequency().trim().isEmpty();
    }
}
