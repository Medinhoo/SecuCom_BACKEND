package com.socialsecretariat.espacepartage.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.socialsecretariat.espacepartage.model.Company;
import com.socialsecretariat.espacepartage.model.CompanyContact;
import com.socialsecretariat.espacepartage.model.SecretariatEmployee;
import com.socialsecretariat.espacepartage.model.SocialSecretariat;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.CompanyRepository;
import com.socialsecretariat.espacepartage.repository.CompanyContactRepository;
import com.socialsecretariat.espacepartage.repository.SecretariatEmployeeRepository;
import com.socialsecretariat.espacepartage.repository.SocialSecretariatRepository;
import com.socialsecretariat.espacepartage.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            SocialSecretariatRepository socialSecretariatRepository,
            SecretariatEmployeeRepository secretariatEmployeeRepository,
            CompanyRepository companyRepository,
            CompanyContactRepository companyContactRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Create an admin user if none exists
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setPhoneNumber("+32 123 456 789");
                admin.setAccountStatus(User.AccountStatus.ACTIVE);
                admin.setCreatedAt(LocalDateTime.now());

                Set<User.Role> roles = new HashSet<>();
                roles.add(User.Role.ROLE_ADMIN);
                admin.setRoles(roles);

                userRepository.save(admin);
                System.out.println("Admin user created successfully.");

                // Create a sample secretariat user
                User secretariat = new User();
                secretariat.setUsername("secretariat");
                secretariat.setEmail("secretariat@example.com");
                secretariat.setPassword(passwordEncoder.encode("password"));
                secretariat.setFirstName("Secretariat");
                secretariat.setLastName("User");
                secretariat.setPhoneNumber("+32 987 654 321");
                secretariat.setAccountStatus(User.AccountStatus.ACTIVE);
                secretariat.setCreatedAt(LocalDateTime.now());

                Set<User.Role> secretariatRoles = new HashSet<>();
                secretariatRoles.add(User.Role.ROLE_SECRETARIAT);
                secretariat.setRoles(secretariatRoles);

                userRepository.save(secretariat);
                System.out.println("Secretariat user created successfully.");

                // Create a sample company user
                User company = new User();
                company.setUsername("company");
                company.setEmail("company@example.com");
                company.setPassword(passwordEncoder.encode("password"));
                company.setFirstName("Company");
                company.setLastName("User");
                company.setPhoneNumber("+32 456 789 123");
                company.setAccountStatus(User.AccountStatus.ACTIVE);
                company.setCreatedAt(LocalDateTime.now());

                Set<User.Role> companyRoles = new HashSet<>();
                companyRoles.add(User.Role.ROLE_COMPANY);
                company.setRoles(companyRoles);

                userRepository.save(company);
                System.out.println("Company user created successfully.");
            }

            // Create sample social secretariats if none exist
            if (socialSecretariatRepository.count() == 0) {
                SocialSecretariat acerta = new SocialSecretariat();
                acerta.setName("Acerta");
                acerta.setCompanyNumber("BE0123456789");
                acerta.setAddress("123 Main Street, 1000 Brussels, Belgium");
                acerta.setPhone("+32 2 123 45 67");
                acerta.setEmail("contact@acerta.be");
                acerta.setWebsite("https://www.acerta.be");

                SocialSecretariat savedAcerta = socialSecretariatRepository.save(acerta);
                System.out.println("Acerta social secretariat created successfully.");

                SocialSecretariat securex = new SocialSecretariat();
                securex.setName("Securex");
                securex.setCompanyNumber("BE9876543210");
                securex.setAddress("456 Business Avenue, 2000 Antwerp, Belgium");
                securex.setPhone("+32 3 765 43 21");
                securex.setEmail("info@securex.be");
                securex.setWebsite("https://www.securex.be");

                SocialSecretariat savedSecurex = socialSecretariatRepository.save(securex);
                System.out.println("Securex social secretariat created successfully.");

                // Create secretariat employees if none exist
                if (secretariatEmployeeRepository.count() == 0) {
                    // Create an employee for Acerta
                    SecretariatEmployee acertaEmployee = new SecretariatEmployee();
                    acertaEmployee.setFirstName("Jean");
                    acertaEmployee.setLastName("Dupont");
                    acertaEmployee.setUsername("jean.dupont");
                    acertaEmployee.setEmail("jean.dupont@acerta.be");
                    acertaEmployee.setPhoneNumber("+32 2 123 45 68");
                    acertaEmployee.setPassword(passwordEncoder.encode("password"));
                    acertaEmployee.setPosition("Consultant RH");
                    acertaEmployee.setSpecialization("Paie");
                    acertaEmployee.setSecretariat(savedAcerta);
                    acertaEmployee.setAccountStatus(User.AccountStatus.ACTIVE);
                    acertaEmployee.setCreatedAt(LocalDateTime.now());

                    Set<User.Role> employeeRoles = new HashSet<>();
                    employeeRoles.add(User.Role.ROLE_SECRETARIAT);
                    acertaEmployee.setRoles(employeeRoles);

                    secretariatEmployeeRepository.save(acertaEmployee);

                    // Ensure bidirectional relationship
                    savedAcerta.getEmployees().add(acertaEmployee);
                    socialSecretariatRepository.save(savedAcerta);

                    System.out.println("Acerta employee created successfully.");

                    // Create another employee for Acerta
                    SecretariatEmployee acertaEmployee2 = new SecretariatEmployee();
                    acertaEmployee2.setFirstName("Sophie");
                    acertaEmployee2.setLastName("Martin");
                    acertaEmployee2.setUsername("sophie.martin");
                    acertaEmployee2.setEmail("sophie.martin@acerta.be");
                    acertaEmployee2.setPhoneNumber("+32 2 123 45 69");
                    acertaEmployee2.setPassword(passwordEncoder.encode("password"));
                    acertaEmployee2.setPosition("Conseiller Juridique");
                    acertaEmployee2.setSpecialization("Droit du Travail");
                    acertaEmployee2.setSecretariat(savedAcerta);
                    acertaEmployee2.setAccountStatus(User.AccountStatus.ACTIVE);
                    acertaEmployee2.setCreatedAt(LocalDateTime.now());
                    acertaEmployee2.setRoles(employeeRoles);

                    secretariatEmployeeRepository.save(acertaEmployee2);

                    // Ensure bidirectional relationship
                    savedAcerta.getEmployees().add(acertaEmployee2);
                    socialSecretariatRepository.save(savedAcerta);

                    System.out.println("Second Acerta employee created successfully.");

                    // Create an employee for Securex
                    SecretariatEmployee securexEmployee = new SecretariatEmployee();
                    securexEmployee.setFirstName("Marie");
                    securexEmployee.setLastName("Dubois");
                    securexEmployee.setUsername("marie.dubois");
                    securexEmployee.setEmail("marie.dubois@securex.be");
                    securexEmployee.setPhoneNumber("+32 3 765 43 22");
                    securexEmployee.setPassword(passwordEncoder.encode("password"));
                    securexEmployee.setPosition("Consultant Senior");
                    securexEmployee.setSpecialization("Sécurité Sociale");
                    securexEmployee.setSecretariat(savedSecurex);
                    securexEmployee.setAccountStatus(User.AccountStatus.ACTIVE);
                    securexEmployee.setCreatedAt(LocalDateTime.now());
                    securexEmployee.setRoles(employeeRoles);

                    secretariatEmployeeRepository.save(securexEmployee);

                    // Ensure bidirectional relationship
                    savedSecurex.getEmployees().add(securexEmployee);
                    socialSecretariatRepository.save(savedSecurex);

                    System.out.println("Securex employee created successfully.");
                }
            }

            // Create sample companies if none exist
            if (companyRepository.count() == 0) {
                // Create first company
                Company company1 = new Company();
                company1.setName("Solutions Informatiques TechCorp");
                company1.setPhoneNumber("+32 2 555 12 34");
                company1.setEmail("contact@techcorp.be");
                company1.setIBAN("BE68 5390 0754 7034");
                company1.setSecurityFund("123456-78");
                company1.setWorkAccidentInsurance("ETHIAS-789456");
                company1.setBceNumber("0123.456.789");
                company1.setOnssNumber("RSZ123456789");
                company1.setLegalForm("SPRL");
                company1.setCompanyName("Solutions Informatiques TechCorp SPRL");
                company1.setCreationDate(LocalDate.of(2020, 1, 15));
                company1.setVatNumber("BE0123456789");
                company1.setWorkRegime("40h/semaine");
                company1.setSalaryReduction("20%");
                company1.setActivitySector("Services Informatiques");
                company1.setJointCommittees(List.of("200", "337"));
                company1.setCategory("PME");
                company1.setWorkCalendar("Standard");
                company1.setCollaborationStartDate(LocalDate.of(2023, 1, 1));
                company1.setSubscriptionFormula("Premium");
                company1.setDeclarationFrequency("Mensuelle");

                Company savedCompany1 = companyRepository.save(company1);
                System.out.println("First company created successfully.");

                // Create second company
                Company company2 = new Company();
                company2.setName("BelConstruction");
                company2.setPhoneNumber("+32 2 555 98 76");
                company2.setEmail("info@belconstruction.be");
                company2.setIBAN("BE71 3350 0254 9869");
                company2.setSecurityFund("987654-32");
                company2.setWorkAccidentInsurance("AG-123789");
                company2.setBceNumber("9876.543.210");
                company2.setOnssNumber("RSZ987654321");
                company2.setLegalForm("SA");
                company2.setCompanyName("BelConstruction SA");
                company2.setCreationDate(LocalDate.of(2018, 6, 20));
                company2.setVatNumber("BE9876543210");
                company2.setWorkRegime("38h/semaine");
                company2.setSalaryReduction("15%");
                company2.setActivitySector("Construction");
                company2.setJointCommittees(List.of("124", "200"));
                company2.setCategory("Grande Entreprise");
                company2.setWorkCalendar("Flexible");
                company2.setCollaborationStartDate(LocalDate.of(2023, 3, 1));
                company2.setSubscriptionFormula("Standard");
                company2.setDeclarationFrequency("Trimestrielle");

                Company savedCompany2 = companyRepository.save(company2);
                System.out.println("Second company created successfully.");

                // Create company contacts if none exist
                if (companyContactRepository.count() == 0) {
                    // Create contacts for first company
                    CompanyContact contact1 = new CompanyContact();
                    contact1.setFirstName("Thomas");
                    contact1.setLastName("Lambert");
                    contact1.setUsername("thomas.lambert");
                    contact1.setEmail("t.lambert@techcorp.be");
                    contact1.setPhoneNumber("+32 2 555 12 35");
                    contact1.setPassword(passwordEncoder.encode("password"));
                    contact1.setFonction("Directeur RH");
                    contact1.setPermissions("FULL_ACCESS");
                    contact1.setCompany(savedCompany1);
                    contact1.setAccountStatus(User.AccountStatus.ACTIVE);
                    contact1.setCreatedAt(LocalDateTime.now());

                    Set<User.Role> contactRoles = new HashSet<>();
                    contactRoles.add(User.Role.ROLE_COMPANY);
                    contact1.setRoles(contactRoles);

                    companyContactRepository.save(contact1);

                    // Ensure bidirectional relationship
                    savedCompany1.getContacts().add(contact1);
                    companyRepository.save(savedCompany1);

                    System.out.println("First company contact created successfully.");

                    // Create another contact for first company
                    CompanyContact contact2 = new CompanyContact();
                    contact2.setFirstName("Julie");
                    contact2.setLastName("Dubois");
                    contact2.setUsername("julie.dubois");
                    contact2.setEmail("j.dubois@techcorp.be");
                    contact2.setPhoneNumber("+32 2 555 12 36");
                    contact2.setPassword(passwordEncoder.encode("password"));
                    contact2.setFonction("Responsable Paie");
                    contact2.setPermissions("PAYROLL_ACCESS");
                    contact2.setCompany(savedCompany1);
                    contact2.setAccountStatus(User.AccountStatus.ACTIVE);
                    contact2.setCreatedAt(LocalDateTime.now());
                    contact2.setRoles(contactRoles);

                    companyContactRepository.save(contact2);

                    // Ensure bidirectional relationship
                    savedCompany1.getContacts().add(contact2);
                    companyRepository.save(savedCompany1);

                    System.out.println("Second company contact created successfully.");

                    // Create contact for second company
                    CompanyContact contact3 = new CompanyContact();
                    contact3.setFirstName("Marc");
                    contact3.setLastName("Leroy");
                    contact3.setUsername("marc.leroy");
                    contact3.setEmail("m.leroy@belconstruction.be");
                    contact3.setPhoneNumber("+32 2 555 98 77");
                    contact3.setPassword(passwordEncoder.encode("password"));
                    contact3.setFonction("Directeur Administratif");
                    contact3.setPermissions("FULL_ACCESS");
                    contact3.setCompany(savedCompany2);
                    contact3.setAccountStatus(User.AccountStatus.ACTIVE);
                    contact3.setCreatedAt(LocalDateTime.now());
                    contact3.setRoles(contactRoles);

                    companyContactRepository.save(contact3);

                    // Ensure bidirectional relationship
                    savedCompany2.getContacts().add(contact3);
                    companyRepository.save(savedCompany2);

                    System.out.println("Third company contact created successfully.");
                }
            }
        };
    }
}
