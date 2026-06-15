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

    @Autowired
    private AuditLogService auditLogService;


    public UserResponseDTO register(RegisterRequestDTO dto) {

        if ("ADMIN".equalsIgnoreCase(dto.getRole())) {
            long adminCount = userRepository.countByRole("ADMIN");
            if (adminCount >= 1) {
                auditLogService.log("AUTH.REGISTER_FAILED | Error: Admin already exists, blocked admin registration for "
                        + dto.getEmail());
                throw new BadRequestException("An admin already exists. Cannot register another admin.");
            }

        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            auditLogService.log(
                    "AUTH.REGISTER_FAILED | Error: Email already registered: " + dto.getEmail());
            throw new BadRequestException("Email already registered: " + dto.getEmail());
        }

        User user = new User();
        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setPhoneNumber(dto.getPhoneNumber());

        User saved = userRepository.save(user);

        auditLogService.log(
                "AUTH.REGISTER_SUCCESS | UserID: " + saved.getUserId()
                        + " | Email: " + saved.getEmail()
                        + " | Role: " + saved.getRole(),
                saved.getEmail());

        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(saved.getUserId());
        response.setUserName(saved.getUserName());
        response.setEmail(saved.getEmail());
        response.setRole(saved.getRole());
        response.setPhoneNumber(saved.getPhoneNumber());
        return response;
    }


    public LoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);


        if (user == null) {

            auditLogService.log(
                    "AUTH.LOGIN_FAILED | Error: No account found for email: " + dto.getEmail());
            throw new BadRequestException("Invalid email or password");
        }


        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            auditLogService.logFailure(
                    "AUTH.LOGIN",
                    "Wrong password for email: " + dto.getEmail(),
                    dto.getEmail());
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        auditLogService.log(
                "AUTH.LOGIN_SUCCESS | Email: " + user.getEmail()
                        + " | Role: " + user.getRole(),
                user.getEmail());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setUserName(user.getUserName());
        response.setUserId(user.getUserId());
        return response;
    }
}