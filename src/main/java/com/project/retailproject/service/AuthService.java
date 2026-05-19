package com.project.retailproject.service;

import com.project.retailproject.db.UserRepository;
import com.project.retailproject.dto.LoginRequestDTO;
import com.project.retailproject.dto.LoginResponseDTO;
import com.project.retailproject.dto.RegisterRequestDTO;
import com.project.retailproject.dto.UserResponseDTO;
import com.project.retailproject.exception.BadRequestException;
import com.project.retailproject.model.User;
import com.project.retailproject.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register
    public UserResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already registered: " + dto.getEmail());
        }

        User user = new User();
        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // hashed!
        user.setRole(dto.getRole());
        user.setPhoneNumber(dto.getPhoneNumber());

        User saved = userRepository.save(user);

        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(saved.getUserId());
        response.setUserName(saved.getUserName());
        response.setEmail(saved.getEmail());
        response.setRole(saved.getRole());
        response.setPhoneNumber(saved.getPhoneNumber());
        return response;
    }

    // Login
    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setUserName(user.getUserName());
        return response;
    }
}