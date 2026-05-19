package com.project.retailproject.controller;

import com.project.retailproject.common.ApiResponse;
import com.project.retailproject.dto.LoginRequestDTO;
import com.project.retailproject.dto.LoginResponseDTO;
import com.project.retailproject.dto.RegisterRequestDTO;
import com.project.retailproject.dto.UserResponseDTO;
import com.project.retailproject.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO dto) {
        UserResponseDTO data = authService.register(dto);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        LoginResponseDTO data = authService.login(dto);
        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }
}