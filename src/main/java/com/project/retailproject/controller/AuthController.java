package com.project.retailproject.controller;

import com.project.retailproject.dto.LoginRequestDTO;
import com.project.retailproject.dto.LoginResponseDTO;
import com.project.retailproject.dto.RegisterRequestDTO;
import com.project.retailproject.dto.UserResponseDTO;
import com.project.retailproject.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}