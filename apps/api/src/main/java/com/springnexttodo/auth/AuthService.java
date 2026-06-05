package com.springnexttodo.auth;

import com.springnexttodo.auth.dto.AuthResponse;
import com.springnexttodo.auth.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new EmailAlreadyUsedException(req.email());
        }
        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        return AuthResponse.from(userRepository.save(user));
    }

    public AuthResponse findByEmail(String email) {
        return AuthResponse.from(getUser(email));
    }

    User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found: " + email));
    }
}
