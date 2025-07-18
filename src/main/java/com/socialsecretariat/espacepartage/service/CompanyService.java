package com.socialsecretariat.espacepartage.service;

import com.socialsecretariat.espacepartage.dto.CompanyDto;
import com.socialsecretariat.espacepartage.dto.CompanyUpdateResponseDto;
import com.socialsecretariat.espacepartage.dto.CompanyConfirmationHistoryDto;
import com.socialsecretariat.espacepartage.model.Company;
import com.socialsecretariat.espacepartage.model.CompanyContact;
import com.socialsecretariat.espacepartage.model.CompanyConfirmationHistory;
import com.socialsecretariat.espacepartage.model.Collaborator;
import com.socialsecretariat.espacepartage.model.Dimona;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.CompanyRepository;
import com.socialsecretariat.espacepartage.repository.CompanyContactRepository;
import com.socialsecretariat.espacepartage.repository.CompanyConfirmationHistoryRepository;
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
    private final CompanyContactRepository companyContactRepository;
    private final CompanyConfirmationHistoryRepository confirmationHistoryRepository;
    private final DimonaRepository dimonaRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CompanyService(CompanyRepository companyRepository, CompanyContactRepository companyContactRepository,
                         CompanyConfirmationHistoryRepository confirmationHistoryRepository,
                         DimonaRepository dimonaRepository, UserRepository userRepository, 
                         NotificationService notificationService) {
        this.companyRepository = companyRepository;
        this.companyContactRepository = companyContactRepository;
        this.confirmationHistoryRepository = confirmationHistoryRepository;
        this.dimonaRepository = dimonaRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public CompanyDto createCompany(CompanyDto companyDto) {
        Company company = new Company();
        BeanUtils.copyProperties(companyDto, company);
        // S'assurer que isCompanyConfirmed est toujours false lors de la création
        company.setCompanyConfirmed(false);
        Company savedCompany = companyRepository.save(company);
        CompanyDto savedDto = new CompanyDto();
        BeanUtils.copyProperties(savedCompany, savedDto);
        return savedDto;
    }

    public CompanyUpdateResponseDto updateCompany(UUID id, CompanyDto companyDto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));

        // Get current user from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.getName() != null) {
            String username = authentication.getName();
            currentUser = userRepository.findByUsername(username).orElse(null);
        }

        boolean wasConfirmed = company.isCompanyConfirmed();

        // Exclure isCompanyConfirmed du BeanUtils.copyProperties pour le gérer manuellement
        BeanUtils.copyProperties(companyDto, company, "id", "isCompanyConfirmed");

        // Sauvegarder d'abord les modifications
        Company updatedCompany = companyRepository.save(company);

        // Vérifier si l'entreprise est complète après la mise à jour
        boolean isNowComplete = isCompanyComplete(updatedCompany);
        
        // Logique selon le rôle de l'utilisateur
        if (currentUser != null) {
            if (currentUser.hasRole(User.Role.ROLE_SECRETARIAT)) {
                // Si le secrétariat modifie, TOUJOURS remettre isCompanyConfirmed à false
                updatedCompany.setCompanyConfirmed(false);
                updatedCompany = companyRepository.save(updatedCompany);
                
                // Notifier seulement si l'entreprise était confirmée avant
                if (wasConfirmed) {
                    notifyCompanyContactsForReconfirmation(updatedCompany);
                }
            } else if (currentUser.hasRole(User.Role.ROLE_COMPANY)) {
                // Si c'est un CompanyContact, vérifier l'intégrité des données
                // Si l'entreprise devient incomplète, mettre isCompanyConfirmed à false automatiquement
                if (!isNowComplete && updatedCompany.isCompanyConfirmed()) {
                    updatedCompany.setCompanyConfirmed(false);
                    updatedCompany = companyRepository.save(updatedCompany);
                }
                // Note: La confirmation se fera via la modal frontend
            }
        } else {
            // Pour les utilisateurs non authentifiés ou autres rôles
            // Si l'entreprise devient incomplète, mettre isCompanyConfirmed à false automatiquement
            if (!isNowComplete && updatedCompany.isCompanyConfirmed()) {
                updatedCompany.setCompanyConfirmed(false);
                updatedCompany = companyRepository.save(updatedCompany);
            }
        }

        // Create response DTO with metadata
        CompanyUpdateResponseDto responseDto = new CompanyUpdateResponseDto();
        BeanUtils.copyProperties(updatedCompany, responseDto);

        // Pour les utilisateurs ROLE_COMPANY : toujours demander confirmation
        if (currentUser != null && currentUser.hasRole(User.Role.ROLE_COMPANY)) {
            responseDto.setNeedsConfirmation(true);
            responseDto.setWasJustCompleted(false); // Pas besoin de cette logique complexe
        }

        return responseDto;
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
     * Confirme les données d'une entreprise. Accessible uniquement aux CompanyContacts de cette entreprise.
     */
    public CompanyDto confirmCompanyData(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + companyId));

        // Vérifier que l'utilisateur connecté est bien un CompanyContact de cette entreprise
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);
            
            if (currentUser != null && isUserCompanyContact(currentUser.getId(), companyId)) {
                company.setCompanyConfirmed(true);
                Company savedCompany = companyRepository.save(company);
                
                // Enregistrer l'historique de confirmation
                CompanyConfirmationHistory history = new CompanyConfirmationHistory();
                history.setCompanyId(companyId);
                history.setConfirmedByUserId(currentUser.getId());
                history.setConfirmedByUserName(currentUser.getFirstName() + " " + currentUser.getLastName());
                confirmationHistoryRepository.save(history);
                
                // Notifier le secrétariat social de la confirmation
                notificationService.notifySecretariatOfCompanyConfirmation(companyId, company.getName());
                
                CompanyDto dto = new CompanyDto();
                BeanUtils.copyProperties(savedCompany, dto);
                return dto;
            } else {
                throw new SecurityException("Only company contacts can confirm company data");
            }
        } else {
            throw new SecurityException("User not authenticated");
        }
    }

    /**
     * Récupère l'historique des confirmations pour une entreprise
     */
    public List<CompanyConfirmationHistoryDto> getConfirmationHistory(UUID companyId) {
        List<CompanyConfirmationHistory> history = confirmationHistoryRepository.findByCompanyIdOrderByConfirmedAtDesc(companyId);
        return history.stream()
                .map(h -> {
                    CompanyConfirmationHistoryDto dto = new CompanyConfirmationHistoryDto();
                    BeanUtils.copyProperties(h, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si un utilisateur est un CompanyContact d'une entreprise donnée
     */
    private boolean isUserCompanyContact(UUID userId, UUID companyId) {
        List<CompanyContact> companyContacts = companyContactRepository.findByCompanyId(companyId);
        return companyContacts.stream().anyMatch(contact -> contact.getId().equals(userId));
    }

    /**
     * Notifie tous les CompanyContacts d'une entreprise qu'ils doivent re-confirmer les données
     */
    private void notifyCompanyContactsForReconfirmation(Company company) {
        List<CompanyContact> companyContacts = companyContactRepository.findByCompanyId(company.getId());
        for (CompanyContact contact : companyContacts) {
            notificationService.notifyCompanyContactForReconfirmation(
                contact.getId(), 
                company.getId(), 
                company.getName()
            );
        }
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
