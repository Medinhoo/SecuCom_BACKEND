package com.socialsecretariat.espacepartage.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.socialsecretariat.espacepartage.model.SocialSecretariat;
import com.socialsecretariat.espacepartage.model.User;
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

            // Create a sample social secretariat if none exists
            if (socialSecretariatRepository.count() == 0) {
                SocialSecretariat socialSecretariat = new SocialSecretariat();
                socialSecretariat.setName("Acerta");
                socialSecretariat.setCompanyNumber("BE0123456789");
                socialSecretariat.setAddress("123 Main Street, 1000 Brussels, Belgium");
                socialSecretariat.setPhone("+32 2 123 45 67");
                socialSecretariat.setEmail("contact@acerta.be");
                socialSecretariat.setWebsite("https://www.acerta.be");

                socialSecretariatRepository.save(socialSecretariat);
                System.out.println("Sample social secretariat created successfully.");

                SocialSecretariat socialSecretariat2 = new SocialSecretariat();
                socialSecretariat2.setName("Securex");
                socialSecretariat2.setCompanyNumber("BE9876543210");
                socialSecretariat2.setAddress("456 Business Avenue, 2000 Antwerp, Belgium");
                socialSecretariat2.setPhone("+32 3 765 43 21");
                socialSecretariat2.setEmail("info@securex.be");
                socialSecretariat2.setWebsite("https://www.securex.be");

                socialSecretariatRepository.save(socialSecretariat2);
                System.out.println("Second sample social secretariat created successfully.");
            }
        };
    }
}