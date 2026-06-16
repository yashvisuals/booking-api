package com.yash.booking.service;

import com.yash.booking.domain.User;
import com.yash.booking.repo.UserRepository;
import com.yash.booking.security.JwtService;
import com.yash.booking.web.dto.AuthResponse;
import com.yash.booking.web.dto.LoginRequest;
import com.yash.booking.web.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already registered");
        }
        User user = new User(req.email(), passwordEncoder.encode(req.password()), req.role());
        users.save(user);
        return new AuthResponse(jwtService.generateToken(user.getEmail(), user.getRole().name()));
    }

    public AuthResponse login(LoginRequest req) {
        // throws on bad credentials → 401
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = users.findByEmail(req.email()).orElseThrow();
        return new AuthResponse(jwtService.generateToken(user.getEmail(), user.getRole().name()));
    }
}
