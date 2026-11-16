package com.example.ticketbooking.service;

import com.example.ticketbooking.dto.AuthRequest;
import com.example.ticketbooking.dto.AuthResponse;
import com.example.ticketbooking.dto.RegisterRequest;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.repository.UserRepository;
import com.example.ticketbooking.security.JwtTokenProvider;
import com.example.ticketbooking.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and login logic.
 * On register: hashes password, persists user, generates JWT.
 * On login: validates credentials, generates JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Register a new CUSTOMER user.
     * Throws IllegalArgumentException if the email is already taken.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        // Build and persist the user with hashed password
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("CUSTOMER")
                .active(true)
                .build();
        userRepository.save(user);

        // Generate JWT for immediate login after registration
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole());
    }

    /**
     * Authenticate an existing user by email and password.
     * Throws BadCredentialsException if credentials are invalid.
     */
    public AuthResponse login(AuthRequest request) {
        // Look up user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Verify password matches the stored hash
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);

        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole());
    }
}
