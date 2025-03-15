package com.socialsecretariat.espacepartage.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.socialsecretariat.espacepartage.model.SecretariatEmployee;
import com.socialsecretariat.espacepartage.model.SocialSecretariat;
import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.SecretariatEmployeeRepository;
import com.socialsecretariat.espacepartage.repository.SocialSecretariatRepository;
import com.socialsecretariat.espacepartage.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            SocialSecretariatRepository socialSecretariatRepository,
            SecretariatEmployeeRepository secretariatEmployeeRepository,
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
                    acertaEmployee.setPosition("HR Consultant");
                    acertaEmployee.setSpecialization("Payroll");
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
                    acertaEmployee2.setPosition("Legal Advisor");
                    acertaEmployee2.setSpecialization("Labor Law");
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
                    securexEmployee.setPosition("Senior Consultant");
                    securexEmployee.setSpecialization("Social Security");
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
        };
    }
}