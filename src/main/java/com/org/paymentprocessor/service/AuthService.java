package com.org.paymentprocessor.service;

import com.org.paymentprocessor.dto.auth.AuthResponse;
import com.org.paymentprocessor.dto.auth.LoginRequest;
import com.org.paymentprocessor.model.User;
import com.org.paymentprocessor.repository.UserRepository;
import com.org.paymentprocessor.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "User not found"
                        )
                );

        String token =
                jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}