package com.socialsecretariat.espacepartage.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User updateUser(User user) {
        // Check if user exists
        userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Handle password update if provided
        User existingUser = userRepository.findById(user.getId()).get();

        // Only update password if a new one is provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // Check if the password is already encoded
            if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                // This is a new password, encode it
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // This is the same password, keep the existing encoded version
                user.setPassword(existingUser.getPassword());
            }
        } else {
            // No password provided, keep the existing one
            user.setPassword(existingUser.getPassword());
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserPassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRolesContaining(role);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByAccountStatus(User.AccountStatus status) {
        return userRepository.findByAccountStatus(status);
    }

    @Transactional(readOnly = true)
    public List<User> searchUsersByName(String searchTerm) {
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm);
    }
}