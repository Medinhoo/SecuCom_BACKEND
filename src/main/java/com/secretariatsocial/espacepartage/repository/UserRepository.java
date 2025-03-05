package com.secretariatsocial.espacepartage.repository;

import com.secretariatsocial.espacepartage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Trouver un utilisateur par son username
    Optional<User> findByUsername(String username);

    // Trouver un utilisateur par son email
    Optional<User> findByEmail(String email);

    // Vérifier si un username existe déjà
    Boolean existsByUsername(String username);

    // Vérifier si un email existe déjà
    Boolean existsByEmail(String email);
}