package com.socialsecretariat.espacepartage.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.socialsecretariat.espacepartage.model.User;
import com.socialsecretariat.espacepartage.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        // Map our custom User entity to Spring Security's UserDetails interface
        // Parameters:
        // 1. username - the user's username
        // 2. password - the encoded password
        // 3. enabled - true if the account is ACTIVE (user can log in)
        // 4. accountNonExpired - true if the account is not expired (we don't use
        // expiration, so always true)
        // 5. credentialsNonExpired - true if credentials haven't expired (we don't use
        // credential expiration, so always true)
        // 6. accountNonLocked - true if the account is not locked (false when status is
        // LOCKED)
        // 7. authorities - the user's granted authorities/roles
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getAccountStatus() == User.AccountStatus.ACTIVE,
                true,
                true,
                user.getAccountStatus() != User.AccountStatus.LOCKED,
                authorities);
    }
}