package com.socialsecretariat.espacepartage.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Crée un utilisateur administrateur si aucun n'existe
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setActive(true);

                Set<User.Role> roles = new HashSet<>();
                roles.add(User.Role.ROLE_ADMIN);
                admin.setRoles(roles);

                userRepository.save(admin);

                System.out.println("Utilisateur administrateur créé avec succès.");
            }
        };
    }
}