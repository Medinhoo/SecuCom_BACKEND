package com.socialsecretariat.espacepartage.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
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
        };
    }
}