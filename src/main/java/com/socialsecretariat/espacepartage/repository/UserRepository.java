package com.socialsecretariat.espacepartage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialsecretariat.espacepartage.model.User;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find a user by username
    Optional<User> findByUsername(String username);

    // Find a user by email
    Optional<User> findByEmail(String email);

    // Check if a username already exists
    Boolean existsByUsername(String username);

    // Check if an email already exists
    Boolean existsByEmail(String email);

    // Find users by role
    List<User> findByRolesContaining(User.Role role);

    // Find active users
    List<User> findByAccountStatus(User.AccountStatus status);

    // Find users by first name and last name (for search functionality)
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}